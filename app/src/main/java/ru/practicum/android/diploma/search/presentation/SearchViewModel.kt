package ru.practicum.android.diploma.search.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.core.domain.model.SearchFilterParameters
import ru.practicum.android.diploma.favourites.presentation.CLICK_DEBOUNCE_DELAY
import ru.practicum.android.diploma.search.domain.usecase.SearchVacancyUseCase
import ru.practicum.android.diploma.util.Resource

class SearchViewModel(private val searchVacancyUseCase: SearchVacancyUseCase) : ViewModel() {
    private var isClickAllowed = true
    private val stateLiveData = MutableLiveData<SearchState>(SearchState.Default)
    private var isSearchAllowed = true

    fun observeState(): LiveData<SearchState> = stateLiveData

    private fun renderState(state: SearchState) {
        stateLiveData.postValue(state)
    }

    fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            viewModelScope.launch {
                delay(CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
        return current
    }

    fun initSearch(
        searchText: String,
        page: Int,
        filterParameters: SearchFilterParameters
    ) {
        if (searchText.isNotEmpty() && isSearchAllowed) {
            renderState(SearchState.Loading)
            viewModelScope.launch(Dispatchers.IO) {
                searchVacancyUseCase
                    .execute(searchText, page, filterParameters)
                    .collect {
                        when (it) {
                            is Resource.Success -> {
                                if (it.data?.vacancies.isNullOrEmpty()) {
                                    stateLiveData.postValue(SearchState.EmptyResult)
                                } else {
                                    stateLiveData.postValue(SearchState.Content(it.data))
                                }
                            }

                            is Resource.InternetError -> {
                                stateLiveData.postValue(SearchState.NetworkError)
                            }

                            is Resource.ServerError -> {
                                stateLiveData.postValue(SearchState.EmptyResult)
                            }
                        }
                    }

            }
        }
    }

    fun searchByPage(searchText: String, page: Int, filterParameters: SearchFilterParameters) {
        if (isSearchAllowed && searchText.isNotEmpty()) {
            isSearchAllowed = false
            viewModelScope.launch(Dispatchers.IO) {
                searchVacancyUseCase.execute(searchText, page, filterParameters).collect {
                    if (it is Resource.Success && !it.data?.vacancies.isNullOrEmpty()) {
                        stateLiveData.postValue(SearchState.Content(it.data))
                    }
                    delay(SEARCH_PAGINATION_DELAY)
                    isSearchAllowed = true
                }
            }
        }
    }

    fun clearSearch() {
        stateLiveData.postValue(SearchState.Default)
    }

    companion object {
        private const val SEARCH_PAGINATION_DELAY = 500L
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}
