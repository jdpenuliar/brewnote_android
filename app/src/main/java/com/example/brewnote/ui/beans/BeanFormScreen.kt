package com.example.brewnote.ui.beans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.brewnote.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BeanFormScreen(
    id: String?,
    onNavigateBack: () -> Unit,
    viewModel: BeansViewModel = viewModel(),
) {
    LaunchedEffect(id) {
        if (id != null) viewModel.loadForEdit(id)
    }

    val name by viewModel.name.collectAsState()
    val countryOfOrigin by viewModel.countryOfOrigin.collectAsState()
    val species by viewModel.species.collectAsState()
    val openFoodFactsId by viewModel.openFoodFactsId.collectAsState()
    val formError by viewModel.formError.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id != null) "Edit Bean" else "New Bean") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::setName,
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = formError != null && name.isBlank()
            )

            OutlinedTextField(
                value = countryOfOrigin,
                onValueChange = viewModel::setCountryOfOrigin,
                label = { Text("Country of Origin") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            SectionHeader(title = "Species")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BeansViewModel.SPECIES_OPTIONS.forEach { option ->
                    FilterChip(
                        selected = option in species,
                        onClick = { viewModel.toggleSpecies(option) },
                        label = { Text(option) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            OutlinedTextField(
                value = openFoodFactsId,
                onValueChange = viewModel::setOpenFoodFactsId,
                label = { Text("OpenFoodFacts ID (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            formError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = { viewModel.saveBean(id, onNavigateBack) },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isSubmitting) "Saving…" else "Save")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
