package com.example.brewnote.ui.beannotes

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brewnote.ui.components.RatingRow
import com.example.brewnote.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BeanNoteFormScreen(
    id: String?,
    onNavigateBack: () -> Unit,
    viewModel: BeanNotesViewModel = viewModel(),
) {
    LaunchedEffect(id) {
        if (id != null) viewModel.loadForEdit(id)
    }

    val title by viewModel.title.collectAsState()
    val selectedBeanIds by viewModel.selectedBeanIds.collectAsState()
    val availableBeans by viewModel.availableBeans.collectAsState()
    val personalRating by viewModel.personalRating.collectAsState()
    val tastingNotes by viewModel.tastingNotes.collectAsState()
    val price by viewModel.price.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val formError by viewModel.formError.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    var beanSearchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id != null) "Edit Bean Note" else "New Bean Note") },
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
                value = title,
                onValueChange = viewModel::setTitle,
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = formError != null && title.isBlank()
            )

            SectionHeader(title = "Beans")
            OutlinedTextField(
                value = beanSearchQuery,
                onValueChange = { beanSearchQuery = it },
                label = { Text("Search beans") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            val filteredBeans = if (beanSearchQuery.isEmpty()) availableBeans
            else availableBeans.filter { it.name.contains(beanSearchQuery, ignoreCase = true) }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredBeans.forEach { bean ->
                    FilterChip(
                        selected = bean._id in selectedBeanIds,
                        onClick = { viewModel.toggleBeanSelection(bean._id) },
                        label = { Text(bean.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            SectionHeader(title = "Rating")
            RatingRow(
                rating = personalRating,
                maxRating = 5,
                onRatingSelected = viewModel::setPersonalRating
            )

            OutlinedTextField(
                value = tastingNotes,
                onValueChange = viewModel::setTastingNotes,
                label = { Text("Tasting Notes") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                maxLines = 6
            )

            OutlinedTextField(
                value = price,
                onValueChange = viewModel::setPrice,
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = currency,
                onValueChange = viewModel::setCurrency,
                label = { Text("Currency (e.g. USD)") },
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
                onClick = { viewModel.saveBeanNote(id, onNavigateBack) },
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
