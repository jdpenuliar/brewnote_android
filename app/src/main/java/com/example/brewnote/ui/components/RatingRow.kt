package com.example.brewnote.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RatingRow(
    rating: Double?,
    maxRating: Int = 5,
    onRatingSelected: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        (1..maxRating).forEach { star ->
            val filled = rating != null && star.toDouble() <= rating
            val icon = if (filled) Icons.Filled.Star else Icons.Outlined.StarOutline
            Icon(
                imageVector = icon,
                contentDescription = "$star star",
                modifier = Modifier
                    .size(20.dp)
                    .then(
                        if (onRatingSelected != null) {
                            Modifier.clickable { onRatingSelected(star) }
                        } else Modifier
                    ),
                tint = if (filled) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (star < maxRating) Spacer(modifier = Modifier.width(2.dp))
        }
    }
}
