package com.example.safarlink.presentation.auth.signup

sealed interface SignUpEvent {
    data class OnNameChange(val name: String) : SignUpEvent

    data class OnEmailChange(val email: String) : SignUpEvent
    data class OnPasswordChange(val password: String) : SignUpEvent
    data class OnConfirmPasswordChange(val confirmPassword: String) : SignUpEvent
    object OnSignUpClick : SignUpEvent
    object OnGoogleSignInClick : SignUpEvent
}