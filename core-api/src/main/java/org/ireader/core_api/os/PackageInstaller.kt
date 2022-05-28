package org.ireader.core_api.os

import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import androidx.core.net.toUri
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ireader.common_resources.UiText
import org.ireader.common_resources.string
import org.ireader.core_api.R
import org.ireader.core_api.log.Log
import org.ireader.core_api.util.calculateSizeRecursively
import java.io.File

class PackageInstaller(
    private val context: Application
) {
    private val packageInstaller = context.packageManager.packageInstaller

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun install(file: File, packageName: String): InstallStep {
        return withInstallReceiver { sender ->
            withContext(Dispatchers.IO) {
                val installParams =
                    PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    installParams.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                }
                val fileSize = file.calculateSizeRecursively()
                installParams.setAppPackageName(packageName)
                installParams.setSize(fileSize)
                val sessionId = packageInstaller.createSession(installParams)
                packageInstaller.openSession(sessionId).use { session ->
                    val output = session.openWrite(packageName, 0, fileSize)
                    output.use {
                        file.inputStream().use { input ->
                            input.copyTo(output)
                        }
                        session.fsync(output)
                    }
                    session.commit(sender)
                }
            }
        }
    }

    suspend fun uninstall(packageName: String): InstallStep {
        return withInstallReceiver { sender ->
            packageInstaller.uninstall(packageName, sender)
        }
    }

    private suspend fun withInstallReceiver(block: suspend (IntentSender) -> Unit): InstallStep {
        val deferred = CompletableDeferred<InstallStep>()
        val receiver = InstallResultReceiver(context, deferred)
        val uid = SystemClock.elapsedRealtime()
        val action = "core-api.INSTALL_APK_$uid"
        // FLAG_MUTABLE is needed for android 12
        val broadcast = PendingIntent.getBroadcast(
            context,
            0,
            Intent(action),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        )
        context.registerReceiver(receiver, IntentFilter(action))
        return try {
            block(broadcast.intentSender)
            deferred.await()
        } finally {
            context.unregisterReceiver(receiver)
            broadcast.cancel()
        }
    }

    private class InstallResultReceiver(
        val context: Application,
        val deferred: CompletableDeferred<InstallStep>
    ) : BroadcastReceiver() {

        override fun onReceive(ctx: Context?, intent: Intent?) {
            intent ?: return
            val status = intent
                .getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)

            Log.error { intent.extras.toString() }
            when (status) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    val confirmationIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                    if (confirmationIntent == null) {
                        Log.warn { "Fatal error for $intent" }
                        deferred.complete(InstallStep.Error(UiText.StringResource(R.string.fatal_error)))
                        return
                    }
                    confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    try {
                        context.startActivity(confirmationIntent)

                        // Mark installation as completed even if it's not finished because we don't always
                        // receive the result callback
                        deferred.complete(InstallStep.Idle)
                    } catch (e: Throwable) {
                        Log.warn("Error while (un)installing package", e)
                        deferred.complete(InstallStep.Error(UiText.StringResource(R.string.installation_error)))
                    }
                }
                PackageInstaller.STATUS_SUCCESS -> {
                    deferred.complete(InstallStep.Success)
                }
                PackageInstaller.STATUS_FAILURE_ABORTED -> {
                    deferred.complete(InstallStep.Error(UiText.StringResource(R.string.installation_error_aborted)))
                    org.ireader.core_api.log.Log.warn(
                        "Package installer failed to install packages",
                        status.toString()
                    )
                }
                PackageInstaller.STATUS_FAILURE_BLOCKED -> {
                    deferred.complete(InstallStep.Error(UiText.StringResource(R.string.installation_error_blocked)))
                }
                PackageInstaller.STATUS_FAILURE_CONFLICT -> {
                    deferred.complete(InstallStep.Error(UiText.StringResource(R.string.installation_error_conflicted)))
                }
                PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> {
                    deferred.complete(InstallStep.Error(UiText.StringResource(R.string.installation_error_incompatible)))
                }
                PackageInstaller.STATUS_FAILURE_STORAGE -> {

                    deferred.complete(InstallStep.Error(UiText.StringResource(R.string.installation_error_Storage)))
                }
                PackageInstaller.STATUS_FAILURE_INVALID -> {
                    deferred.complete(InstallStep.Error(UiText.StringResource(R.string.installation_error_invalid)))
                }
                else -> {
                    org.ireader.core_api.log.Log.error(
                        "Package installer failed to install packages",
                        status.toString()
                    )
                    deferred.complete(InstallStep.Error(UiText.DynamicString(context.string(R.string.package_installer_failed_to_install_packages) + " $status")))
                }
            }
        }
    }


    companion object {
        const val APK_MIME = "application/vnd.android.package-archive"
        const val EXTRA_PKG_NAME = "ExtensionInstaller.extra.PKG_NAME"
        const val EXTRA_FILE_PATH = "ExtensionInstaller.extra.FILE_PATH"
        const val FILE_SCHEME = "file://"
    }
    fun <T> installApk(uri: Uri,pkgName: String,file:File, targetActivity:Class<T>) {
        val intent = Intent(context, targetActivity)
            .setDataAndType(uri, org.ireader.core_api.os.PackageInstaller.APK_MIME)
            .putExtra(EXTRA_PKG_NAME, pkgName)
            .putExtra(EXTRA_FILE_PATH, file.absolutePath)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)

    }
    /**
     * Starts an intent to uninstall the extension by the given package name.
     *
     * @param pkgName The package name of the extension to uninstall
     */
    fun uninstallApk(pkgName: String) {
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, "package:$pkgName".toUri())
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
    }
}


private const val INSTALL_ACTION = "PackageInstallerInstaller.INSTALL_ACTION"

sealed class InstallStep(val name: UiText? = null, error: UiText? = null) {
    object Success : InstallStep(UiText.StringResource(R.string.success), null)
    object Downloading : InstallStep(UiText.StringResource(R.string.downloading), null)
    object Idle : InstallStep()
    data class Error(val error: UiText) : InstallStep(UiText.StringResource(R.string.failed), error)

    fun isFinished(): Boolean {
        return this is Idle || this is Error || this is Success
    }

    fun isLoading(): Boolean {
        return this is Downloading
    }
}