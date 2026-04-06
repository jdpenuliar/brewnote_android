package com.example.brewnote.ui.brews

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewnote.BrewNoteApp
import com.example.brewnote.data.model.Bean
import com.example.brewnote.data.model.BrewMethod
import com.example.brewnote.data.model.BrewNote
import com.example.brewnote.data.model.Equipment
import com.example.brewnote.data.model.PaginationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed interface BrewsUiState {
    object Loading : BrewsUiState
    data class Success(val items: List<BrewNote>) : BrewsUiState
    data class Error(val message: String) : BrewsUiState
}

sealed interface BrewDetailUiState {
    object Loading : BrewDetailUiState
    data class Success(val item: BrewNote) : BrewDetailUiState
    data class Error(val message: String) : BrewDetailUiState
}

class BrewsViewModel(application: Application) : AndroidViewModel(application) {

    private val convex = (application as BrewNoteApp).convexClient

    private val _brewsState = MutableStateFlow<BrewsUiState>(BrewsUiState.Loading)
    val brewsState: StateFlow<BrewsUiState> = _brewsState.asStateFlow()

    private val _detailState = MutableStateFlow<BrewDetailUiState>(BrewDetailUiState.Loading)
    val detailState: StateFlow<BrewDetailUiState> = _detailState.asStateFlow()

    private val _availableBeans = MutableStateFlow<List<Bean>>(emptyList())
    val availableBeans: StateFlow<List<Bean>> = _availableBeans.asStateFlow()

    private val _availableEquipment = MutableStateFlow<List<Equipment>>(emptyList())
    val availableEquipment: StateFlow<List<Equipment>> = _availableEquipment.asStateFlow()

    private val _availableBrewMethods = MutableStateFlow<List<BrewMethod>>(emptyList())
    val availableBrewMethods: StateFlow<List<BrewMethod>> = _availableBrewMethods.asStateFlow()

    // Form fields
    private val _brewMethodId = MutableStateFlow<String?>(null)
    val brewMethodId: StateFlow<String?> = _brewMethodId.asStateFlow()

    private val _brewMethodName = MutableStateFlow("")
    val brewMethodName: StateFlow<String> = _brewMethodName.asStateFlow()

    private val _selectedBeanIds = MutableStateFlow<List<String>>(emptyList())
    val selectedBeanIds: StateFlow<List<String>> = _selectedBeanIds.asStateFlow()

    private val _selectedEquipmentIds = MutableStateFlow<List<String>>(emptyList())
    val selectedEquipmentIds: StateFlow<List<String>> = _selectedEquipmentIds.asStateFlow()

    private val _rating = MutableStateFlow<Double?>(null)
    val rating: StateFlow<Double?> = _rating.asStateFlow()

    private val _grindSize = MutableStateFlow<Double?>(null)
    val grindSize: StateFlow<Double?> = _grindSize.asStateFlow()

    private val _roast = MutableStateFlow<String?>(null)
    val roast: StateFlow<String?> = _roast.asStateFlow()

    private val _beansWeight = MutableStateFlow("")
    val beansWeight: StateFlow<String> = _beansWeight.asStateFlow()

    private val _beansWeightType = MutableStateFlow("GRAMS")
    val beansWeightType: StateFlow<String> = _beansWeightType.asStateFlow()

    private val _brewTemperature = MutableStateFlow("")
    val brewTemperature: StateFlow<String> = _brewTemperature.asStateFlow()

    private val _brewTemperatureType = MutableStateFlow("CELSIUS")
    val brewTemperatureType: StateFlow<String> = _brewTemperatureType.asStateFlow()

    private val _brewTime = MutableStateFlow("")
    val brewTime: StateFlow<String> = _brewTime.asStateFlow()

    private val _waterToGrindRatio = MutableStateFlow("")
    val waterToGrindRatio: StateFlow<String> = _waterToGrindRatio.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    init {
        subscribeBrews()
        subscribeBeans()
        subscribeEquipment()
        subscribeBrewMethods()
    }

