package com.example.safarlink.presentation.auth.login

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safarlink.common.Resource
import com.example.safarlink.domain.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUser.value = firebaseAuth.currentUser
    }

    init {
        auth.addAuthStateListener(authStateListener)

        // Fix for "Ghost Login": Check if user actually exists on server at startup
        auth.currentUser?.reload()?.addOnFailureListener {
            auth.signOut()
            _currentUser.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnEmailChange -> _state.update { it.copy(email = event.email, error = null) }
            is LoginEvent.OnPasswordChange -> _state.update { it.copy(password = event.password, error = null) }
            is LoginEvent.OnTogglePasswordVisibility -> _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            is LoginEvent.OnLoginClick -> performEmailLogin()
            is LoginEvent.OnGoogleSignInClick -> { /* Handled in UI */ }
            else -> {}
        }
    }

    private fun performEmailLogin() {
        val email = state.value.email
        val password = state.value.password

        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "Please fill all fields") }
            return
        }

        // Start loading explicitly
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            repository.loginWithEmailPassword(email, password).collect { result ->
                handleAuthResult(result)
            }
        }
    }

    fun initiateGoogleLogin(launcher: ActivityResultLauncher<Intent>) {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    fun handleGoogleSignInResult(intent: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken

            if (idToken != null) {
                authWithGoogleFirebase(idToken)
            } else {
                _state.update { it.copy(error = "Google Sign-In failed", isLoading = false) }
            }
        } catch (e: ApiException) {
            _state.update { it.copy(error = "Google Sign-In failed: ${e.message}", isLoading = false) }
        }
    }

    private fun authWithGoogleFirebase(idToken: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            // Pass 'true' here to ENFORCE strict login (no new users allowed)
            repository.signInWithGoogle(idToken, isLoginOnly = true).collect { result ->
                handleAuthResult(result)
            }
        }
    }

    private fun handleAuthResult(result: Resource<*>) {
        when (result) {
            is Resource.Loading -> {
                // If loading finishes (isLoading = false), we ONLY update isLoading.
                // We do NOT set error = null, otherwise we wipe the error message before the user sees it.
                val shouldLoad = result.isLoading
                _state.update { it.copy(isLoading = shouldLoad) }
            }
            is Resource.Success -> {
                // Success: Now it is safe to clear errors
                _state.update { it.copy(isLoading = false, error = null) }
            }
            is Resource.Error -> {
                val rawError = result.message ?: "Unknown error"

                // Friendly Error Messages
                val friendlyError = when {
                    rawError.contains("user-not-found") ||
                            rawError.contains("no user record") ||
                            rawError.contains("Account not found") || // Catch our custom error
                            rawError.contains("INVALID_LOGIN_CREDENTIALS") -> "Account not found. Please Sign Up."

                    rawError.contains("password") -> "Incorrect password."
                    rawError.contains("network") -> "Network error. Check internet connection."
                    else -> rawError
                }

                // Stop loading and set the error
                _state.update { it.copy(isLoading = false, error = friendlyError) }
            }
        }
    }
}