package com.example.safarlink.presentation.auth.login

// Simple state holder
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPasswordVisible: Boolean = false
)

// User actions
sealed interface LoginEvent {
    data class OnEmailChange(val email: String) : LoginEvent
    data class OnPasswordChange(val password: String) : LoginEvent
    data object OnTogglePasswordVisibility : LoginEvent
    data object OnLoginClick : LoginEvent
    data object OnGoogleSignInClick : LoginEvent
    data object OnSignUpClick : LoginEvent
}