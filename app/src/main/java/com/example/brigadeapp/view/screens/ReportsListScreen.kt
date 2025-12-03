package com.example.brigadeapp.view.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brigadeapp.R
import com.example.brigadeapp.domain.entity.CachedReport
import com.example.brigadeapp.view.common.StandardScreen
import com.example.brigadeapp.view.components.ReportDetailBottomSheet
import com.example.brigadeapp.viewmodel.screens.ReportsListViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsListScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportsListViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onReportClick: (CachedReport) -> Unit = {}
) {
    val state = viewModel.state
    var selectedReport by remember { mutableStateOf<CachedReport?>(null) }
    
    StandardScreen(title = stringResource(R.string.Emergency_Report), onBack = onBack) { inner ->
        val keyboardController = LocalSoftwareKeyboardController.current
        
        Column(
            modifier
                .padding(inner)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Cache info banner
            CacheInfoBanner(
                lastSyncTime = state.lastSyncTime,
                dataAge = state.dataAge,
                isOnline = state.isOnline,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                var query by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = query,
                    onValueChange = { newValue -> 
                        if (newValue.length <= 30) {
                            query = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.SearchByType)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { 
                        keyboardController?.hide()
                    }),
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (state.error != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    val filteredPending = remember(state.pendingReports, query) {
                        if (query.isBlank()) state.pendingReports
                        else {
                            val q = query.trim().lowercase()
                            state.pendingReports.filter { r ->
                                r.type.lowercase().contains(q) ||
                                        r.place.lowercase().contains(q) ||
                                        r.reportId.lowercase().contains(q)
                            }
                        }
                    }

                    val filteredSynced = remember(state.syncedReports, query) {
                        if (query.isBlank()) state.syncedReports
                        else {
                            val q = query.trim().lowercase()
                            state.syncedReports.filter { r ->
                                r.type.lowercase().contains(q) ||
                                        r.place.lowercase().contains(q) ||
                                        r.reportId.lowercase().contains(q)
                            }
                        }
                    }

                    if (filteredPending.isEmpty() && filteredSynced.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (query.isBlank()) stringResource(R.string.NOT_CACHED_REPORTS) else stringResource(R.string.NOT_RESULTS),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (query.isBlank()) stringResource(R.string.RECONNECT_MESSSAGE_REPORTS) else stringResource(R.string.IVALID_QUERY),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pending reports section
                            if (filteredPending.isNotEmpty()) {
                                item {
                                    PendingReportsHeader()
                                }
                                items(filteredPending, key = { it.reportId }) { report ->
                                    ReportCard(
                                        report = report,
                                        isPending = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        onClick = {
                                            val cachedReport = viewModel.getReportFromCache(report.reportId) ?: report
                                            selectedReport = cachedReport
                                            onReportClick(cachedReport)
                                        }
                                    )
                                }
                                item {
                                    Spacer(Modifier.height(16.dp))
                                }
                            }

                            // Synced reports
                            items(filteredSynced, key = { it.reportId }) { report ->
                                ReportCard(
                                    report = report,
                                    isPending = false,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    onClick = {
                                        val cachedReport = viewModel.getReportFromCache(report.reportId) ?: report
                                        selectedReport = cachedReport
                                        onReportClick(cachedReport)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        selectedReport?.let { report ->
            ReportDetailBottomSheet(
                report = report,
                onDismiss = { selectedReport = null }
            )
        }
    }
}

@Composable
fun CacheInfoBanner(
    lastSyncTime: String,
    dataAge: String,
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
            text = if (isOnline) "Using Firebase data" else "Viewing Cache data",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Data age: $dataAge",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
        Text(
            text = "Last sync: $lastSyncTime",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
fun PendingReportsHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFF9800).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CloudUpload,
            contentDescription = "Pending sync",
            tint = Color(0xFFFF9800),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "Pending Reports (will sync when online)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFFF9800)
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun formatTimestamp(timestamp: String): String {
    return try {
        val dateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a", Locale.getDefault())
        dateTime.format(formatter)
    } catch (e: Exception) {
        throw Exception("Error formatting timestamp: ${e.message}")
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReportCard(
    report: CachedReport,
    isPending: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = when {
        !isPending -> MaterialTheme.colorScheme.surfaceContainerLow
        isDarkTheme -> Color(0xFF3E2723)  // Dark orange-brown for dark mode
        else -> Color(0xFFFFF3E0)  // Light orange for light mode
    }
    
    Surface(
        modifier = modifier,
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isPending) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Pending",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${report.reportId} - ${report.type}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = report.place,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(report.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
