//package org.ireader.domain.catalog_installer
//
//import android.app.Application
//import io.ktor.client.request.*
//import io.ktor.http.*
//import io.ktor.utils.io.*
//import kotlinx.coroutines.flow.flow
//import org.ireader.core.io.saveTo
//
//import org.ireader.domain.catalog.model.InstallStep.*
//import org.ireader.domain.catalog.service.CatalogInstaller
//import org.ireader.domain.models.entities.CatalogRemote
//import tachiyomi.core.http.HttpClients
//import tachiyomi.core.log.Log
//import java.io.File
//
///**
// * The installer which installs, updates and uninstalls the extensions.
// *
// * @param context The application context.
// */
//class AndroidCatalogInstaller(
//  private val context: Application,
//  private val httpClients: HttpClients,
//  private val installationChanges: AndroidCatalogInstallationChanges,
//) : CatalogInstaller {
//
//  /**
//   * The client used for http requests.
//   */
//  private val client get() = httpClients.default
//
//  /**
//   * Adds the given extension to the downloads queue and returns an observable containing its
//   * step in the installation process.
//   *
//   * @param catalog The catalog to install.
//   */
//  override fun install(catalog: CatalogRemote) = flow {
//    emit(Downloading)
//    val tmpApkFile = File(context.cacheDir, "${catalog.pkgName}.apk")
//    val tmpIconFile = File(context.cacheDir, "${catalog.pkgName}.png")
//    try {
//      val apkResponse = client.get<ByteReadChannel>(catalog.pkgUrl) {
//        headers.append(HttpHeaders.CacheControl, "no-store")
//      }
//      apkResponse.saveTo(tmpApkFile)
//
//      val iconResponse = client.get<ByteReadChannel>(catalog.iconUrl) {
//        headers.append(HttpHeaders.CacheControl, "no-store")
//      }
//      iconResponse.saveTo(tmpIconFile)
//
//      emit(Installing)
//
//      val extDir = File(context.filesDir, "catalogs/${catalog.pkgName}").apply { mkdirs() }
//      val apkFile = File(extDir, tmpApkFile.name)
//      val iconFile = File(extDir, tmpIconFile.name)
//
//      val apkSuccess = tmpApkFile.renameTo(apkFile)
//      val iconSuccess = tmpIconFile.renameTo(iconFile)
//      val success = apkSuccess && iconSuccess
//      if (success) {
//        installationChanges.notifyAppInstall(catalog.pkgName)
//      }
//
//      emit(if (success) Completed else Error)
//    } catch (e: Exception) {
//      Log.warn(e, "Error installing package")
//      emit(Error)
//    } finally {
//      tmpApkFile.delete()
//      tmpIconFile.delete()
//    }
//  }
//
//  /**
//   * Starts an intent to uninstall the extension by the given package name.
//   *
//   * @param pkgName The package name of the extension to uninstall
//   */
//  override suspend fun uninstall(pkgName: String): Boolean {
//    val file = File(context.filesDir, "catalogs/${pkgName}")
//    val deleted = file.deleteRecursively()
//    installationChanges.notifyAppUninstall(pkgName)
//    return deleted
//  }
//
//}