package com.example.brigadeapp.viewmodel.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.usecase.RegisterWithEmail
import com.example.brigadeapp.domain.usecase.SendEmailVerification
import com.example.brigadeapp.domain.utils.AuthConstants
import com.example.brigadeapp.domain.utils.validateBloodGroup
import com.example.brigadeapp.domain.utils.validateEmailDomain
import com.example.brigadeapp.domain.utils.validateLastName
import com.example.brigadeapp.domain.utils.validateName
import com.example.brigadeapp.domain.utils.validatePassword
import com.example.brigadeapp.domain.utils.validatePasswordConfirm
import com.example.brigadeapp.domain.utils.validateRole
import com.example.brigadeapp.domain.utils.validateUniandesCode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



data class RegisterUiState(
    val email: String = "",
    val name: String = "",
    val lastName: String = "",
    val code: String = "",
    val bloodGroup: String = AuthConstants.BLOOD_GROUPS.first(),
    val role: String = AuthConstants.ROLES.first(),
    val password: String = "",
    val confirm: String = "",

    // Dropdown helpers
    val bgExpanded: Boolean = false,
    val roleExpanded: Boolean = false,

    // Allowed lists for user info
    val allowedBloodGroups: List<String> = AuthConstants.BLOOD_GROUPS,
    val allowedRoles: List<String> = AuthConstants.ROLES,

    // Progress / dialogs
    val isLoading: Boolean = false,
    val showVerifyDialog: Boolean = false,


    val emailError: String? = null,
    val nameError: String? = null,
    val lastNameError: String? = null,
    val codeError: String? = null,
    val bgError: String? = null,
    val roleError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val generalError: String? = null
)



sealed interface RegisterEvent {
    data class EditEmail(val v: String) : RegisterEvent
    data class EditName(val v: String) : RegisterEvent
    data class EditLastName(val v: String) : RegisterEvent
    data class EditCode(val v: String) : RegisterEvent
    data class EditPassword(val v: String) : RegisterEvent
    data class EditConfirm(val v: String) : RegisterEvent

    data object ToggleBg : RegisterEvent
    data object ToggleRole : RegisterEvent
    data class SelectBg(val v: String) : RegisterEvent
    data class SelectRole(val v: String) : RegisterEvent

    data object Submit : RegisterEvent
    data object DismissVerifyDialog : RegisterEvent
    data object ClearGeneralError : RegisterEvent
}



@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerWithEmail: RegisterWithEmail,
    private val sendEmailVerification: SendEmailVerification
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onEvent(e: RegisterEvent) {
        when (e) {
            is RegisterEvent.EditEmail     -> _state.update { it.copy(email = e.v, emailError = null, generalError = null) }
            is RegisterEvent.EditName      -> _state.update { it.copy(name = e.v, nameError = null, generalError = null) }
            is RegisterEvent.EditLastName  -> _state.update { it.copy(lastName = e.v, lastNameError = null, generalError = null) }
            is RegisterEvent.EditCode      -> _state.update { it.copy(code = e.v, codeError = null, generalError = null) }
            is RegisterEvent.EditPassword  -> _state.update { it.copy(password = e.v, passwordError = null, generalError = null) }
            is RegisterEvent.EditConfirm   -> _state.update { it.copy(confirm = e.v, confirmError = null, generalError = null) }

            RegisterEvent.ToggleBg         -> _state.update { it.copy(bgExpanded = !it.bgExpanded) }
            RegisterEvent.ToggleRole       -> _state.update { it.copy(roleExpanded = !it.roleExpanded) }
            is RegisterEvent.SelectBg      -> _state.update { it.copy(bloodGroup = e.v, bgError = null, bgExpanded = false) }
            is RegisterEvent.SelectRole    -> _state.update { it.copy(role = e.v, roleError = null, roleExpanded = false) }

            RegisterEvent.Submit           -> submit()
            RegisterEvent.DismissVerifyDialog -> _state.update { it.copy(showVerifyDialog = false) }
            RegisterEvent.ClearGeneralError   -> _state.update { it.copy(generalError = null) }
        }
    }

    private fun validate(): Boolean {
        val s = _state.value

        val emailErr = validateEmailDomain(s.email)
        val nameErr = validateName(s.name)
        val lastErr = validateLastName(s.lastName)
        val codeErr = validateUniandesCode(s.code)
        val bgErr   = validateBloodGroup(s.bloodGroup)
        val roleErr = validateRole(s.role)
        val passErr = validatePassword(s.password)
        val confErr = validatePasswordConfirm(s.confirm, s.password)

        _state.update {
            it.copy(
                emailError = emailErr,
                nameError = nameErr,
                lastNameError = lastErr,
                codeError = codeErr,
                bgError = bgErr,
                roleError = roleErr,
                passwordError = passErr,
                confirmError = confErr
            )
        }
        return listOf(emailErr, nameErr, lastErr, codeErr, bgErr, roleErr, passErr, confErr).all { it == null }
    }

    private fun submit() = viewModelScope.launch {
        if (!validate()) return@launch
        val s = _state.value

        _state.update { it.copy(isLoading = true, generalError = null) }
        try {
            // The use case must ALSO write the profile to Firestore (including email).
            registerWithEmail(
                email = s.email.trim(),
                password = s.password,
                confirmPassword = s.confirm,
                name = s.name.trim(),
                lastName = s.lastName.trim(),
                uniandesCode = s.code.trim(),
                bloodGroup = s.bloodGroup,
                role = s.role
            )
            // Send verification email.
            sendEmailVerification()

            _state.update { it.copy(isLoading = false, showVerifyDialog = true) }
        } catch (t: Throwable) {
            _state.update { it.copy(isLoading = false, generalError = t.message ?: "Unexpected error") }
        }
    }
}
