package com.example.brewnote

import android.app.Application
import com.clerk.api.Clerk
import com.example.brewnote.data.ClerkConvexAuthProvider
import dev.convex.android.ConvexClientWithAuth

class BrewNoteApp : Application() {

    lateinit var convexClient: ConvexClientWithAuth<String>
        private set

    override fun onCreate() {
        super.onCreate()
        Clerk.initialize(
            context = this,
            publishableKey = BuildConfig.CLERK_PUBLISHABLE_KEY
        )

        val authProvider = ClerkConvexAuthProvider()
        convexClient = ConvexClientWithAuth(
            deploymentUrl = BuildConfig.CONVEX_URL,
            authProvider = authProvider
        )
        authProvider.bind(convexClient, applicationContext)
    }
}
