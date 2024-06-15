package ru.practicum.android.diploma.filter.placeselector.area.presentation

import ru.practicum.android.diploma.filter.placeselector.area.domain.model.Area

sealed class AreaScreenState {
    class Content(val areas: ArrayList<Area>) : AreaScreenState()
    data object EmptyError : AreaScreenState()
    data object GetError : AreaScreenState()

}
