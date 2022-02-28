package org.ireader.domain.view_models.explore

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.domain.R
import org.ireader.domain.catalog.service.CatalogStore
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.entities.Book
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.remote.RemoteUseCases
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Listing
import tachiyomi.source.model.MangasPageInfo
import javax.inject.Inject


@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val preferencesUseCase: org.ireader.domain.use_cases.preferences.reader_preferences.PreferencesUseCase,
    private val remoteUseCases: RemoteUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val catalogStore: CatalogStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var state: MutableState<ExploreScreenState> =
        mutableStateOf<ExploreScreenState>(ExploreScreenState())
        private set
    var filterState: MutableState<BrowseFilterState> =
        mutableStateOf<BrowseFilterState>(BrowseFilterState())
        private set

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        val sourceId = savedStateHandle.get<Long>("sourceId")
        val source =
            catalogStore.catalogs.find { it.source.id == sourceId }?.source
        if (sourceId != null && source is CatalogSource) {
            if (source != null) {
                state.value = state.value.copy(source = source)
                state.value = state.value.copy(exploreType = source.getListings().first())
                getBooks(source = source)
                readLayoutType()
            } else {
                viewModelScope.launch {
                    showSnackBar(UiText.StringResource(org.ireader.core.R.string.the_source_is_not_found))
                }
            }
        } else {
            viewModelScope.launch {
                showSnackBar(UiText.StringResource(org.ireader.core.R.string.something_is_wrong_with_this_book))
            }
        }

    }

    private val _books = MutableStateFlow<PagingData<Book>>(PagingData.empty())
    val books = _books



    fun onEvent(event: ExploreScreenEvents) {
        when (event) {
            is ExploreScreenEvents.OnLayoutTypeChnage -> {
                saveLayoutType(event.layoutType)
            }
            is ExploreScreenEvents.ToggleMenuDropDown -> {
                toggleMenuDropDown(isShown = event.isShown)
            }
            is ExploreScreenEvents.ToggleSearchMode -> {
                toggleSearchMode(event.inSearchMode)
            }
            is ExploreScreenEvents.OnQueryChange -> {
                onQueryChange(event.query)
            }
        }
    }

    private var getBooksJob: Job? = null

    @OptIn(ExperimentalPagingApi::class)
    fun getBooks(query: String? = null, listing: Listing? = null, source: CatalogSource) {
        getBooksJob?.cancel()
        getBooksJob = viewModelScope.launch(Dispatchers.Main) {
            remoteUseCases.getRemoteBookByPaginationUseCase(
                source,
                listing ?: state.value.exploreType,
                query = query).cachedIn(viewModelScope)
                .collect { snapshot ->
                    _books.value = snapshot
                }
        }
    }

    private fun onQueryChange(query: String) {
        state.value = state.value.copy(searchQuery = query)
    }

    private fun toggleSearchMode(inSearchMode: Boolean) {
        state.value =
            state.value.copy(isSearchModeEnable = inSearchMode)
        val source = state.value.source
        if (!inSearchMode && source != null && source is CatalogSource) {
            exitSearchedMode()
            getBooks(source = source)
        }
    }

    private fun exitSearchedMode() {
        state.value = state.value.copy(
            searchedBook = MangasPageInfo(emptyList(), false),
            searchQuery = "",
            isLoading = false,
            error = UiText.StringResource(R.string.no_error))
    }

    private fun saveLayoutType(layoutType: DisplayMode) {
        state.value = state.value.copy(layout = layoutType.layout)
        preferencesUseCase.saveBrowseLayoutUseCase(layoutType.layoutIndex)
    }

    private fun readLayoutType() {
        state.value =
            state.value.copy(layout = preferencesUseCase.readBrowseLayoutUseCase().layout)
    }

    private fun toggleMenuDropDown(isShown: Boolean) {
        state.value = state.value.copy(isMenuDropDownShown = isShown)
    }

    suspend fun showSnackBar(message: UiText?) {
        _eventFlow.emit(
            UiEvent.ShowSnackbar(
                uiText = message ?: UiText.StringResource(org.ireader.core.R.string.error_unknown)
            )
        )
    }

    fun toggleFilterMode(enable: Boolean? = null) {
        state.value = state.value.copy(isFilterEnable = enable ?: !state.value.isFilterEnable)
    }

}