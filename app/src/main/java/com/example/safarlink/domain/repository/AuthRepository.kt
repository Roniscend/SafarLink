package com.example.safarlink.domain.repository

import com.example.safarlink.common.Resource
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun loginWithEmailPassword(email: String, pass: String): Flow<Resource<AuthResult>>

    // UPDATED: Added 'isLoginOnly' boolean
    fun signInWithGoogle(idToken: String, isLoginOnly: Boolean): Flow<Resource<AuthResult>>

    fun registerWithEmailPassword(email: String, pass: String): Flow<Resource<AuthResult>>
}