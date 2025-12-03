package com.example.brigadeapp.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import com.example.brigadeapp.viewmodel.utils.RcpViewModel
import com.example.brigadeapp.R
import com.example.brigadeapp.domain.entity.AuthClient
import com.example.brigadeapp.view.common.StandardScreen
import com.example.brigadeapp.viewmodel.utils.ConnectivityViewModel

@Composable
fun RcpScreen(
    auth: AuthClient,
    onBack: () -> Unit = {},
    viewModel: RcpViewModel = hiltViewModel(),
    connectivityViewModel: ConnectivityViewModel = hiltViewModel()
) {
    val isOnlineState = connectivityViewModel.isOnline.collectAsState()
    val isOnline = isOnlineState.value

    val isGuidingState by viewModel.isGuiding.collectAsState()
    val isGuiding = isGuidingState

    val lastOpenAIResponseTime by viewModel.lastOpenAIResponseTime.collectAsState()
    
    // Refresh timestamp when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.refreshTimestamp()
    }
    
    val lastResponseTimeFormatted = remember(lastOpenAIResponseTime) {
        if (lastOpenAIResponseTime > 0) {
            val instant = Instant.ofEpochMilli(lastOpenAIResponseTime)
            val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
                .withZone(ZoneId.systemDefault())
            formatter.format(instant)
        } else {
            "Never"
        }
    }

    StandardScreen(title = stringResource(R.string.RCP), onBack = onBack) { inner ->
        Column(modifier = Modifier.fillMaxSize()) {
            // Banner with OpenAI response info
            OpenAIInfoBanner(
                lastResponseTime = lastResponseTimeFormatted,
                isOnline = isOnline,
                modifier = Modifier.fillMaxWidth()
            )
            
            Box(modifier = Modifier.fillMaxSize().padding(10.dp)
                .padding(inner),
                contentAlignment = Alignment.Center) {

                val message = if (isOnline) {
                    stringResource(R.string.CPR_Connection_Messsage)
                } else {
                    stringResource(R.string.CPR_NotConnection_Message)
                }
                AlertMessage(message,
                    modifier = Modifier.align(Alignment.TopCenter))

                if (!isGuiding) {
                    EmergencyButton(onClick = {
                        viewModel.startGuidance()
                    })
                } else {
                    val currentLine by viewModel.currentSpoken.collectAsState()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.FOLLOW_STEPS),
                            textAlign = TextAlign.Center,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                        )

                        Button(
                            modifier = Modifier
                                .padding(12.dp)
                                .width(200.dp)
                                .height(60.dp),
                            shape = RoundedCornerShape(24.dp),
                            onClick = {
                            viewModel.stopGuidance()
                        }) {
                            Text(
                                fontSize = 23.sp,
                                text = stringResource(R.string.STOP))
                        }

                        if (!currentLine.isNullOrEmpty()) {
                            Text(
                                text = currentLine.toString(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OpenAIInfoBanner(
    lastResponseTime: String,
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFFF9800)) // Orange color
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = if (isOnline) "Using OpenAI (Online)" else "Using Cached Response (Offline)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Last OpenAI response: $lastResponseTime",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}

@Composable
fun AlertMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0x7A673AB7))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Alert",
            tint = Color(0xFFE8E0E0),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EmergencyButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE24842)),
        modifier = Modifier
            .size(220.dp)
            .padding(24.dp)
            .background(Color.Transparent, shape = RoundedCornerShape(24.dp))
            .border(4.dp, Color(0xFFE24842), shape = RoundedCornerShape(24.dp))
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = stringResource(R.string.CPR),
            style = TextStyle(
                lineHeight = 30.sp
            ),
            textAlign = TextAlign.Center,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}