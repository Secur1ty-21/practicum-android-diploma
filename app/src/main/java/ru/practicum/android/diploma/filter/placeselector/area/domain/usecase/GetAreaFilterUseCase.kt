package ru.practicum.android.diploma.filter.placeselector.area.domain.usecase

import ru.practicum.android.diploma.filter.domain.api.FilterRepository
import ru.practicum.android.diploma.filter.placeselector.area.domain.model.Area

class GetAreaFilterUseCase(
    private val filterRepository: FilterRepository
) {
    fun execute(): Area? {
        return filterRepository.getAreaFilterFromLocalStorage()
    }
}
