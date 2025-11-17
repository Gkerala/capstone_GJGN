package com.example.gjgn_02v.data.model.records

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_table")
data class WeightEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weight: Float,
    val date: String
)
