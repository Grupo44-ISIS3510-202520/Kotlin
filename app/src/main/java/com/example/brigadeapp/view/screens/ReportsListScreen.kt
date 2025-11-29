package com.example.brigadeapp.view.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.brigadeapp.R
import com.example.brigadeapp.domain.entity.Report
import com.example.brigadeapp.view.common.StandardScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsListScreen(
    modifier: Modifier = Modifier,
    reports: List<Report> = emptyList(),
    onBack: () -> Unit = {},
    onReportClick: (Report) -> Unit = {}
) {
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
                modifier = Modifier
                    .fillMaxWidth()
            )

            val filtered = remember(reports, query) {
                if (query.isBlank()) reports
                else {
                    val q = query.trim().lowercase()
                    reports.filter { r ->
                        r.type.lowercase().contains(q) ||
                                r.place.lowercase().contains(q) ||
                                (r.time ?: "").lowercase().contains(q)
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filtered, key = { it.id }) { report ->
                    ReportCard(
                        report = report,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = { onReportClick(report) }
                    )
                }
            }
        }
    }
}


@Composable
fun ReportCard(
    report: Report,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = report.type, style = MaterialTheme.typography.titleMedium)
                Text(text = report.place, style = MaterialTheme.typography.bodyMedium)
                Text(text = report.time ?: "", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}