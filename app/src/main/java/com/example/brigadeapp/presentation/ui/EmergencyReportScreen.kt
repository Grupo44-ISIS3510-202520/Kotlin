package com.example.brigadeapp.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brigadeapp.ui.common.StandardScreen

@Composable
fun EmergencyReportScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSubmit: () -> Unit = {},
    onCall: () -> Unit = {}
) {
    StandardScreen(title = "Emergency Report", onBack = onBack) { inner ->
        Column(
            modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search reports...") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            )

            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Emergency Type") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Place & Time") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = false, onClick = { })
                Spacer(Modifier.width(8.dp))
                Text("Follow-up report")
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2962FF)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Submit Report", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportPreview() {
    MaterialTheme {
        EmergencyReportScreen()
    }
}