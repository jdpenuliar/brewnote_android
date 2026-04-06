package com.example.brewnote.ui.beannotes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clerk.api.Clerk
import com.example.brewnote.ui.components.DetailRow
import com.example.brewnote.ui.components.EmptyState
import com.example.brewnote.ui.components.RatingRow
import com.example.brewnote.ui.components.SectionHeader
import com.example.brewnote.ui.components.SkeletonListItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeanNoteDetailScreen(
    id: String,
    onNavigateToEdit: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: BeanNotesViewModel = viewModel(),
) {
    LaunchedEffect(id) {
        viewModel.loadDetail(id)
    }

    val detailState by viewModel.detailState.collectAsState()
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bean Note") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = detailState) {
            is BeanNoteDetailUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    repeat(3) { SkeletonListItem() }
                }
            }
            is BeanNoteDetailUiState.Error -> {
                EmptyState(message = state.message)
            }
            is BeanNoteDetailUiState.Success -> {
                val doc = state.item
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    DetailRow(
                        label = "Added",
                        value = dateFormat.format(Date(doc._creationTime.toLong()))
                    )

                    SectionHeader(title = "Rating")
                    RatingRow(
                        rating = doc.personalRating?.toDouble(),
                        maxRating = 5,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    if (doc.beans.isNotEmpty()) {
                        DetailRow(
                            label = "Beans",
                            value = doc.beans.joinToString(", ") { it.name }
                        )
                    }

                    doc.tastingNotes?.let {
                        DetailRow(label = "Tasting Notes", value = it)
                    }

                    if (doc.price != null) {
                        val priceStr = if (doc.currency != null) "${doc.price} ${doc.currency}" else "${doc.price}"
                        DetailRow(label = "Price", value = priceStr)
                    }

                    doc.roastDate?.let {
                        DetailRow(
                            label = "Roast Date",
                            value = dateFormat.format(Date(it.toLong()))
                        )
                    }

                    if (doc.createdById == Clerk.userFlow.collectAsState().value?.id) {
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedButton(
                            onClick = onNavigateToEdit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text("Edit")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
