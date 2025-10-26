@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.brigadeapp.view.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brigadeapp.viewmodel.utils.ConnectivityViewModel

@Composable
fun StandardScreen(
    title: String,
    onBack: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val connectivityViewModel: ConnectivityViewModel = hiltViewModel()
    val isOnlineState = connectivityViewModel.isOnline.collectAsState()
    val isOnline = isOnlineState.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    ConnectivityBanner(isOnline = isOnline)
                }
            )
        },
        content = content
    )
}

