/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.domain.catalog.interactor

import org.ireader.domain.catalog.service.CatalogInstaller
import org.ireader.domain.models.entities.CatalogInstalled
import javax.inject.Inject

class UninstallCatalog @Inject constructor(
    private val catalogInstaller: CatalogInstaller,

) {

    suspend fun await(catalog: CatalogInstalled,onError : (Throwable) -> Unit = {}): Boolean {
        return catalogInstaller.uninstall(catalog.pkgName,onError)
    }

}
