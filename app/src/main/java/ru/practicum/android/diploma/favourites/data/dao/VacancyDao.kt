package ru.practicum.android.diploma.favourites.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.practicum.android.diploma.favourites.data.entity.FavoriteEntity

@Dao
interface VacancyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addVacancyToFavorites(vacancy: FavoriteEntity)

    @Delete
    suspend fun removeVacancyFromFavorites(vacancy: FavoriteEntity)

    @Query("SELECT * FROM vacancy_favorites_table ORDER BY insertionTime DESC")
    suspend fun getVacancy(): List<FavoriteEntity>?

    @Query("SELECT * FROM vacancy_favorites_table WHERE id=:id")
    suspend fun getVacancyById(id: Long): FavoriteEntity?
}
