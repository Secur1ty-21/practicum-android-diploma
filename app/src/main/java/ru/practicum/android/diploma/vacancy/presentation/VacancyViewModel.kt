package ru.practicum.android.diploma.vacancy.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.util.Resource
import ru.practicum.android.diploma.vacancy.domain.usecase.DetailVacancyUseCase
import ru.practicum.android.diploma.vacancy.domain.usecase.MakeCallUseCase
import ru.practicum.android.diploma.vacancy.domain.usecase.SendEmailUseCase
import ru.practicum.android.diploma.vacancy.domain.usecase.ShareVacancyUseCase

class VacancyViewModel(
    private val detailVacancyUseCase: DetailVacancyUseCase,
    private val makeCallUseCase: MakeCallUseCase,
    private val sendEmailUseCase: SendEmailUseCase,
    private val shareVacancyUseCase: ShareVacancyUseCase
) : ViewModel() {

    private val stateLiveData = MutableLiveData<VacancyScreenState>()
    fun observeState(): LiveData<VacancyScreenState> = stateLiveData

    fun getDetailVacancyById() {
        stateLiveData.postValue(VacancyScreenState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            detailVacancyUseCase.execute(TODO("Получить id в зависимости от способа передачи")).collect {
                when (it) {
                    is Resource.Success -> stateLiveData.postValue(VacancyScreenState.Content(it.data!!))
                    is Resource.InternetError -> stateLiveData.postValue(VacancyScreenState.Error)
                    is Resource.ServerError -> stateLiveData.postValue(VacancyScreenState.Error)
                }
            }
        }
    }

    fun makeCall(phoneNumber: String) {
        makeCallUseCase.execute(phoneNumber)
    }

    fun sendEmail(email: String) {
        sendEmailUseCase.execute(email)
    }

    fun shareVacancy() {
        shareVacancyUseCase.execute()
    }
}
