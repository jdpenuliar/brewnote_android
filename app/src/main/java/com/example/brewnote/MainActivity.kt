package com.example.brewnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.clerk.api.Clerk
import com.example.brewnote.navigation.BrewNoteNavGraph
import com.example.brewnote.ui.theme.BrewnoteTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrewnoteTheme {
                val user by Clerk.userFlow.collectAsStateWithLifecycle()
                BrewNoteNavGraph(isAuthenticated = user != null)
            }
        }
    }
}
