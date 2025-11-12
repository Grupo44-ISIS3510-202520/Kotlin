package com.example.brigadeapp.viewmodel.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.usecase.GetCurrentUserUseCase
import com.example.brigadeapp.domain.usecase.ObserveAuthStateUseCase
import com.example.brigadeapp.domain.usecase.SignInWithEmail
import com.example.brigadeapp.viewmodel.utils.AuthErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    data object ClearGeneralError : SignInEvent
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInWithEmail: SignInWithEmail,
    private val observeAuth: ObserveAuthStateUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val errorMapper: AuthErrorMapper
) : ViewModel() {

    private val _state = MutableStateFlow(SignInUiState())
    val state: StateFlow<SignInUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeAuth().collect {
            }
        }
    }

    fun onEvent(e: SignInEvent) {
        when (e) {
            is SignInEvent.EditEmail ->
                _state.update { it.copy(email = e.v, emailError = null, generalError = null) }
            is SignInEvent.EditPassword ->
                _state.update { it.copy(password = e.v, passwordError = null, generalError = null) }
            SignInEvent.SubmitLogin -> submit()
            SignInEvent.ClearGeneralError ->
                _state.update { it.copy(generalError = null) }
        }
    }

    private fun validate(): Boolean {
        var ok = true
        val s = _state.value

        val emailErr = when {
            s.email.isBlank() -> "Please enter your email."
            !android.util.Patterns.EMAIL_ADDRESS.matcher(s.email.trim()).matches() ->
                "Invalid email format."
            else -> null
        }

        // Min 6 characters and at least 1 non-alphanumeric
        val hasSpecial = Regex("[^A-Za-z0-9]").containsMatchIn(s.password)
        val passErr = when {
            s.password.isBlank() -> "Please enter your password."
            s.password.length < 6 -> "Password must contain at least 6 characters."
            !hasSpecial -> "Password must include at least one special character (e.g., ! @ # \$ %)."
            else -> null
        }

        if (emailErr != null || passErr != null) ok = false
        _state.update { it.copy(emailError = emailErr, passwordError = passErr) }
        return ok
    }

    private fun submit() = viewModelScope.launch {
        if (!validate()) return@launch
        val s = _state.value

        _state.update { it.copy(isLoading = true, generalError = null) }
        try {
            signInWithEmail(email = s.email.trim(), password = s.password)
        } catch (t: Throwable) {
            _state.update { it.copy(generalError = errorMapper.userFriendly(t)) }
        } finally {
            _state.update { it.copy(isLoading = false) }
        }
    }
}
