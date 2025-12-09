package com.example.gjgn_02v.data.model.records

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WeightDao {

    @Insert
    suspend fun insert(weight: WeightEntity)

    @Query("SELECT * FROM weight_table ORDER BY id DESC")
    suspend fun getAll(): List<WeightEntity>
}
