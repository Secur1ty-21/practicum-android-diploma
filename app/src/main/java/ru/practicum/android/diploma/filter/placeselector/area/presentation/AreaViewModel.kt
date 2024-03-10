package ru.practicum.android.diploma.filter.placeselector.area.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.core.data.mapper.mapToDomain
import ru.practicum.android.diploma.favourites.presentation.CLICK_DEBOUNCE_DELAY
import ru.practicum.android.diploma.filter.placeselector.area.domain.model.Area
import ru.practicum.android.diploma.filter.placeselector.area.domain.model.AreaError
import ru.practicum.android.diploma.filter.placeselector.area.domain.usecase.GetAreasByTextUseCase
import ru.practicum.android.diploma.filter.placeselector.area.domain.usecase.GetAllAreaUseCase
import ru.practicum.android.diploma.util.Result

class AreaViewModel(private val areaUseCase: GetAreasByTextUseCase, private val countryUseCase: GetAllAreaUseCase) : ViewModel() {
    private val stateLiveData = MutableLiveData<AreaScreenState>()
    private var isClickAllowed = true
    private val areas: ArrayList<Area> = arrayListOf()
    private var allAreas: ArrayList<Area> = arrayListOf()


    fun observeState(): LiveData<AreaScreenState> = stateLiveData

    fun getAreas(searchText: String, countryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            areaUseCase.execute(searchText, countryId).collect { result ->
                if (result is Result.Success) {
                    if (result.data.isEmpty()) {
                        renderState(AreaScreenState.EmptyError)
                    } else {
                        areas.clear()
                        areas.addAll(result.data)
                        areas.sortBy { it.name }
                        renderState(AreaScreenState.Content(areas))
                    }
                } else {
                    if ((result as Result.Error).errorType is AreaError.GetError) {
                        renderState(AreaScreenState.GetError)
                    } else {
                        renderState(AreaScreenState.EmptyError)
                    }
                }
            }

            countryUseCase.execute().collect { result ->
                if (result is Result.Success) {
                    if (result.data.isEmpty()) {
                        renderState(AreaScreenState.EmptyError)
                    } else {
                        allAreas.clear()
                        allAreas.addAll(result.data)
                    }
                } else {
                    if ((result as Result.Error).errorType is AreaError.GetError) {
                        renderState(AreaScreenState.GetError)
                    } else {
                        renderState(AreaScreenState.EmptyError)
                    }
                }
            }
        }
    }

    fun getCountryByRegion(areaId: String): Area? {
        var tempId = areaId
        while (!tempId.isNullOrEmpty()) {
            val area = allAreas.find {
                it.id == tempId
            }

            if (area?.parentId.isNullOrEmpty()) {
                return area
            } else {
                tempId = area?.parentId!!
            }
        }
        return null
    }

    private fun renderState(areaScreenState: AreaScreenState) {
        stateLiveData.postValue(areaScreenState)
    }

    fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            viewModelScope.launch(Dispatchers.IO) {
                delay(CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
        return current
    }
}
