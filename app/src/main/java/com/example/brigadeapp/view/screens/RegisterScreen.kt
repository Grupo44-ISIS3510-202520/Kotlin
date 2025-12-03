package com.example.brigadeapp.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brigadeapp.R
import com.example.brigadeapp.viewmodel.screens.RegisterEvent
import com.example.brigadeapp.viewmodel.screens.RegisterUiState
import com.example.brigadeapp.viewmodel.utils.ConnectivityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onEvent: (RegisterEvent) -> Unit,
    onBack: () -> Unit,
    onRegistered: () -> Unit
) {
    val connectivityVm: ConnectivityViewModel = hiltViewModel()
    val isOnline by connectivityVm.isOnline.collectAsState(initial = true)
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

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
            text = { Text("Please check your email and verify your account.") }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // App Logo
            Image(
                painter = painterResource(R.drawable.icon_app),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Create your account to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Offline Banner
            if (!isOnline) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Hey Uniandino, you're offline! Reconnect to create your account.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // Name Field
            OutlinedTextField(
                value = state.name,
                onValueChange = { 
                    if (it.length <= 15) onEvent(RegisterEvent.EditName(it))
                },
                label = { Text("Name") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                singleLine = true,
                isError = state.nameError != null,
                supportingText = { 
                    state.nameError?.let { Text(it) } ?: Text("${state.name.length}/15")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(12.dp))

            // Last Name Field
            OutlinedTextField(
                value = state.lastName,
                onValueChange = { 
                    if (it.length <= 15) onEvent(RegisterEvent.EditLastName(it))
                },
                label = { Text("Last name") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                singleLine = true,
                isError = state.lastNameError != null,
                supportingText = { 
                    state.lastNameError?.let { Text(it) } ?: Text("${state.lastName.length}/15")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(12.dp))

            // Uniandes Code Field
            OutlinedTextField(
                value = state.code,
                onValueChange = { 
                    if (it.length <= 12) onEvent(RegisterEvent.EditCode(it))
                },
                label = { Text("Uniandes code") },
                leadingIcon = {
                    Icon(Icons.Default.Badge, contentDescription = null)
                },
                singleLine = true,
                isError = state.codeError != null,
                supportingText = { state.codeError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(12.dp))

            // Blood Group Dropdown
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
                    leadingIcon = {
                        Icon(Icons.Default.Bloodtype, contentDescription = null)
                    },
                    trailingIcon = { 
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.bgExpanded) 
                    },
                    isError = state.bgError != null,
                    shape = MaterialTheme.shapes.medium
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

            Spacer(Modifier.height(12.dp))

            // Role Dropdown
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
                    leadingIcon = {
                        Icon(Icons.Default.School, contentDescription = null)
                    },
                    trailingIcon = { 
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.roleExpanded) 
                    },
                    isError = state.roleError != null,
                    shape = MaterialTheme.shapes.medium
                )
                ExposedDropdownMenu(
                    expanded = state.roleExpanded,
                    onDismissRequest = { onEvent(RegisterEvent.ToggleRole) }
                ) {
                    state.allowedRoles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role) },
                            onClick = { onEvent(RegisterEvent.SelectRole(role)) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Email Field
            OutlinedTextField(
                value = state.email,
                onValueChange = { 
                    if (it.length <= 30) onEvent(RegisterEvent.EditEmail(it))
                },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                singleLine = true,
                isError = state.emailError != null,
                supportingText = { 
                    state.emailError?.let { Text(it) } ?: Text("${state.email.length}/30")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(12.dp))

            // Password Field
            OutlinedTextField(
                value = state.password,
                onValueChange = { 
                    if (it.length <= 20) onEvent(RegisterEvent.EditPassword(it))
                },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showPassword) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                isError = state.passwordError != null,
                supportingText = { 
                    state.passwordError?.let { Text(it) } ?: Text("Min 6, max 20 chars")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(12.dp))

            // Confirm Password Field
            OutlinedTextField(
                value = state.confirm,
                onValueChange = { 
                    if (it.length <= 20) onEvent(RegisterEvent.EditConfirm(it))
                },
                label = { Text("Confirm password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { showConfirm = !showConfirm }) {
                        Icon(
                            if (showConfirm) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showConfirm) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                isError = state.confirmError != null,
                supportingText = { state.confirmError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(24.dp))

            // Create Account Button
            Button(
                onClick = { onEvent(RegisterEvent.Submit) },
                enabled = !state.isLoading && isOnline,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = if (state.isLoading) "Creating…" else "Create account",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // Error Message
            state.generalError?.let {
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
