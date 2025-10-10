package com.example.brigadeapp.presentation.ui

import com.example.brigadeapp.presentation.viewmodel.ReportViewModel
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brigadeapp.R
import com.example.brigadeapp.presentation.ui.report.Alert
import com.example.brigadeapp.presentation.ui.report.CallButton
import com.example.brigadeapp.presentation.ui.report.CameraButton
import com.example.brigadeapp.presentation.ui.report.HoraDialog
import com.example.brigadeapp.presentation.ui.report.PlayAudioButton
import com.example.brigadeapp.presentation.ui.report.RecordAudioButton
import com.example.brigadeapp.ui.common.StandardScreen



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyReportScreen(
    modifier: Modifier = Modifier,
    reportViewModel: ReportViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onSubmit: () -> Unit,
) {
    LaunchedEffect(Unit) {
        reportViewModel.startTimer()
    }

    var lastPhotoFile by remember { mutableStateOf<Uri?>(null) }
    var lastAudioFile by remember { mutableStateOf<String?>(null) }
    var emergency_type by rememberSaveable { mutableStateOf("") }
    var emergency_place by rememberSaveable { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var selectedTime by rememberSaveable { mutableStateOf("") }
    var emergency_description by rememberSaveable { mutableStateOf("") }
    var select_followup by rememberSaveable { mutableStateOf(true) }

    val state = reportViewModel.state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.success) {
        if (state.success) showSuccessDialog = true
    }

    LaunchedEffect(state.error) {
        if (state.error != null) showErrorDialog = true
    }

    StandardScreen(title = stringResource(R.string.Emergency_Report), onBack = onBack) { inner ->
        Column(
            modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = emergency_type,
                onValueChange = { emergency_type = it },
                placeholder = { Text(stringResource(R.string.Emergency_Type)) },
                modifier = Modifier.fillMaxWidth()
            )

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                OutlinedTextField(
                    value = emergency_place,
                    onValueChange = { emergency_place = it },
                    placeholder = { Text(stringResource(R.string.Emergency_Place)) },
                    modifier = Modifier.padding(end = 10.dp).width(220.dp)
                )

                Button(
                    onClick = { showDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.width(90.dp)
                ) {
                    if (selectedTime == "") {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = stringResource(R.string.Emergency_Time),
                            tint = Color.White,
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        )
                    } else {
                        Text(
                            text = selectedTime,
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                HoraDialog(
                    showDialog = showDialog,
                    onDismiss = { showDialog = false },
                    onConfirm = { hora, minuto ->
                        selectedTime = "%02d:%02d".format(hora, minuto)
                    }
                )
            }

            OutlinedTextField(
                value = emergency_description,
                onValueChange = { emergency_description = it },
                placeholder = { Text(stringResource(R.string.Emergency_Description)) },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { select_followup = !select_followup }
            ) {
                RadioButton(selected = select_followup, onClick = null )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.FolloUp_Report))
            }

            Spacer(Modifier.height(5.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CameraButton { savedFile -> lastPhotoFile = savedFile }
                Spacer(Modifier.width(8.dp))
                RecordAudioButton { savedFile -> lastAudioFile = savedFile }
                Spacer(Modifier.width(8.dp))
                PlayAudioButton(filePath = lastAudioFile)
            }

            Button(
                onClick = {
                    reportViewModel.submitReport(
                        type = emergency_type,
                        place = emergency_place,
                        time = selectedTime,
                        description = emergency_description,
                        followUp = select_followup,
                        imageUri = lastPhotoFile.toString(),
                        audioUri = lastAudioFile
                    )
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2962FF)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(R.string.Submit_Report), color = Color.White)
            }

            Spacer(Modifier.height(25.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CallButton("6013394949", onCall = { reportViewModel.onCallPressed() })
            }

            if (showSuccessDialog) {
                Alert(
                    title = stringResource(R.string.SUCCESS),
                    text = stringResource(R.string.Created_Report),
                    onDismissRequest = { showSuccessDialog = false },
                    toggleEventDialog = { showSuccessDialog = false },
                    changeState = {
                        reportViewModel.state = reportViewModel.state.copy(success = false)
                    }
                )
                emergency_type = ""
                emergency_place = ""
                selectedTime = ""
                emergency_description = ""
                select_followup = true

                lastPhotoFile = null
                lastAudioFile = null

            }

            if (showErrorDialog) {
                Alert(
                    title = stringResource(R.string.ERROR),
                    text = state.error ?: stringResource(R.string.UnexpectedError),
                    onDismissRequest = { showErrorDialog = false },
                    toggleEventDialog = { showErrorDialog = false },
                    changeState = {
                        reportViewModel.state = reportViewModel.state.copy(error = null)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportPreview() {
    MaterialTheme {
        EmergencyReportScreen(
            modifier = TODO(),
            reportViewModel = TODO(),
            onBack = TODO(),
            onSubmit = TODO()
        )
    }
}
