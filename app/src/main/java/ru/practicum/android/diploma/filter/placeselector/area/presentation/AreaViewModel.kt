package ru.practicum.android.diploma.filter.placeselector.area.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.favourites.presentation.CLICK_DEBOUNCE_DELAY
import ru.practicum.android.diploma.filter.placeselector.area.domain.model.Area
import ru.practicum.android.diploma.filter.placeselector.area.domain.model.AreaError
import ru.practicum.android.diploma.filter.placeselector.area.domain.usecase.GetAreasByTextUseCase
import ru.practicum.android.diploma.filter.placeselector.area.domain.usecase.GetCountryByRegionUseCase
import ru.practicum.android.diploma.util.Result

class AreaViewModel(private val areaUseCase: GetAreasByTextUseCase, private val countryUseCase: GetCountryByRegionUseCase) : ViewModel() {
    private val stateLiveData = MutableLiveData<AreaScreenState>()
    private var isClickAllowed = true
    private val areas: ArrayList<Area> = arrayListOf()


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
        }
    }

    fun getCountryByRegion(areaId: String): Area? {
        var area: Area? = null
        viewModelScope.launch(Dispatchers.IO) {
            countryUseCase.execute(areaId).collect {result ->
                if (result is Result.Success) {
                    area = result.data
                }
            }
        }
        return area
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
