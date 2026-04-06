package com.example.brewnote.ui.beans

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewnote.BrewNoteApp
import com.example.brewnote.data.model.Bean
import com.example.brewnote.data.model.PaginationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface BeansUiState {
    object Loading : BeansUiState
    data class Success(val items: List<Bean>) : BeansUiState
    data class Error(val message: String) : BeansUiState
}

sealed interface BeanDetailUiState {
    object Loading : BeanDetailUiState
    data class Success(val item: Bean) : BeanDetailUiState
    data class Error(val message: String) : BeanDetailUiState
}

class BeansViewModel(application: Application) : AndroidViewModel(application) {

    private val convex = (application as BrewNoteApp).convexClient

    private val _beansState = MutableStateFlow<BeansUiState>(BeansUiState.Loading)
    val beansState: StateFlow<BeansUiState> = _beansState.asStateFlow()

    private val _detailState = MutableStateFlow<BeanDetailUiState>(BeanDetailUiState.Loading)
    val detailState: StateFlow<BeanDetailUiState> = _detailState.asStateFlow()

    // Form fields
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _countryOfOrigin = MutableStateFlow("")
    val countryOfOrigin: StateFlow<String> = _countryOfOrigin.asStateFlow()

    private val _species = MutableStateFlow<List<String>>(emptyList())
    val species: StateFlow<List<String>> = _species.asStateFlow()

    private val _openFoodFactsId = MutableStateFlow("")
    val openFoodFactsId: StateFlow<String> = _openFoodFactsId.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    companion object {
        val SPECIES_OPTIONS = listOf("ARABICA", "ROBUSTA", "LIBERICA", "EXCELSA")
    }

    init {
        subscribeBeans()
    }

    private fun subscribeBeans() {
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<PaginationResult<Bean>>(
                name = "beans:getRecentBeans",
                args = mapOf("paginationOpts" to mapOf("numItems" to 50.0, "cursor" to null))
            ).collect { result ->
                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = { _beansState.value = BeansUiState.Success(it.page) },
                        onFailure = { _beansState.value = BeansUiState.Error(it.message ?: "Failed to load beans") }
                    )
                }
            }
        }
    }

    fun loadDetail(id: String) {
        _detailState.value = BeanDetailUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<Bean>(
                name = "beans:getBeanById",
                args = mapOf("id" to id)
            ).collect { result ->
                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = { _detailState.value = BeanDetailUiState.Success(it) },
                        onFailure = { _detailState.value = BeanDetailUiState.Error(it.message ?: "Failed to load bean") }
                    )
                }
            }
        }
    }

    fun loadForEdit(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<Bean>(
                name = "beans:getBeanById",
                args = mapOf("id" to id)
            ).collect { result ->
                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = { bean ->
                            _name.value = bean.name
                            _countryOfOrigin.value = bean.countryOfOrigin
                            _species.value = bean.species
                            _openFoodFactsId.value = bean.openFoodFactsId ?: ""
                        },
                        onFailure = { _formError.value = it.message }
                    )
                }
            }
        }
    }

    fun saveBean(id: String?, onSuccess: () -> Unit) {
        if (_name.value.isBlank()) {
            _formError.value = "Name is required"
            return
        }
        _isSubmitting.value = true
        _formError.value = null
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                convex.mutation<Unit>(
                    name = "beans:upsertBeans",
                    args = buildMap {
                        id?.let { put("id", it) }
                        put("name", _name.value)
                        put("countryOfOrigin", _countryOfOrigin.value)
                        if (_species.value.isNotEmpty()) put("species", _species.value)
                        if (_openFoodFactsId.value.isNotEmpty()) put("openFoodFactsId", _openFoodFactsId.value)
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
    fun setCountryOfOrigin(value: String) { _countryOfOrigin.value = value }
    fun toggleSpecies(s: String) {
        val current = _species.value.toMutableList()
        if (current.contains(s)) current.remove(s) else current.add(s)
        _species.value = current
    }
    fun setOpenFoodFactsId(value: String) { _openFoodFactsId.value = value }
}
