package com.example.brigadeapp.view.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

import com.example.brigadeapp.R

@Composable
internal fun Alert(
    title: String,
    text: String,
    onDismissRequest: () -> Unit,
    toggleEventDialog: () -> Unit,
    changeState: () -> Unit
){
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = {
                toggleEventDialog()
                changeState()
            }) {
                Text(stringResource(R.string.ACCEPT))
            }
        },

    )
}