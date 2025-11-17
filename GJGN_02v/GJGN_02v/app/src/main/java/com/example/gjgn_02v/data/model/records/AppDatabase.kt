package com.example.gjgn_02v.data.model.records

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WeightEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weightDao(): WeightDao
}
