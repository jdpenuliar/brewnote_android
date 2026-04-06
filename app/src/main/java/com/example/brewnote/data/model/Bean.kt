package com.example.brewnote.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Bean(
    val _id: String,
    val _creationTime: Double = 0.0,
    val name: String,
    val species: List<String> = emptyList(),
    val countryOfOrigin: String,
    val openFoodFactsId: String? = null,
    val createdById: String,
    val createdAt: Double = 0.0,
)
