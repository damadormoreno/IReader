package ir.kazemcodes.infinity.feature_library.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.kazemcodes.infinity.feature_library.presentation.LibraryViewModel


@Composable
fun FilterScreen(viewModel: LibraryViewModel) {
    Column(Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background)
        .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top) {
        CheckBoxWithText("Unread",
            viewModel.state.value.unreadFilter.index == FilterType.Unread.index) {
            if (viewModel.state.value.unreadFilter == FilterType.Unread) {
                viewModel.enableUnreadFilter(FilterType.Disable)
            } else {
                viewModel.enableUnreadFilter(FilterType.Unread)
            }
        }
    }
}
