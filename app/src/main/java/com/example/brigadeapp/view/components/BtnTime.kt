package com.example.brigadeapp.view.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.brigadeapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoraDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    if (showDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = 12,
            initialMinute = 0,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                    onDismiss()
                }) {
                    Text(stringResource(R.string.ACCEPT))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.CANCEL))
                }
            },
            title = { Text(stringResource(R.string.Time_Pick)) },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}