package com.example.brewnote.data.model

import kotlinx.serialization.Serializable

@Serializable
data class HomeStats(
    val brewsThisWeek: Double,
    val topBrewMethod: String? = null,
)
