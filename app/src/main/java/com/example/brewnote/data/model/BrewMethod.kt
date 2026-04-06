package com.example.brewnote.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BrewMethod(
    val _id: String,
    val _creationTime: Double = 0.0,
    val name: String,
    val description: String? = null,
    val createdById: String,
    val createdAt: Double = 0.0,
)
