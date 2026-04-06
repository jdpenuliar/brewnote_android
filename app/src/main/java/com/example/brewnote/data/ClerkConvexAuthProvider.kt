package com.example.brewnote.data

import android.content.Context
import com.clerk.api.Clerk
import com.clerk.api.session.GetTokenOptions
import com.clerk.api.network.serialization.ClerkResult
import com.clerk.api.session.Session
import dev.convex.android.AuthProvider
import dev.convex.android.ConvexClientWithAuth
import java.lang.ref.WeakReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Custom AuthProvider that passes "convex" JWT template to Clerk.
 */
class ClerkConvexAuthProvider : AuthProvider<String> {

    private var client: WeakReference<ConvexClientWithAuth<String>>? = null
    private var onIdToken: ((String?) -> Unit)? = null
    private lateinit var applicationContext: Context
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var sessionSyncJob: Job? = null

    internal fun bind(client: ConvexClientWithAuth<String>, context: Context) {
        this.client = WeakReference(client)
        this.applicationContext = context.applicationContext
        startSessionSync()
    }

    override suspend fun login(context: Context, onIdToken: (String?) -> Unit): Result<String> =
        authenticate(onIdToken)

    override suspend fun loginFromCache(onIdToken: (String?) -> Unit): Result<String> =
        authenticate(onIdToken)

    override suspend fun logout(context: Context): Result<Void?> {
        onIdToken = null
        if (Clerk.activeSession != null) {
            when (val result = Clerk.auth.signOut()) {
                is ClerkResult.Failure -> {
                    val error = result.throwable ?: Exception("Sign out failed")
                    return Result.failure(error)
                }
                is ClerkResult.Success -> Unit
            }
        }
        return Result.success(null)
    }

    override fun extractIdToken(authResult: String): String = authResult

    fun close() {
        scope.cancel()
    }

    private suspend fun authenticate(onIdToken: (String?) -> Unit): Result<String> {
        this.onIdToken = onIdToken
        return fetchToken()
    }

    private suspend fun fetchToken(): Result<String> {
        return when {
            !Clerk.isInitialized.value -> {
                Result.failure(Exception("Clerk not initialized"))
            }
            Clerk.activeSession == null -> {
                Result.failure(Exception("No active session"))
            }
            else ->
                when (val result = Clerk.auth.getToken(GetTokenOptions(template = "convex"))) {
                    is ClerkResult.Success -> {
                        val token = result.value
                        onIdToken?.invoke(token)
                        Result.success(token)
                    }
                    is ClerkResult.Failure -> {
                        val reason = result.throwable?.message ?: "Token retrieval failed"
                        Result.failure(Exception(reason))
                    }
                }
        }
    }

    private fun startSessionSync() {
        sessionSyncJob?.cancel()
        sessionSyncJob =
            scope.launch {
                var previousSession: Session? = null
                Clerk.sessionFlow.collect { newSession ->
                    syncSession(previousSession, newSession)
                    previousSession = newSession
                }
            }
    }

    private suspend fun syncSession(oldSession: Session?, newSession: Session?) {
        val convexClient = client?.get() ?: return

        if (shouldLogin(oldSession, newSession)) {
            convexClient.loginFromCache()
        } else if (shouldLogout(oldSession, newSession)) {
            onIdToken?.invoke(null)
            onIdToken = null
            convexClient.logout(applicationContext)
        }
    }

    private fun shouldLogin(oldSession: Session?, newSession: Session?): Boolean =
        newSession?.status == Session.SessionStatus.ACTIVE &&
                (oldSession?.status != Session.SessionStatus.ACTIVE || oldSession.id != newSession.id)

    private fun shouldLogout(oldSession: Session?, newSession: Session?): Boolean =
        oldSession?.id != null && newSession == null
}
