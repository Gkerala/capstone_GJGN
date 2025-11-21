package com.example.gjgn_02v.data.model.goals

data class GoalUpdateRequest(
    val goal_type: Int? = null,     // 1 유지 / 2 감량 / 3 증량
    val goal_weight: Float? = null,
    val activity_level: Int? = null // 1 ~ 5
)
