package com.example.brewnote.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Vendor(
    val _id: String,
    val _creationTime: Double = 0.0,
    val name: String,
    val type: String,
    val url: String? = null,
    val createdById: String,
    val createdAt: Double = 0.0,
    val beans: List<Bean> = emptyList(),
    val equipment: List<Equipment> = emptyList(),
)
