package ru.practicum.android.diploma.filter.placeselector.area.domain.usecase

import kotlinx.coroutines.flow.map
import ru.practicum.android.diploma.filter.placeselector.area.domain.api.AreaRepository
import ru.practicum.android.diploma.util.Result

class GetAllAreaUseCase(
    private val areaRepository: AreaRepository
) {

    fun execute() = areaRepository.getAllArea().map { result ->
        if (result is Result.Success) {
            Result.Success(result.data)
        } else {
            result
        }
    }
}
