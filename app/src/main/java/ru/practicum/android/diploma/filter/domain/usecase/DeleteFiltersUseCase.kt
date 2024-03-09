package ru.practicum.android.diploma.filter.domain.usecase

import ru.practicum.android.diploma.filter.domain.api.FilterRepository
import ru.practicum.android.diploma.filter.domain.models.FilterType

class DeleteFiltersUseCase(
    private val filterRepository: FilterRepository
) {
    fun execute() {
        filterRepository.deleteFilters()
    }

    fun execute(filterType: FilterType) {
        filterRepository.deleteFilter(filterType)
    }
}
