package ru.practicum.android.diploma.filter.placeselector.area.domain.usecase

import ru.practicum.android.diploma.filter.domain.api.FilterRepository
import ru.practicum.android.diploma.filter.domain.models.FilterType
import ru.practicum.android.diploma.filter.placeselector.area.domain.model.Area

class SaveAreaUseCase(
    private val filterRepository: FilterRepository
) {
    fun execute(area: Area) {
        filterRepository.saveFilterToLocalStorage(FilterType.Region(
            id = area.id,
            name = area.name
        ))
    }
}
