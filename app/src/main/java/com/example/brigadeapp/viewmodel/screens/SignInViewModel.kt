package com.example.brigadeapp.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.core.AuthClient
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
)

sealed interface SignInEvent {
    data class EditEmail(val v: String) : SignInEvent
    data class EditPassword(val v: String) : SignInEvent
    data object SubmitLogin : SignInEvent
    data object SubmitRegister : SignInEvent
    data object ClearGeneralError : SignInEvent
}

class SignInViewModel(private val auth: AuthClient) : ViewModel() {

    private val _state = MutableStateFlow(SignInUiState())
    val state: StateFlow<SignInUiState> = _state.asStateFlow()

    fun onEvent(e: SignInEvent) {
        when (e) {
            is SignInEvent.EditEmail -> _state.update {
                it.copy(email = e.v, emailError = null, generalError = null)
            }
            is SignInEvent.EditPassword -> _state.update {
                it.copy(password = e.v, passwordError = null, generalError = null)
            }
            SignInEvent.SubmitLogin -> submit(isRegister = false)
            SignInEvent.SubmitRegister -> submit(isRegister = true)
            SignInEvent.ClearGeneralError -> _state.update { it.copy(generalError = null) }
        }
    }

    private fun validate(): Boolean {
        var ok = true
        val s = _state.value

        // email
        val emailErr = when {
            s.email.isBlank() -> "Please write your email"
            !Patterns.EMAIL_ADDRESS.matcher(s.email.trim()).matches() -> "The email format is not valid"
            else -> null
        }

        // Contrasenia: minimo 6 y al menos 1 caracter no alfanumerico
        val hasSpecial = Regex("[^A-Za-z0-9]").containsMatchIn(s.password)
        val passErr = when {
            s.password.isBlank() -> "Please write your password"
            s.password.length < 6 -> "The password must contain at least 6 characters"
            !hasSpecial -> "Your password should include at least 1 special character (for example: ! @ # \$ %)"
            else -> null
        }

        if (emailErr != null || passErr != null) ok = false

        _state.update { it.copy(emailError = emailErr, passwordError = passErr) }
        return ok
    }

    private fun submit(isRegister: Boolean) = viewModelScope.launch {
        if (!validate()) return@launch

        _state.update { it.copy(isLoading = true, generalError = null) }
        val s = _state.value
        val res = if (isRegister)
            auth.registerWithEmail(s.email.trim(), s.password)
        else
            auth.signInWithEmail(s.email.trim(), s.password)

        _state.update {
            it.copy(
                isLoading = false,
                generalError = res.exceptionOrNull()?.let(::userFriendly)
            )
        }
    }


    private fun userFriendly(t: Throwable): String {
        return when (t) {
            is FirebaseAuthInvalidUserException ->
                "We couldn't find an account with that email"
            is FirebaseAuthUserCollisionException ->
                "There is already an account with that email"
            is FirebaseAuthInvalidCredentialsException ->
                "Wrong email or password"
            else -> t.message ?: "An error has happened: try again"
        }
    }
}
