package com.example.brewnote.ui.brews

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brewnote.ui.components.RatingRow
import com.example.brewnote.ui.components.SectionHeader
import kotlinx.coroutines.launch

private val QUICK_BREW_METHODS = listOf(
    "Espresso", "V60", "Aeropress", "Chemex", "French Press", "Moka Pot", "Cold Brew"
)

private val ROAST_OPTIONS = listOf("LIGHT", "MEDIUM_LIGHT", "MEDIUM", "MEDIUM_DARK", "DARK")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BrewFormScreen(
    id: String?,
    onNavigateBack: () -> Unit,
    viewModel: BrewsViewModel = viewModel(),
) {
    LaunchedEffect(id) {
        if (id != null) viewModel.loadForEdit(id)
    }

    val brewMethodId by viewModel.brewMethodId.collectAsState()
    val brewMethodName by viewModel.brewMethodName.collectAsState()
    val selectedBeanIds by viewModel.selectedBeanIds.collectAsState()
    val selectedEquipmentIds by viewModel.selectedEquipmentIds.collectAsState()
    val availableBeans by viewModel.availableBeans.collectAsState()
    val availableEquipment by viewModel.availableEquipment.collectAsState()
    val availableBrewMethods by viewModel.availableBrewMethods.collectAsState()
    val rating by viewModel.rating.collectAsState()
    val grindSize by viewModel.grindSize.collectAsState()
    val roast by viewModel.roast.collectAsState()
    val beansWeight by viewModel.beansWeight.collectAsState()
    val beansWeightType by viewModel.beansWeightType.collectAsState()
    val brewTemperature by viewModel.brewTemperature.collectAsState()
    val brewTemperatureType by viewModel.brewTemperatureType.collectAsState()
    val brewTime by viewModel.brewTime.collectAsState()
    val waterToGrindRatio by viewModel.waterToGrindRatio.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val formError by viewModel.formError.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    var methodSearchQuery by remember { mutableStateOf("") }
    var beanSearchQuery by remember { mutableStateOf("") }
    var equipmentSearchQuery by remember { mutableStateOf("") }
    var methodDropdownExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val filteredMethods = availableBrewMethods.filter {
        methodSearchQuery.isNotEmpty() && it.name.contains(methodSearchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id != null) "Edit Brew" else "New Brew") },
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

            // Brew Method Section
            SectionHeader(title = "Brew Method")

            // Quick-select pills
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QUICK_BREW_METHODS.forEach { method ->
                    FilterChip(
                        selected = brewMethodName == method,
                        onClick = {
                            if (brewMethodName == method) {
                                viewModel.setBrewMethodName("")
                                viewModel.setBrewMethodId(null)
                            } else {
                                viewModel.setBrewMethodName(method)
                                scope.launch {
                                    val newId = viewModel.upsertAndGetBrewMethodId(method)
                                    viewModel.setBrewMethodId(newId)
                                }
                            }
                        },
                        label = { Text(method) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Method search
            ExposedDropdownMenuBox(
                expanded = methodDropdownExpanded && filteredMethods.isNotEmpty(),
                onExpandedChange = { methodDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = methodSearchQuery,
                    onValueChange = {
                        methodSearchQuery = it
                        methodDropdownExpanded = true
                    },
                    label = { Text("Search brew method") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = methodDropdownExpanded && filteredMethods.isNotEmpty(),
                    onDismissRequest = { methodDropdownExpanded = false }
                ) {
                    filteredMethods.forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method.name) },
                            onClick = {
                                viewModel.setBrewMethodName(method.name)
                                viewModel.setBrewMethodId(method._id)
                                methodSearchQuery = ""
                                methodDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Selected method chip
            if (brewMethodId != null || brewMethodName.isNotEmpty()) {
                FilterChip(
                    selected = true,
                    onClick = {},
                    label = { Text(brewMethodName.ifEmpty { brewMethodId ?: "" }) },
                    trailingIcon = {
                        IconButton(onClick = {
                            viewModel.setBrewMethodId(null)
                            viewModel.setBrewMethodName("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear method", modifier = Modifier.padding(4.dp))
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            // Beans Section
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

            // Equipment Section
            SectionHeader(title = "Equipment")
            OutlinedTextField(
                value = equipmentSearchQuery,
                onValueChange = { equipmentSearchQuery = it },
                label = { Text("Search equipment") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            val filteredEquipment = if (equipmentSearchQuery.isEmpty()) availableEquipment
            else availableEquipment.filter { it.name.contains(equipmentSearchQuery, ignoreCase = true) }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredEquipment.forEach { equip ->
                    FilterChip(
                        selected = equip._id in selectedEquipmentIds,
                        onClick = { viewModel.toggleEquipmentSelection(equip._id) },
                        label = { Text(equip.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Rating
            SectionHeader(title = "Rating")
            RatingRow(
                rating = rating,
                maxRating = 5,
                onRatingSelected = viewModel::setRating
            )

            // Grind Size
            SectionHeader(title = "Grind Size")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Fine", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = (grindSize ?: 10).toFloat(),
                    onValueChange = { viewModel.setGrindSize(it.toInt()) },
                    valueRange = 1f..20f,
                    steps = 18,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                Text("Coarse", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (grindSize != null) "Size: $grindSize" else "Not set",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (grindSize != null) {
                    androidx.compose.material3.TextButton(onClick = { viewModel.setGrindSize(null) }) {
                        Text("Clear")
                    }
                }
            }

            // Roast
            SectionHeader(title = "Roast")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ROAST_OPTIONS.forEach { option ->
                    FilterChip(
                        selected = roast == option,
                        onClick = { viewModel.setRoast(if (roast == option) null else option) },
                        label = { Text(option.replace("_", " ")) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Dose
            SectionHeader(title = "Dose")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = beansWeight,
                    onValueChange = viewModel::setBeansWeight,
                    label = { Text("Amount") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                FilterChip(
                    selected = beansWeightType == "GRAMS",
                    onClick = { viewModel.setBeansWeightType("GRAMS") },
                    label = { Text("g") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                FilterChip(
                    selected = beansWeightType == "OUNCES",
                    onClick = { viewModel.setBeansWeightType("OUNCES") },
                    label = { Text("oz") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            // Temperature
            SectionHeader(title = "Temperature")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = brewTemperature,
                    onValueChange = viewModel::setBrewTemperature,
                    label = { Text("Temperature") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                FilterChip(
                    selected = brewTemperatureType == "CELSIUS",
                    onClick = { viewModel.setBrewTemperatureType("CELSIUS") },
                    label = { Text("°C") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                FilterChip(
                    selected = brewTemperatureType == "FAHRENHEIT",
                    onClick = { viewModel.setBrewTemperatureType("FAHRENHEIT") },
                    label = { Text("°F") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            // Brew Time
            OutlinedTextField(
                value = brewTime,
                onValueChange = viewModel::setBrewTime,
                label = { Text("Brew Time (seconds)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Ratio
            OutlinedTextField(
                value = waterToGrindRatio,
                onValueChange = viewModel::setWaterToGrindRatio,
                label = { Text("Water to Grind Ratio (e.g. 15:1)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = viewModel::setNotes,
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                maxLines = 6
            )

            formError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = { viewModel.saveBrewNote(id, onNavigateBack) },
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
