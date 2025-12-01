package com.example.brigadeapp.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brigadeapp.R
import com.example.brigadeapp.domain.entity.CachedReport
import com.example.brigadeapp.view.common.StandardScreen
import com.example.brigadeapp.view.components.ReportDetailBottomSheet
import com.example.brigadeapp.viewmodel.screens.ReportsListViewModel


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
                val filtered = remember(state.reports, query) {
                    if (query.isBlank()) state.reports
                    else {
                        val q = query.trim().lowercase()
                        state.reports.filter { r ->
                            r.type.lowercase().contains(q) ||
                                    r.place.lowercase().contains(q) ||
                                    r.reportId.lowercase().contains(q)
                        }
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(filtered, key = { it.reportId }) { report ->
                        ReportCard(
                            report = report,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = { 
                                selectedReport = report
                                onReportClick(report)
                            }
                        )
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
fun ReportCard(
    report: CachedReport,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                    text = report.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