    private fun subscribeBrews() {
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<PaginationResult<BrewNote>>(
                name = "brewNotes:getRecentBrewNotes",
                args = mapOf("paginationOpts" to mapOf("numItems" to 50.0, "cursor" to null))
            ).collect { result ->
                result.fold(
                    onSuccess = { _brewsState.value = BrewsUiState.Success(it.page) },
                    onFailure = { _brewsState.value = BrewsUiState.Error(it.message ?: "Failed to load brews") }
                )
            }
        }
    }

    private fun subscribeBeans() {
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<PaginationResult<Bean>>(
                name = "beans:getRecentBeans",
                args = mapOf("paginationOpts" to mapOf("numItems" to 50.0, "cursor" to null))
            ).collect { result ->
                result.fold(
                    onSuccess = { _availableBeans.value = it.page },
                    onFailure = { /* silently ignore */ }
                )
            }
        }
    }

    private fun subscribeEquipment() {
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<PaginationResult<Equipment>>(
                name = "equipments:getRecentEquipments",
                args = mapOf("paginationOpts" to mapOf("numItems" to 50.0, "cursor" to null))
            ).collect { result ->
                result.fold(
                    onSuccess = { _availableEquipment.value = it.page },
                    onFailure = { /* silently ignore */ }
                )
            }
        }
    }

    private fun subscribeBrewMethods() {
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<PaginationResult<BrewMethod>>(
                name = "brewMethods:getRecentBrewMethods",
                args = mapOf("paginationOpts" to mapOf("numItems" to 50.0, "cursor" to null))
            ).collect { result ->
                result.fold(
                    onSuccess = { _availableBrewMethods.value = it.page },
                    onFailure = { /* silently ignore */ }
                )
            }
        }
    }

    fun loadDetail(id: String) {
        _detailState.value = BrewDetailUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<BrewNote>(
                name = "brewNotes:getBrewNoteById",
                args = mapOf("id" to id)
            ).collect { result ->
                result.fold(
                    onSuccess = { _detailState.value = BrewDetailUiState.Success(it) },
                    onFailure = { _detailState.value = BrewDetailUiState.Error(it.message ?: "Failed to load brew") }
                )
            }
        }
    }

    fun loadForEdit(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<BrewNote>(
                name = "brewNotes:getBrewNoteById",
                args = mapOf("id" to id)
            ).collect { result ->
                result.fold(
                    onSuccess = { brew ->
                        _brewMethodId.value = brew.brewMethodId
                        _brewMethodName.value = brew.brewMethodDoc?.name ?: ""
                        _selectedBeanIds.value = brew.beans.map { it._id }
                        _selectedEquipmentIds.value = brew.equipment.map { it._id }
                        _rating.value = brew.rating
                        _grindSize.value = brew.grindSize
                        _roast.value = brew.roast
                        _beansWeight.value = brew.beansWeight?.toString() ?: ""
                        _beansWeightType.value = brew.beansWeightType ?: "GRAMS"
                        _brewTemperature.value = brew.brewTemperature?.toString() ?: ""
                        _brewTemperatureType.value = brew.brewTemperatureType ?: "CELSIUS"
                        _brewTime.value = brew.brewTime?.toString() ?: ""
                        _waterToGrindRatio.value = brew.waterToGrindRatio ?: ""
                        _notes.value = brew.notes ?: ""
                    },
                    onFailure = { _formError.value = it.message }
                )
            }
        }
    }

    fun saveBrewNote(id: String?, onSuccess: () -> Unit) {
        _isSubmitting.value = true
        _formError.value = null
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                convex.mutation<Unit>(
                    name = "brewNotes:upsertBrewNote",
                    args = buildMap {
                        id?.let { put("id", it) }
                        _brewMethodId.value?.let { put("brewMethodId", it) }
                        if (_selectedBeanIds.value.isNotEmpty()) put("beanIds", _selectedBeanIds.value)
                        if (_selectedEquipmentIds.value.isNotEmpty()) put("equipmentIds", _selectedEquipmentIds.value)
                        _rating.value?.let { put("rating", it) }
                        _grindSize.value?.let { put("grindSize", it) }
                        _roast.value?.let { put("roast", it) }
                        _beansWeight.value.toDoubleOrNull()?.let { put("beansWeight", it) }
                        if (_beansWeightType.value.isNotEmpty()) put("beansWeightType", _beansWeightType.value)
                        _brewTemperature.value.toDoubleOrNull()?.let { put("brewTemperature", it) }
                        if (_brewTemperatureType.value.isNotEmpty()) put("brewTemperatureType", _brewTemperatureType.value)
                        _brewTime.value.toIntOrNull()?.let { put("brewTime", it) }
                        if (_waterToGrindRatio.value.isNotEmpty()) put("waterToGrindRatio", _waterToGrindRatio.value)
                        if (_notes.value.isNotEmpty()) put("notes", _notes.value)
                    }
                )
            }.onSuccess {
                _isSubmitting.value = false
                onSuccess()
            }.onFailure {
                _isSubmitting.value = false
                _formError.value = it.message
            }
        }
    }

    suspend fun upsertAndGetBrewMethodId(name: String): String? {
        return runCatching {
            convex.mutation<String>(
                name = "brewMethods:upsertBrewMethod",
                args = mapOf("name" to name)
            )
        }.getOrNull()
    }

    // Setters
    fun setBrewMethodId(id: String?) { _brewMethodId.value = id }
    fun setBrewMethodName(name: String) { _brewMethodName.value = name }
    fun setRating(r: Int) { _rating.value = r.toDouble() }
    fun setGrindSize(g: Int?) { _grindSize.value = g?.toDouble() }
    fun setRoast(r: String?) { _roast.value = r }
    fun setBeansWeight(w: String) { _beansWeight.value = w }
    fun setBeansWeightType(t: String) { _beansWeightType.value = t }
    fun setBrewTemperature(t: String) { _brewTemperature.value = t }
    fun setBrewTemperatureType(t: String) { _brewTemperatureType.value = t }
    fun setBrewTime(t: String) { _brewTime.value = t }
    fun setWaterToGrindRatio(r: String) { _waterToGrindRatio.value = r }
    fun setNotes(n: String) { _notes.value = n }

    fun toggleBeanSelection(beanId: String) {
        val current = _selectedBeanIds.value.toMutableList()
        if (current.contains(beanId)) current.remove(beanId) else current.add(beanId)
        _selectedBeanIds.value = current
    }

    fun toggleEquipmentSelection(equipmentId: String) {
        val current = _selectedEquipmentIds.value.toMutableList()
        if (current.contains(equipmentId)) current.remove(equipmentId) else current.add(equipmentId)
        _selectedEquipmentIds.value = current
    }
}
