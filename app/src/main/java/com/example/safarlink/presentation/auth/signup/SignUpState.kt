package com.example.safarlink.presentation.auth.signup

data class SignUpState(
    val name: String = "", // Make sure this line exists
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)