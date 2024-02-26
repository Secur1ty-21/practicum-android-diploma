package ru.practicum.android.diploma.di

import org.koin.dsl.module
import ru.practicum.android.diploma.vacancy.domain.usecase.DetailVacancyUseCase
import ru.practicum.android.diploma.vacancy.domain.usecase.MakeCallUseCase
import ru.practicum.android.diploma.vacancy.domain.usecase.SendEmailUseCase
import ru.practicum.android.diploma.vacancy.domain.usecase.ShareVacancyUseCase
import ru.practicum.android.diploma.search.domain.usecase.SearchVacancyUseCase

val interactorModule = module {
    single {
        DetailVacancyUseCase(detailVacancyRepository = get())
    }
    single {
        MakeCallUseCase(externalNavigator = get())
    }
    single {
        SendEmailUseCase(externalNavigator = get())
    }

    single {
        ShareVacancyUseCase(externalNavigator = get())
    }
    single {
        SearchVacancyUseCase(searchVacancyRepository = get())
    }
}
