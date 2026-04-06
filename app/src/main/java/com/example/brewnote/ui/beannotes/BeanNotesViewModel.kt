package com.example.brewnote.ui.beannotes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewnote.BrewNoteApp
import com.example.brewnote.data.model.Bean
import com.example.brewnote.data.model.BeanNote
import com.example.brewnote.data.model.PaginationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface BeanNotesUiState {
    object Loading : BeanNotesUiState
    data class Success(val items: List<BeanNote>) : BeanNotesUiState
    data class Error(val message: String) : BeanNotesUiState
}

sealed interface BeanNoteDetailUiState {
    object Loading : BeanNoteDetailUiState
    data class Success(val item: BeanNote) : BeanNoteDetailUiState
    data class Error(val message: String) : BeanNoteDetailUiState
}

class BeanNotesViewModel(application: Application) : AndroidViewModel(application) {

    private val convex = (application as BrewNoteApp).convexClient

    private val _beanNotesState = MutableStateFlow<BeanNotesUiState>(BeanNotesUiState.Loading)
    val beanNotesState: StateFlow<BeanNotesUiState> = _beanNotesState.asStateFlow()

    private val _detailState = MutableStateFlow<BeanNoteDetailUiState>(BeanNoteDetailUiState.Loading)
    val detailState: StateFlow<BeanNoteDetailUiState> = _detailState.asStateFlow()

    private val _availableBeans = MutableStateFlow<List<Bean>>(emptyList())
    val availableBeans: StateFlow<List<Bean>> = _availableBeans.asStateFlow()

    // Form fields
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _selectedBeanIds = MutableStateFlow<List<String>>(emptyList())
    val selectedBeanIds: StateFlow<List<String>> = _selectedBeanIds.asStateFlow()

    private val _personalRating = MutableStateFlow<Double?>(null)
    val personalRating: StateFlow<Double?> = _personalRating.asStateFlow()

    private val _tastingNotes = MutableStateFlow("")
    val tastingNotes: StateFlow<String> = _tastingNotes.asStateFlow()

    private val _roastDate = MutableStateFlow("")
    val roastDate: StateFlow<String> = _roastDate.asStateFlow()

    private val _price = MutableStateFlow("")
    val price: StateFlow<String> = _price.asStateFlow()

    private val _currency = MutableStateFlow("")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    init {
        subscribeBeanNotes()
        subscribeBeans()
    }

    private fun subscribeBeanNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<PaginationResult<BeanNote>>(
                name = "beanNotes:getRecentBeanNotes",
                args = mapOf("paginationOpts" to mapOf("numItems" to 50.0, "cursor" to null))
            ).collect { result ->
                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = { _beanNotesState.value = BeanNotesUiState.Success(it.page) },
                        onFailure = { _beanNotesState.value = BeanNotesUiState.Error(it.message ?: "Failed to load bean notes") }
                    )
                }
            }
        }
    }

    private fun subscribeBeans() {
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<PaginationResult<Bean>>(
                name = "beans:getRecentBeans",
                args = mapOf("paginationOpts" to mapOf("numItems" to 50.0, "cursor" to null))
            ).collect { result ->
                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = { _availableBeans.value = it.page },
                        onFailure = { /* silently ignore */ }
                    )
                }
            }
        }
    }

    fun loadDetail(id: String) {
        _detailState.value = BeanNoteDetailUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<BeanNote>(
                name = "beanNotes:getBeanNoteById",
                args = mapOf("id" to id)
            ).collect { result ->
                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = { _detailState.value = BeanNoteDetailUiState.Success(it) },
                        onFailure = { _detailState.value = BeanNoteDetailUiState.Error(it.message ?: "Failed to load bean note") }
                    )
                }
            }
        }
    }

    fun loadForEdit(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            convex.subscribe<BeanNote>(
                name = "beanNotes:getBeanNoteById",
                args = mapOf("id" to id)
            ).collect { result ->
                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = { note ->
                            _title.value = note.title
                            _selectedBeanIds.value = note.beans.map { it._id }
                            _personalRating.value = note.personalRating
                            _tastingNotes.value = note.tastingNotes ?: ""
                            _price.value = note.price?.toString() ?: ""
                            _currency.value = note.currency ?: ""
                        },
                        onFailure = { _formError.value = it.message }
                    )
                }
            }
        }
    }

    fun saveBeanNote(id: String?, onSuccess: () -> Unit) {
        if (_title.value.isBlank()) {
            _formError.value = "Title is required"
            return
        }
        _isSubmitting.value = true
        _formError.value = null
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                convex.mutation<Unit>(
                    name = "beanNotes:upsertBeanNote",
                    args = buildMap {
                        id?.let { put("id", it) }
                        put("title", _title.value)
                        if (_selectedBeanIds.value.isNotEmpty()) put("beanIds", _selectedBeanIds.value)
                        _personalRating.value?.let { put("personalRating", it) }
                        if (_tastingNotes.value.isNotEmpty()) put("tastingNotes", _tastingNotes.value)
                        _price.value.toDoubleOrNull()?.let { put("price", it) }
                        if (_currency.value.isNotEmpty()) put("currency", _currency.value)
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

    fun setTitle(value: String) { _title.value = value }
    fun setPersonalRating(value: Int) { _personalRating.value = value.toDouble() }
    fun setTastingNotes(value: String) { _tastingNotes.value = value }
    fun setPrice(value: String) { _price.value = value }
    fun setCurrency(value: String) { _currency.value = value }
    fun toggleBeanSelection(beanId: String) {
        val current = _selectedBeanIds.value.toMutableList()
        if (current.contains(beanId)) current.remove(beanId) else current.add(beanId)
        _selectedBeanIds.value = current
    }
}
