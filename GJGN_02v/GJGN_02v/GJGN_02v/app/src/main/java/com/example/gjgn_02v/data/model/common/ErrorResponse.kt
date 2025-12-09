package com.example.gjgn_02v.data.model.common

data class ErrorResponse(
    val error: String,
    val detail: String? = null
)