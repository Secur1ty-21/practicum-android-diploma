package ru.practicum.android.diploma.favourites.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.practicum.android.diploma.core.data.mapper.VacancyMapper
import ru.practicum.android.diploma.core.domain.model.DetailVacancy
import ru.practicum.android.diploma.favourites.data.db.AppDatabase
import ru.practicum.android.diploma.favourites.domain.api.GetFavouritesRepository
import java.lang.Exception

class GetFavouritesRepositoryImpl(private val appDatabase: AppDatabase) : GetFavouritesRepository {
    override suspend fun getFavouritesList(): Flow<List<DetailVacancy>?> = flow {
        val vacancy = try {
            appDatabase.vacancyDao().getVacancy()
        } catch (e: Exception) {
            null
        }
        emit(vacancy?.map { VacancyMapper.mapToDetailVacancy(it) })
    }

    override suspend fun fillVacList(vac: DetailVacancy) {
        appDatabase.vacancyDao().addVacancyToFavorites(VacancyMapper.mapToFavoritesEntity(vac))
    }

}
