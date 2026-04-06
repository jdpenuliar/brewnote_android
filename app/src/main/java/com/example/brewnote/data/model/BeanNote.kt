package com.example.brewnote.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BeanNote(
    val _id: String,
    val _creationTime: Double = 0.0,
    val title: String,
    val roastDate: Double? = null,
    val tastingNotes: String? = null,
    val price: Double? = null,
    val currency: String? = null,
    val personalRating: Double? = null,
    val createdById: String,
    val createdAt: Double = 0.0,
    val beans: List<Bean> = emptyList(),
)
