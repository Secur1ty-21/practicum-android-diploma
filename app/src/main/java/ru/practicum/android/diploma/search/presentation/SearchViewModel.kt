package ru.practicum.android.diploma.search.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.core.domain.model.SearchFilterParameters
import ru.practicum.android.diploma.core.domain.model.SearchVacanciesResult
import ru.practicum.android.diploma.favourites.presentation.CLICK_DEBOUNCE_DELAY
import ru.practicum.android.diploma.filter.domain.models.FilterType
import ru.practicum.android.diploma.filter.domain.usecase.GetApplyFilterFlagUseCase
import ru.practicum.android.diploma.filter.domain.usecase.GetFiltersUseCase
import ru.practicum.android.diploma.search.domain.usecase.SearchVacancyUseCase
import ru.practicum.android.diploma.util.Resource

class SearchViewModel(
    private val searchVacancyUseCase: SearchVacancyUseCase,
    private val getFiltersUseCase: GetFiltersUseCase,
    private val getApplyFilterFlagUseCase: GetApplyFilterFlagUseCase
) : ViewModel() {
    private var isClickAllowed = true
    private val stateLiveData = MutableLiveData<SearchState>(SearchState.Default)
    private var isSearchByPageAllowed = true
    private var searchByTextJob: Job? = null
    private var previousSearchText = ""
    private var previousSearchPage = -1

    fun observeState(): LiveData<SearchState> = stateLiveData

    private fun renderState(state: SearchState) {
        stateLiveData.postValue(state)
    }

    fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            viewModelScope.launch(Dispatchers.IO) {
                delay(CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
        return current
    }

    fun searchByButton(searchText: String) {
        if (searchText.isNotEmpty()) {
            searchByTextJob?.cancel()
            viewModelScope.launch(Dispatchers.IO) {
                renderState(SearchState.Loading)
                searchVacancyUseCase.execute(searchText, 0, getFilterParameters()).collect {
                    processSearchByTextResult(it)
                }
            }
        }
    }

    private fun getFilterParameters(): SearchFilterParameters {
        if (!getApplyFilterFlagUseCase.execute()) {
            return SearchFilterParameters()
        }
        var parameters = SearchFilterParameters()
        getFiltersUseCase.execute().forEach {
            when (it) {
                is FilterType.Country -> {
                    if (parameters.regionId.isEmpty()) {
                        parameters = parameters.copy(regionId = it.id)
                    }
                }

                is FilterType.Region -> {
                    parameters = parameters.copy(regionId = it.id)
                }

                is FilterType.Salary -> {
                    parameters = parameters.copy(salary = it.amount)
                }

                is FilterType.Industry -> {
                    parameters = parameters.copy(industriesId = it.id)
                }

                is FilterType.ShowWithSalaryFlag -> {
                    parameters = parameters.copy(isOnlyWithSalary = it.flag)
                }
            }
        }
        return parameters
    }

    fun searchByText(searchText: String) {
        searchByTextJob?.cancel()
        if (searchText.isNotEmpty() && searchText != previousSearchText) {
            searchByTextJob = viewModelScope.launch(Dispatchers.IO) {
                delay(SEARCH_DEBOUNCE_DELAY)
                previousSearchText = searchText
                renderState(SearchState.Loading)
                searchVacancyUseCase.execute(searchText, 0, getFilterParameters()).collect {
                    processSearchByTextResult(it)
                }
            }
        } else if (searchText.isEmpty()) {
            clearSearch()
        }
    }

    private fun processSearchByTextResult(result: Resource<SearchVacanciesResult>) {
        when (result) {
            is Resource.Success -> {
                if (result.data?.vacancies.isNullOrEmpty()) {
                    stateLiveData.postValue(SearchState.EmptyResult)
                } else {
                    clearSearch()
                    stateLiveData.postValue(SearchState.Content(result.data))
                }
            }

            is Resource.InternetError -> {
                stateLiveData.postValue(SearchState.NetworkError)
            }

            is Resource.ServerError -> {
                stateLiveData.postValue(SearchState.ServerError)
            }
        }
        previousSearchPage = if (result is Resource.Success && !result.data?.vacancies.isNullOrEmpty()) {
            0
        } else {
            -1
        }
    }

    fun searchByPage(searchText: String, page: Int) {
        if (isSearchByPageAllowed(searchText, page)) {
            isSearchByPageAllowed = false
            viewModelScope.launch(Dispatchers.IO) {
                searchVacancyUseCase.execute(searchText, page, getFilterParameters()).collect {
                    processSearchByPageResult(it, page)
                    delay(SEARCH_PAGINATION_DELAY)
                    isSearchByPageAllowed = true
                }
            }
        }
    }

    private fun processSearchByPageResult(result: Resource<SearchVacanciesResult>, page: Int) {
        when (result) {
            is Resource.Success -> {
                if (result.data != null && result.data.vacancies.isNotEmpty()) {
                    previousSearchPage = page
                    stateLiveData.postValue(
                        SearchState.Pagination(result.data)
                    )
                } else {
                    stateLiveData.postValue(
                        SearchState.Pagination(
                            SearchVacanciesResult(result.data?.numOfResults ?: 0, emptyList())
                        )
                    )
                }
            }

            is Resource.ServerError -> {
                stateLiveData.postValue(
                    SearchState.Pagination(
                        SearchVacanciesResult(0, emptyList()),
                        SearchState.ServerError
                    )
                )
            }

            is Resource.InternetError -> {
                stateLiveData.postValue(
                    SearchState.Pagination(
                        SearchVacanciesResult(0, emptyList()),
                        SearchState.NetworkError
                    )
                )
            }
        }
    }

    private fun isSearchByPageAllowed(searchText: String, page: Int): Boolean {
        return isSearchByPageAllowed && searchText.isNotEmpty() && searchByTextJob?.isCompleted != false &&
            previousSearchPage != page
    }

    fun clearSearch() {
        stateLiveData.postValue(SearchState.Default)
    }

    fun isFilterApplied(): Boolean {
        val filterParameters = getFilterParameters()
        return (filterParameters.regionId.isNotEmpty()
            || filterParameters.industriesId.isNotEmpty()
            || filterParameters.salary.isNotEmpty()
            || filterParameters.isOnlyWithSalary)
    }

    companion object {
        private const val SEARCH_PAGINATION_DELAY = 500L
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}
