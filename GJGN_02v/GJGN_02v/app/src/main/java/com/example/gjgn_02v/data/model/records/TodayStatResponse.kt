package com.example.gjgn_02v.data.model.records

data class TodayStatResponse(
    val date: String,
    val total_kcal: Int,
    val count: Int,
    val recent: List<String>
)
