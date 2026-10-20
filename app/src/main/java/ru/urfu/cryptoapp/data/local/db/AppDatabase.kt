package ru.urfu.cryptoapp.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoriteCoinEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoritesDao(): FavoritesDao

    companion object {
        const val NAME = "crypto.db"
    }
}
