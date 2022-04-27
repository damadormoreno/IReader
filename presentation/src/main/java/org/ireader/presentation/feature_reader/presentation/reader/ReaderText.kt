package org.ireader.presentation.feature_reader.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ireader.core_ui.ui_components.LazyColumnScrollbar
import org.ireader.domain.models.entities.Chapter
import org.ireader.presentation.feature_reader.presentation.reader.reverse_swip_refresh.ISwipeRefreshIndicator
import org.ireader.presentation.feature_reader.presentation.reader.reverse_swip_refresh.MultiSwipeRefresh
import org.ireader.presentation.feature_reader.presentation.reader.reverse_swip_refresh.SwipeRefreshState
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.ReaderScreenViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReaderText(
    modifier: Modifier = Modifier,
    vm: ReaderScreenViewModel,
    chapter: Chapter,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    swipeState: SwipeRefreshState,
    scrollState: LazyListState,
    modalState: ModalBottomSheetState,
) {

    val scope = rememberCoroutineScope()

    val interactionSource = remember { MutableInteractionSource() }


    Box(
        Modifier
            .clickable(interactionSource = interactionSource,
                indication = null) {
                vm.apply {
                    vm.toggleReaderMode(!vm.isReaderModeEnable)
                }
            }
            .fillMaxSize()
            .background(vm.backgroundColor)
            .wrapContentSize(Alignment.CenterStart)
    ) {

        MultiSwipeRefresh(
            modifier = Modifier.fillMaxSize(),
            state = swipeState,
            indicators = listOf(
                ISwipeRefreshIndicator(scrollState.firstVisibleItemScrollOffset == 0,
                    alignment = Alignment.TopCenter,
                    indicator = { state, trigger ->
                        ArrowIndicator(
                            icon = Icons.Default.KeyboardArrowUp,
                            swipeRefreshState = swipeState,
                            refreshTriggerDistance = 80.dp,
                            color = vm.textColor
                        )
                    }, onRefresh = {
                        onPrev()
                    }),
                ISwipeRefreshIndicator(scrollState.firstVisibleItemScrollOffset != 0,
                    alignment = Alignment.BottomCenter,
                    onRefresh = {
                        onNext()
                    },
                    indicator = { state, trigger ->
                        ArrowIndicator(
                            icon = Icons.Default.KeyboardArrowDown,
                            swipeRefreshState = swipeState,
                            refreshTriggerDistance = 80.dp,
                            color = vm.textColor
                        )
                    }),
            ),
        )
        {
            vm.stateContent?.value?.let { content ->
                LazyColumnScrollbar(
                    listState = scrollState,
                    padding = if (vm.scrollIndicatorPadding < 0) 0.dp else vm.scrollIndicatorPadding.dp,
                    thickness = if (vm.scrollIndicatorWith < 0) 0.dp else vm.scrollIndicatorWith.dp
                ) {
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier
                    ) {
                        items(count = content.size) { index ->
                            TextSelectionContainer(selectable = vm.selectableMode) {
                                Text(
                                    modifier = modifier
                                        .padding(horizontal = vm.paragraphsIndent.dp)
                                        .background(if (index in vm.queriedTextIndex) vm.textColor.copy(
                                            .1f) else Color.Transparent),
                                    text = if (index == 0) "\n\n" + content[index].plus(
                                        "\n".repeat(
                                            vm.distanceBetweenParagraphs)) else content[index].plus(
                                        "\n".repeat(vm.distanceBetweenParagraphs)),
                                    fontSize = vm.fontSize.sp,
                                    fontFamily = vm.font.fontFamily,
                                    textAlign = TextAlign.Start,
                                    color = vm.textColor,
                                    lineHeight = vm.lineHeight.sp,
                                )
                            }
                        }

                    }
                }

            }

        }


        if (!vm.verticalScrolling) {
            Row(modifier = Modifier.fillMaxSize()) {

                Box(Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .clickable(interactionSource = interactionSource,
                        indication = null) {
                        scope.launch {

                            if (scrollState.layoutInfo.viewportStartOffset != scrollState.firstVisibleItemScrollOffset) {
                                scrollState.scrollBy(-scrollState.layoutInfo.viewportEndOffset.toFloat())
                            } else {
                                onPrev()
                            }


                        }
                    }) {

                }
                Box(Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .clickable(interactionSource = interactionSource,
                        indication = null) {
                        vm.apply {
                            vm.toggleReaderMode(!vm.isReaderModeEnable)
                        }
                    }) {

                }
                Box(Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .clickable(interactionSource = interactionSource,
                        indication = null) {
                        scope.launch {
                            if (!scrollState.isScrolledToTheEnd()) {
                                scrollState.scrollBy(scrollState.layoutInfo.viewportEndOffset.toFloat())

                            } else {
                                onNext()
                            }

                        }
                    }) {

                }
            }
        }


    }
}

fun LazyListState.isScrolledToTheEnd(): Boolean {
    val lastItem = layoutInfo.visibleItemsInfo.lastOrNull()
    return lastItem == null || lastItem.size + lastItem.offset <= layoutInfo.viewportEndOffset
}

fun LazyGridState.isScrolledToTheEnd(): Boolean {
    val lastItem = layoutInfo.visibleItemsInfo.lastOrNull()
    return lastItem == null || lastItem.size.height + lastItem.offset.y <= layoutInfo.viewportEndOffset
}

@Composable
fun TextSelectionContainer(
    modifier: Modifier = Modifier,
    selectable: Boolean,
    content: @Composable () -> Unit,
) {
    when (selectable) {
        true -> SelectionContainer {
            content()
        }
        else -> {
            content()
        }
    }
}