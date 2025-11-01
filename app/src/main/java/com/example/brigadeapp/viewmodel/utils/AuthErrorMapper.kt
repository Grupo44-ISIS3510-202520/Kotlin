package com.example.brigadeapp.viewmodel.utils

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthErrorMapper @Inject constructor() {

    /** Maps any throwable to a user-friendly message (English). */
    fun userFriendly(t: Throwable, emailForHints: String? = null): String = when (t) {
        is UnknownHostException,
        is FirebaseNetworkException ->
            "Hey Uniandino, you’re offline! Reconnect to get all features back."

        is FirebaseAuthUserCollisionException ->
            "Sorry${personal(emailForHints)}, you already have an account. Try resetting your password."

        is FirebaseAuthInvalidUserException ->
            "We couldn't find an account with that email."

        is FirebaseAuthInvalidCredentialsException -> {
            val code = (t as? FirebaseAuthException)?.errorCode ?: ""
            if (code.contains("wrong-password", true)) "Wrong email or password"
            else "Wrong email or password"
        }

        is FirebaseAuthException -> when {
            t.errorCode.equals("too-many-requests", true) ->
                "Too many attempts. Please try again later."
            else -> t.message ?: "An authentication error occurred"
        }

        else -> t.message ?: "Something went wrong. Please try again."
    }

    private fun personal(email: String?) =
        email?.substringBefore("@")?.let { " $it" } ?: ""
}
