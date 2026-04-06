package com.example.brewnote.ui.brewmethods

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewnote.BrewNoteApp
import com.example.brewnote.data.model.BrewMethod
import com.example.brewnote.data.model.PaginationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed interface BrewMethodsUiState {
    object Loading : BrewMethodsUiState
    data class Success(val items: List<BrewMethod>) : BrewMethodsUiState
    data class Error(val message: String) : BrewMethodsUiState
}

sealed interface BrewMethodDetailUiState {
    object Loading : BrewMethodDetailUiState
    data class Success(val item: BrewMethod) : BrewMethodDetailUiState
    data class Error(val message: String) : BrewMethodDetailUiState
}

class BrewMethodsViewModel(application: Application) : AndroidViewModel(application) {

    private val convex = (application as BrewNoteApp).convexClient

    private val _brewMethodsState = MutableStateFlow<BrewMethodsUiState>(BrewMethodsUiState.Loading)
    val brewMethodsState: StateFlow<BrewMethodsUiState> = _brewMethodsState.asStateFlow()

    private val _detailState = MutableStateFlow<BrewMethodDetailUiState>(BrewMethodDetailUiState.Loading)
    val detailState: StateFlow<BrewMethodDetailUiState> = _detailState.asStateFlow()

    // Form fields
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    init {
        subscribeBrewMethods()
    }

    private fun subscribeBrewMethods() {
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<PaginationResult<BrewMethod>>(
                name = "brewMethods:getRecentBrewMethods",
                args = mapOf("paginationOpts" to mapOf("numItems" to 50.0, "cursor" to null))
            ).collect { result ->
                result.fold(
                    onSuccess = { _brewMethodsState.value = BrewMethodsUiState.Success(it.page) },
                    onFailure = { _brewMethodsState.value = BrewMethodsUiState.Error(it.message ?: "Failed to load brew methods") }
                )
            }
        }
    }

    fun loadDetail(id: String) {
        _detailState.value = BrewMethodDetailUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<BrewMethod>(
                name = "brewMethods:getBrewMethodById",
                args = mapOf("id" to id)
            ).collect { result ->
                result.fold(
                    onSuccess = { _detailState.value = BrewMethodDetailUiState.Success(it) },
                    onFailure = { _detailState.value = BrewMethodDetailUiState.Error(it.message ?: "Failed to load brew method") }
                )
            }
        }
    }

    fun loadForEdit(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<BrewMethod>(
                name = "brewMethods:getBrewMethodById",
                args = mapOf("id" to id)
            ).collect { result ->
                result.fold(
                    onSuccess = { method ->
                        _name.value = method.name
                        _description.value = method.description ?: ""
                    },
                    onFailure = { _formError.value = it.message }
                )
            }
        }
    }

    fun saveBrewMethod(id: String?, onSuccess: () -> Unit) {
        if (_name.value.isBlank()) {
            _formError.value = "Name is required"
            return
        }
        _isSubmitting.value = true
        _formError.value = null
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                convex.mutation<Unit>(
                    name = "brewMethods:upsertBrewMethod",
                    args = buildMap {
                        id?.let { put("id", it) }
                        put("name", _name.value)
                        if (_description.value.isNotEmpty()) put("description", _description.value)
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

    fun setName(value: String) { _name.value = value }
    fun setDescription(value: String) { _description.value = value }
}
