package com.example.brewnote.ui.brews

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brewnote.ui.components.EmptyState
import com.example.brewnote.ui.components.RatingRow
import com.example.brewnote.ui.components.SkeletonListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrewsListScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToNew: () -> Unit,
    viewModel: BrewsViewModel = viewModel(),
) {
    val brewsState by viewModel.brewsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Brews") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToNew) {
                Icon(Icons.Default.Add, contentDescription = "New Brew")
            }
        }
    ) { innerPadding ->
        when (val state = brewsState) {
            is BrewsUiState.Loading -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    items(3) { SkeletonListItem() }
                }
            }
            is BrewsUiState.Error -> {
                EmptyState(message = state.message)
            }
            is BrewsUiState.Success -> {
                if (state.items.isEmpty()) {
                    EmptyState(message = "No brews yet")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        items(state.items) { brew ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                onClick = { onNavigateToDetail(brew._id) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = "☕", style = MaterialTheme.typography.bodyLarge)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val beanName = brew.beans.firstOrNull()?.name ?: "No beans"
                                        val methodName = brew.brewMethodDoc?.name ?: "No method"
                                        Text(
                                            text = "$beanName – $methodName",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    RatingRow(rating = brew.rating?.toDouble(), maxRating = 5)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
