/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.presentation.feature_updates.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.ireader.core_ui.modifier.selectedBackground
import org.ireader.domain.feature_services.io.coil.rememberBookCover
import org.ireader.domain.models.entities.Update
import org.ireader.presentation.feature_history.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpdatesItem(
    book: Update,
    isSelected: Boolean,
    onClickItem: (Update) -> Unit,
    onLongClickItem: (Update) -> Unit,
    onClickCover: (Update) -> Unit,
    onClickDownload: (Update) -> Unit,
) {
    val alpha = if (book.read) 0.38f else 1f

    BookListItem(
        modifier = Modifier
            .combinedClickable(
                onClick = { onClickItem(book) },
                onLongClick = { onLongClickItem(book) }
            )
            .selectedBackground(isSelected)
            .height(56.dp)
            .fillMaxWidth()
            .padding(end = 4.dp)
    ) {
        BookListItemImage(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable { onClickCover(book) },
            mangaCover = rememberBookCover(book)
        )
        BookListItemColumn(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
                .alpha(alpha)
        ) {
            BookListItemTitle(
                text = book.bookTitle,
                fontWeight = FontWeight.SemiBold
            )
            BookListItemSubtitle(
                text = if (book.number == -1F) book.chapterTitle else "${book.number}  ${book.chapterTitle}"
            )
        }

        if (book.chapterDateUpload == 0L) {
            IconButton(onClick = { onClickDownload(book) }) {
                Icon(imageVector = Icons.Outlined.Download, contentDescription = "")
            }
        }

    }
}