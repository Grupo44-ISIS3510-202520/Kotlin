package com.example.brigadeapp.view.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.R
import com.example.brigadeapp.model.core.AuthClient
import com.example.brigadeapp.view.common.StandardScreen
import com.example.brigadeapp.view.components.Alert
import com.example.brigadeapp.view.components.CameraButton
import com.example.brigadeapp.view.components.HoraDialog
import com.example.brigadeapp.view.components.PlayAudioButton
import com.example.brigadeapp.view.components.RecordAudioButton
import com.example.brigadeapp.viewmodel.screens.ReportViewModel
import com.example.brigadeapp.viewmodel.utils.TimerViewModel
import com.example.brigadeapp.viewmodel.utils.UploadFileViewModel
import kotlinx.coroutines.launch
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyReportScreen(
    modifier: Modifier = Modifier,
    reportViewModel: ReportViewModel = hiltViewModel(),
    fileViewModel: UploadFileViewModel = hiltViewModel(),
    timerViewModel: TimerViewModel = hiltViewModel(),
    auth: AuthClient,
    onBack: () -> Unit = {}
) {

    var lastPhotoFile by rememberSaveable { mutableStateOf<File?>(null) }
    var photoUrl by rememberSaveable { mutableStateOf<String?>(null) }

    var lastAudioFile by rememberSaveable { mutableStateOf<File?>(null) }
    var lastAudioPath by rememberSaveable { mutableStateOf<String?>(null) }
    var audioUrl by rememberSaveable { mutableStateOf<String?>(null) }

    var emergency_type by rememberSaveable { mutableStateOf("") }
    var emergency_place by rememberSaveable { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var selectedTime by rememberSaveable { mutableStateOf("") }
    var emergency_description by rememberSaveable { mutableStateOf("") }
    var select_followup by rememberSaveable { mutableStateOf(true) }

    val state = reportViewModel.state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val elapsed by timerViewModel.elapsedTime.collectAsState()
    var isLoading by remember { mutableStateOf(false) }

    // Init timer from start of the screen until the report is send
    LaunchedEffect(Unit) {
        timerViewModel.startTimer()
    }

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
                onValueChange = {
                    if (it.length <= 20){
                        emergency_type = it
                    }
                },
                placeholder = { Text(stringResource(R.string.Emergency_Type)) },
                label = { Text(stringResource(R.string.Emergency_Type)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                OutlinedTextField(
                    value = emergency_place,
                    onValueChange = {
                        if (it.length <= 20){
                            emergency_place = it
                        }
                    },
                    placeholder = { Text(stringResource(R.string.Emergency_Place)) },
                    label = { Text(stringResource(R.string.Emergency_Place)) },
                    singleLine = true,
                    modifier = Modifier.padding(end = 10.dp).width(220.dp)
                )

                Button(
                    onClick = { showDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.width(90.dp).height(70.dp)
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
                onValueChange = {
                    if (it.length <= 200){
                        emergency_description = it
                    }
                },
                placeholder = { Text(stringResource(R.string.Emergency_Description)) },
                label = { Text(stringResource(R.string.Emergency_Description)) },
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
                RecordAudioButton(
                    onAudioSaved = { savedFile -> lastAudioFile = savedFile },
                    onPathSaved = { filePath -> lastAudioPath = filePath })
                Spacer(Modifier.width(8.dp))
                PlayAudioButton(filePath = lastAudioPath)
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Button(
                    onClick = {
                        fileViewModel.viewModelScope.launch {
                            isLoading = true
                            if (lastPhotoFile != null) {
                                photoUrl = fileViewModel.uploadFile(
                                    lastPhotoFile!!,
                                    "brigadeapp-report-images",
                                    "${System.currentTimeMillis()}.jpg"
                                )
                            }

                            if (lastAudioFile != null) {
                                audioUrl = fileViewModel.uploadFile(
                                    lastAudioFile!!,
                                    "brigadeapp-report-audios",
                                    "${System.currentTimeMillis()}.mp3"
                                )
                            }

                            val duration = timerViewModel.stopTimer()

                            reportViewModel.submitReport(
                                type = emergency_type,
                                place = emergency_place,
                                time = selectedTime,
                                description = emergency_description,
                                followUp = select_followup,
                                imageUrl = photoUrl,
                                audioUrl = audioUrl,
                                elapsedTime = duration
                            )
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2962FF)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(R.string.Submit_Report), color = Color.White)
                }
            }

            Spacer(Modifier.height(25.dp))

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

                photoUrl = ""
                audioUrl = ""
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
            auth = TODO()
        )
    }
}
