package ru.practicum.android.diploma.filter.presentation

import ru.practicum.android.diploma.core.domain.model.Country
import ru.practicum.android.diploma.filter.industry.domain.model.Industry
import ru.practicum.android.diploma.filter.placeselector.area.domain.model.Area

data class FilterState(
    val country: Country? = null,
    val area: Area? = null,
    val industry: Industry? = null,
    val salary: Int? = null,
    val isNotShowWithoutSalary: Boolean = false
)
