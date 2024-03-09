package ru.practicum.android.diploma.filter.placeselector.area.domain.model

sealed interface AreaError {
    data object NotFound : AreaError
    data object GetError : AreaError
}
