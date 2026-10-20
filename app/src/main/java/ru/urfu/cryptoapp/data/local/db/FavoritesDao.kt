package ru.urfu.cryptoapp.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    @Query("SELECT * FROM favorite_coins ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteCoinEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_coins WHERE id = :id)")
    fun observeExists(id: String): Flow<Boolean>

    @Upsert
    suspend fun upsert(coin: FavoriteCoinEntity)

    @Query("DELETE FROM favorite_coins WHERE id = :id")
    suspend fun delete(id: String)
}
