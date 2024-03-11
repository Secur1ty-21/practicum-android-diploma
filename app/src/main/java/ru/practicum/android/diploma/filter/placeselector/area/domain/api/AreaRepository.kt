package ru.practicum.android.diploma.filter.placeselector.area.domain.api

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.filter.placeselector.area.domain.model.Area
import ru.practicum.android.diploma.filter.placeselector.area.domain.model.AreaError
import ru.practicum.android.diploma.util.Result

interface AreaRepository {
    fun getAreas(id: String): Flow<Result<List<Area>, AreaError>>

    fun getAllArea(): Flow<Result<List<Area>, AreaError>>

}
