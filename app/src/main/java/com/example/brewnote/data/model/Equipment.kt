package com.example.brewnote.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Equipment(
    val _id: String,
    val _creationTime: Double = 0.0,
    val name: String,
    val brand: String? = null,
    val createdById: String,
    val createdAt: Double = 0.0,
    val vendors: List<Vendor> = emptyList(),
)
