package com.example.brewnote.ui.vendors

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewnote.BrewNoteApp
import com.example.brewnote.data.model.PaginationResult
import com.example.brewnote.data.model.Vendor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface VendorsUiState {
    object Loading : VendorsUiState
    data class Success(val items: List<Vendor>) : VendorsUiState
    data class Error(val message: String) : VendorsUiState
}

sealed interface VendorDetailUiState {
    object Loading : VendorDetailUiState
    data class Success(val item: Vendor) : VendorDetailUiState
    data class Error(val message: String) : VendorDetailUiState
}

class VendorsViewModel(application: Application) : AndroidViewModel(application) {

    private val convex = (application as BrewNoteApp).convexClient

    private val _vendorsState = MutableStateFlow<VendorsUiState>(VendorsUiState.Loading)
    val vendorsState: StateFlow<VendorsUiState> = _vendorsState.asStateFlow()

    private val _detailState = MutableStateFlow<VendorDetailUiState>(VendorDetailUiState.Loading)
    val detailState: StateFlow<VendorDetailUiState> = _detailState.asStateFlow()

    // Form fields
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _type = MutableStateFlow("ONLINE")
    val type: StateFlow<String> = _type.asStateFlow()

    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    companion object {
        val TYPE_OPTIONS = listOf("ONLINE", "LOCAL", "USER")
    }

    init {
        subscribeVendors()
    }

    private fun subscribeVendors() {
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<PaginationResult<Vendor>>(
                name = "vendors:getRecentVendors",
                args = mapOf("paginationOpts" to mapOf("numItems" to 50.0, "cursor" to null))
            ).collect { result ->
                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = { _vendorsState.value = VendorsUiState.Success(it.page) },
                        onFailure = { _vendorsState.value = VendorsUiState.Error(it.message ?: "Failed to load vendors") }
                    )
                }
            }
        }
    }

    fun loadDetail(id: String) {
        _detailState.value = VendorDetailUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<Vendor>(
                name = "vendors:getVendorById",
                args = mapOf("id" to id)
            ).collect { result ->
                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = { _detailState.value = VendorDetailUiState.Success(it) },
                        onFailure = { _detailState.value = VendorDetailUiState.Error(it.message ?: "Failed to load vendor") }
                    )
                }
            }
        }
    }

    fun loadForEdit(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<Vendor>(
                name = "vendors:getVendorById",
                args = mapOf("id" to id)
            ).collect { result ->
                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = { vendor ->
                            _name.value = vendor.name
                            _type.value = vendor.type
                            _url.value = vendor.url ?: ""
                        },
                        onFailure = { _formError.value = it.message }
                    )
                }
            }
        }
    }

    fun saveVendor(id: String?, onSuccess: () -> Unit) {
        if (_name.value.isBlank()) {
            _formError.value = "Name is required"
            return
        }
        _isSubmitting.value = true
        _formError.value = null
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                convex.mutation<Unit>(
                    name = "vendors:upsertVendor",
                    args = buildMap {
                        id?.let { put("id", it) }
                        put("name", _name.value)
                        put("type", _type.value)
                        if (_url.value.isNotEmpty()) put("url", _url.value)
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
    fun setType(value: String) { _type.value = value }
    fun setUrl(value: String) { _url.value = value }
}
