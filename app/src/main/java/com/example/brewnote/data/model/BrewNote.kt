package com.example.brewnote.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BrewNote(
    val _id: String,
    val _creationTime: Double = 0.0,
    val brewMethodId: String? = null,
    val notes: String? = null,
    val grindSize: Double? = null,
    val roast: String? = null,
    val roastDate: Double? = null,
    val beansWeight: Double? = null,
    val beansWeightType: String? = null,
    val brewTemperature: Double? = null,
    val brewTemperatureType: String? = null,
    val brewTime: Double? = null,
    val waterToGrindRatio: String? = null,
    val rating: Double? = null,
    val createdById: String,
    val createdAt: Double = 0.0,
    val beans: List<Bean> = emptyList(),
    val equipment: List<Equipment> = emptyList(),
    val brewMethodDoc: BrewMethod? = null,
)
