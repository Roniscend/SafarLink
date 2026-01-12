package com.example.safarlink.presentation.auth.signup

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safarlink.common.Resource
import com.example.safarlink.domain.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state = _state.asStateFlow()

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.OnNameChange -> _state.update { it.copy(name = event.name, error = null) }
            is SignUpEvent.OnEmailChange -> _state.update { it.copy(email = event.email, error = null) }
            is SignUpEvent.OnPasswordChange -> _state.update { it.copy(password = event.password, error = null) }
            is SignUpEvent.OnConfirmPasswordChange -> _state.update { it.copy(confirmPassword = event.confirmPassword, error = null) }
            is SignUpEvent.OnSignUpClick -> performSignUp()
            is SignUpEvent.OnGoogleSignInClick -> {}
        }
    }

    private fun performSignUp() {
        val name = state.value.name
        val email = state.value.email
        val password = state.value.password
        val confirmPass = state.value.confirmPassword

        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPass.isBlank()) {
            _state.update { it.copy(error = "Please fill all fields") }
            return
        }

        if (password != confirmPass) {
            _state.update { it.copy(error = "Passwords do not match") }
            return
        }

        if (password.length < 6) {
            _state.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            repository.registerWithEmailPassword(email, password).collect { result ->
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
            repository.signInWithGoogle(idToken, isLoginOnly = false).collect { result ->
                handleAuthResult(result)
            }
        }
    }

    private fun handleAuthResult(result: Resource<*>) {
        when (result) {
            is Resource.Loading -> {
                val shouldLoad = result.isLoading
                _state.update { it.copy(isLoading = shouldLoad) }
            }
            is Resource.Success -> {
                _state.update { it.copy(isLoading = false, isSuccess = true, error = null) }
            }
            is Resource.Error -> {
                _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}