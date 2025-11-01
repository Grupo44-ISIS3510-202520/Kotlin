package com.example.brigadeapp.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.brigadeapp.viewmodel.screens.RegisterEvent
import com.example.brigadeapp.viewmodel.screens.RegisterUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onEvent: (RegisterEvent) -> Unit,
    onBack: () -> Unit,
    onRegistered: () -> Unit
) {
    if (state.showVerifyDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(RegisterEvent.DismissVerifyDialog) },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(RegisterEvent.DismissVerifyDialog)
                    onRegistered()
                }) { Text("OK") }
            },
            title = { Text("Verify your email") },
            text  = { Text("Please check your email and verify your account.") }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create account") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.email,
                onValueChange = { onEvent(RegisterEvent.EditEmail(it)) },
                label = { Text("Email (@uniandes.edu.co)") },
                singleLine = true,
                isError = state.emailError != null,
                supportingText = { state.emailError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = { onEvent(RegisterEvent.EditName(it)) },
                label = { Text("Name (max 15)") },
                singleLine = true,
                isError = state.nameError != null,
                supportingText = { state.nameError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.lastName,
                onValueChange = { onEvent(RegisterEvent.EditLastName(it)) },
                label = { Text("Last name (max 15)") },
                singleLine = true,
                isError = state.lastNameError != null,
                supportingText = { state.lastNameError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.code,
                onValueChange = { onEvent(RegisterEvent.EditCode(it)) },
                label = { Text("Uniandes code") },
                singleLine = true,
                isError = state.codeError != null,
                supportingText = { state.codeError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            // Blood group
            ExposedDropdownMenuBox(
                expanded = state.bgExpanded,
                onExpandedChange = { onEvent(RegisterEvent.ToggleBg) }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    value = state.bloodGroup,
                    onValueChange = {},
                    label = { Text("Blood group") },
                    isError = state.bgError != null,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.bgExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = state.bgExpanded,
                    onDismissRequest = { onEvent(RegisterEvent.ToggleBg) }
                ) {
                    state.allowedBloodGroups.forEach { bg ->
                        DropdownMenuItem(
                            text = { Text(bg) },
                            onClick = { onEvent(RegisterEvent.SelectBg(bg)) }
                        )
                    }
                }
            }

            // Role
            ExposedDropdownMenuBox(
                expanded = state.roleExpanded,
                onExpandedChange = { onEvent(RegisterEvent.ToggleRole) }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    value = state.role,
                    onValueChange = {},
                    label = { Text("Role") },
                    isError = state.roleError != null,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.roleExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = state.roleExpanded,
                    onDismissRequest = { onEvent(RegisterEvent.ToggleRole) }
                ) {
                    state.allowedRoles.forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r) },
                            onClick = { onEvent(RegisterEvent.SelectRole(r)) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.password,
                onValueChange = { onEvent(RegisterEvent.EditPassword(it)) },
                label = { Text("Password (min 6, max 20)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = state.passwordError != null,
                supportingText = { state.passwordError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.confirm,
                onValueChange = { onEvent(RegisterEvent.EditConfirm(it)) },
                label = { Text("Confirm password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = state.confirmError != null,
                supportingText = { state.confirmError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            state.generalError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = { onEvent(RegisterEvent.Submit) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isLoading) "Creating…" else "Create account")
            }
        }
    }
}
