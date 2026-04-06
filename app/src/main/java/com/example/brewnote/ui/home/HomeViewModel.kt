package com.example.brewnote.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewnote.BrewNoteApp
import com.example.brewnote.data.model.Bean
import com.example.brewnote.data.model.BrewNote
import com.example.brewnote.data.model.HomeStats
import com.example.brewnote.data.model.PaginationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val convex = (application as BrewNoteApp).convexClient

    private val _homeStats = MutableStateFlow<HomeStats?>(null)
    val homeStats: StateFlow<HomeStats?> = _homeStats.asStateFlow()

    private val _recentBrews = MutableStateFlow<List<BrewNote>>(emptyList())
    val recentBrews: StateFlow<List<BrewNote>> = _recentBrews.asStateFlow()

    private val _recentBeans = MutableStateFlow<List<Bean>>(emptyList())
    val recentBeans: StateFlow<List<Bean>> = _recentBeans.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        subscribeHomeStats()
        subscribeRecentBrews()
        subscribeRecentBeans()
    }

    private fun subscribeHomeStats() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("HomeViewModel", "homeStats: starting collect")
            try {
                convex.subscribe<HomeStats>(
                    name = "home:getHomeStats",
                    args = emptyMap()
                ).collect { result ->
                    Log.d("HomeViewModel", "homeStats: in collect")
                    result.fold(
                        onSuccess = {
                            Log.d("HomeViewModel", "homeStats success: $it")
                            _homeStats.value = it
                        },
                        onFailure = { e ->
                            Log.e("HomeViewModel", "homeStats error", e)
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "homeStats: exception", e)
            }
        }
    }

    private fun subscribeRecentBrews() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("HomeViewModel", "recentBrews: starting collect")
            try {
                convex.subscribe<PaginationResult<BrewNote>>(
                    name = "brewNotes:getRecentBrewNotes",
                    args = mapOf("paginationOpts" to mapOf("numItems" to 5.0, "cursor" to null))
                ).collect { result ->
                    result.fold(
                        onSuccess = {
                            Log.d("HomeViewModel", "recentBrews success: ${it.page.size} items")
                            _recentBrews.value = it.page
                            _isLoading.value = false
                        },
                        onFailure = { e ->
                            Log.e("HomeViewModel", "recentBrews error", e)
                            _isLoading.value = false
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "recentBrews: exception", e)
            }
        }
    }

    private fun subscribeRecentBeans() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("HomeViewModel", "recentBeans: starting collect")
            try {
                convex.subscribe<PaginationResult<Bean>>(
                    name = "beans:getRecentBeans",
                    args = mapOf("paginationOpts" to mapOf("numItems" to 5.0, "cursor" to null))
                ).collect { result ->
                    result.fold(
                        onSuccess = {
                            Log.d("HomeViewModel", "recentBeans success: ${it.page.size} items")
                            _recentBeans.value = it.page
                        },
                        onFailure = { e ->
                            Log.e("HomeViewModel", "recentBeans error", e)
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "recentBeans: exception", e)
            }
        }
    }
}
