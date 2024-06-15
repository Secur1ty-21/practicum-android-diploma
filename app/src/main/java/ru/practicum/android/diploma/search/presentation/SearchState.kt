package ru.practicum.android.diploma.search.presentation

import ru.practicum.android.diploma.core.domain.model.SearchVacanciesResult

sealed interface SearchState {
    data object Default : SearchState
    data object Loading : SearchState
    data class Content(val data: SearchVacanciesResult?) : SearchState
    data object NetworkError : SearchState
    data object EmptyResult : SearchState
    data object ServerError : SearchState
    data class Pagination(val data: SearchVacanciesResult, val error: SearchState? = null) : SearchState
}
