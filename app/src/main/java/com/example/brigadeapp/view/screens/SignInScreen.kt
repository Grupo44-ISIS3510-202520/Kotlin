package com.example.brigadeapp.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.brigadeapp.viewmodel.screens.SignInEvent
import com.example.brigadeapp.viewmodel.screens.SignInUiState

@Composable
fun SignInScreen(
    state: SignInUiState,
    onEvent: (SignInEvent) -> Unit,
    onCreateAccount: () -> Unit,
    logoRes: Int? = null
) {
    Scaffold { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (logoRes != null) {
                        Image(
                            painter = painterResource(logoRes),
                            contentDescription = "Logo"
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Logo",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Text("Inicia sesión", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = state.email,
                onValueChange = { onEvent(SignInEvent.EditEmail(it)) },
                label = { Text("Correo electrónico") },
                singleLine = true,
                isError = state.emailError != null,
                supportingText = { state.emailError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            var showPass by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = state.password,
                onValueChange = { onEvent(SignInEvent.EditPassword(it)) },
                label = { Text("Contraseña") },
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                trailingIcon = {
                    TextButton(onClick = { showPass = !showPass }) {
                        Text(if (showPass) "Ocultar" else "Ver")
                    }
                },
                isError = state.passwordError != null,
                supportingText = { state.passwordError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            state.generalError?.let {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { onEvent(SignInEvent.ClearGeneralError) }) {
                            Text("Entendido")
                        }
                    }
                }
            }

            Button(
                onClick = { onEvent(SignInEvent.SubmitLogin) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isLoading) "Ingresando…" else "Ingresar")
            }

            OutlinedButton(
                onClick = onCreateAccount,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear cuenta")
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}
