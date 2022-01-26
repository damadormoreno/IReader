package ir.kazemcodes.infinity.feature_sources.presentation.extension.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import ir.kazemcodes.infinity.feature_sources.presentation.extension.ExtensionViewModel


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserSourcesScreen(viewModel: ExtensionViewModel) {
    val scrollState = rememberLazyListState()
    val sources = viewModel.state.value.sources
    val backstack = LocalBackstack.current
    SourceList(modifier = Modifier.padding(bottom = 50.dp),sources = sources,scrollState=scrollState)
}