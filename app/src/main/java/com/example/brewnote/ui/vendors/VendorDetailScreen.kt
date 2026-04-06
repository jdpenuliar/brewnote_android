package com.example.brewnote.ui.vendors

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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clerk.api.Clerk
import com.example.brewnote.ui.components.DetailRow
import com.example.brewnote.ui.components.EmptyState
import com.example.brewnote.ui.components.SectionHeader
import com.example.brewnote.ui.components.SkeletonListItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDetailScreen(
    id: String,
    onNavigateToEdit: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: VendorsViewModel = viewModel(),
) {
    LaunchedEffect(id) {
        viewModel.loadDetail(id)
    }

    val detailState by viewModel.detailState.collectAsState()
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendor") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = detailState) {
            is VendorDetailUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    repeat(3) { SkeletonListItem() }
                }
            }
            is VendorDetailUiState.Error -> {
                EmptyState(message = state.message)
            }
            is VendorDetailUiState.Success -> {
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
                    DetailRow(label = "Type", value = doc.type)

                    doc.url?.let { url ->
                        SectionHeader(title = "URL")
                        androidx.compose.material3.TextButton(
                            onClick = { uriHandler.openUri(url) },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = url,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }

                    if (doc.beans.isNotEmpty()) {
                        SectionHeader(title = "Beans")
                        doc.beans.forEach { bean ->
                            Text(
                                text = "• ${bean.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (doc.equipment.isNotEmpty()) {
                        SectionHeader(title = "Equipment")
                        doc.equipment.forEach { equip ->
                            Text(
                                text = "• ${equip.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                            )
                        }
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
