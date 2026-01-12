package com.example.safarlink.data.repository

import com.example.safarlink.common.Resource
import com.example.safarlink.domain.repository.AuthRepository
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override fun loginWithEmailPassword(email: String, pass: String): Flow<Resource<AuthResult>> = flow {
        emit(Resource.Loading(true))
        try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Login failed"))
        } finally {
            emit(Resource.Loading(false))
        }
    }

    override fun signInWithGoogle(idToken: String, isLoginOnly: Boolean): Flow<Resource<AuthResult>> = flow {
        emit(Resource.Loading(true))
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            if (isLoginOnly) {
                val isNewUser = authResult.additionalUserInfo?.isNewUser == true
                if (isNewUser) {
                    try {
                        auth.currentUser?.delete()?.await()
                    } catch (e: Exception) {
                        auth.signOut()
                    }
                    emit(Resource.Error("Account not found. Please Sign Up first."))
                } else {
                    emit(Resource.Success(authResult))
                }
            } else {
                emit(Resource.Success(authResult))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Google Sign-In failed"))
        } finally {
            emit(Resource.Loading(false))
        }
    }

    override fun registerWithEmailPassword(email: String, pass: String): Flow<Resource<AuthResult>> = flow {
        emit(Resource.Loading(true))
        try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Sign up failed"))
        } finally {
            emit(Resource.Loading(false))
        }
    }
}