package com.example.brewnote.ui.equipment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewnote.BrewNoteApp
import com.example.brewnote.data.model.Equipment
import com.example.brewnote.data.model.PaginationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface EquipmentUiState {
    object Loading : EquipmentUiState
    data class Success(val items: List<Equipment>) : EquipmentUiState
    data class Error(val message: String) : EquipmentUiState
}

sealed interface EquipmentDetailUiState {
    object Loading : EquipmentDetailUiState
    data class Success(val item: Equipment) : EquipmentDetailUiState
    data class Error(val message: String) : EquipmentDetailUiState
}

class EquipmentViewModel(application: Application) : AndroidViewModel(application) {

    private val convex = (application as BrewNoteApp).convexClient

    private val _equipmentState = MutableStateFlow<EquipmentUiState>(EquipmentUiState.Loading)
    val equipmentState: StateFlow<EquipmentUiState> = _equipmentState.asStateFlow()

    private val _detailState = MutableStateFlow<EquipmentDetailUiState>(EquipmentDetailUiState.Loading)
    val detailState: StateFlow<EquipmentDetailUiState> = _detailState.asStateFlow()

    // Form fields
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _brand = MutableStateFlow("")
    val brand: StateFlow<String> = _brand.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    init {
        subscribeEquipment()
    }

    private fun subscribeEquipment() {
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<PaginationResult<Equipment>>(
                name = "equipments:getRecentEquipments",
                args = mapOf("paginationOpts" to mapOf("numItems" to 50.0, "cursor" to null))
            ).collect { result ->
                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = { _equipmentState.value = EquipmentUiState.Success(it.page) },
                        onFailure = { _equipmentState.value = EquipmentUiState.Error(it.message ?: "Failed to load equipment") }
                    )
                }
            }
        }
    }

    fun loadDetail(id: String) {
        _detailState.value = EquipmentDetailUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<Equipment>(
                name = "equipments:getEquipmentById",
                args = mapOf("id" to id)
            ).collect { result ->
                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = { _detailState.value = EquipmentDetailUiState.Success(it) },
                        onFailure = { _detailState.value = EquipmentDetailUiState.Error(it.message ?: "Failed to load equipment") }
                    )
                }
            }
        }
    }

    fun loadForEdit(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<Equipment>(
                name = "equipments:getEquipmentById",
                args = mapOf("id" to id)
            ).collect { result ->
                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = { equip ->
                            _name.value = equip.name
                            _brand.value = equip.brand ?: ""
                        },
                        onFailure = { _formError.value = it.message }
                    )
                }
            }
        }
    }

    fun saveEquipment(id: String?, onSuccess: () -> Unit) {
        if (_name.value.isBlank()) {
            _formError.value = "Name is required"
            return
        }
        _isSubmitting.value = true
        _formError.value = null
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                convex.mutation<Unit>(
                    name = "equipments:upsertEquipment",
                    args = buildMap {
                        id?.let { put("id", it) }
                        put("name", _name.value)
                        if (_brand.value.isNotEmpty()) put("brand", _brand.value)
                    }
                )
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    _isSubmitting.value = false
                    onSuccess()
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    _isSubmitting.value = false
                    _formError.value = it.message
                }
            }
        }
    }

    fun setName(value: String) { _name.value = value }
    fun setBrand(value: String) { _brand.value = value }
}
