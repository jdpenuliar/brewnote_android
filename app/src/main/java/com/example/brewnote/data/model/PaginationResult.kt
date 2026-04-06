package com.example.brewnote.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PaginationResult<T>(
    val page: List<T>,
    val isDone: Boolean,
    val continueCursor: String,
)
