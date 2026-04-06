package com.example.brewnote.ui.brewmethods

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.brewnote.ui.components.SkeletonListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrewMethodsListScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToNew: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: BrewMethodsViewModel = viewModel(),
) {
    val brewMethodsState by viewModel.brewMethodsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Brew Methods") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToNew) {
                Icon(Icons.Default.Add, contentDescription = "New Brew Method")
            }
        }
    ) { innerPadding ->
        when (val state = brewMethodsState) {
            is BrewMethodsUiState.Loading -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    items(3) { SkeletonListItem() }
                }
            }
            is BrewMethodsUiState.Error -> {
                EmptyState(message = state.message)
            }
            is BrewMethodsUiState.Success -> {
                if (state.items.isEmpty()) {
                    EmptyState(message = "No brew methods yet")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        items(state.items) { method ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                onClick = { onNavigateToDetail(method._id) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "⚗️", style = MaterialTheme.typography.bodyLarge)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val descSnippet = method.description
                                        ?.let { if (it.length > 40) it.take(40) + "…" else it }
                                        ?: ""
                                    val subtitle = if (descSnippet.isNotEmpty()) " · $descSnippet" else ""
                                    Text(
                                        text = "${method.name}$subtitle",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
