package ru.practicum.android.diploma.filter.placeselector.area.domain.usecase

import kotlinx.coroutines.flow.map
import ru.practicum.android.diploma.filter.placeselector.area.domain.api.AreaRepository
import ru.practicum.android.diploma.util.Result

class GetCountryByRegionUseCase(
    private val areaRepository: AreaRepository
) {
    /**
     * @param areaId parent_Id от региона для которого ищем страну
     */

    fun execute(areaId: String) = areaRepository.getAreaByRegion(areaId).map { result ->
        if (result is Result.Success) {
            Result.Success(result.data)
        } else {
            result
        }
    }
}
