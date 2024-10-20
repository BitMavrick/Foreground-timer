package com.bitmavrick.jet_watch.ui.permission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bitmavrick.jet_watch.service.JetWatchForegroundService
import com.bitmavrick.jet_watch.ui.home.HomeScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
    windowSize: WindowSizeClass,
    stopwatchService : JetWatchForegroundService
){
    val context = LocalContext.current
    val notificationPermissionState = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    val permissionString = "Jet Watch needs to allow notification permission to working properly, even in the background."
    val errorPermissionString = "You may not be able to use the app without granting this permission."
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }

    // * Tracks the permission
    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }
    var permissionRequestCompleted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect( notificationPermissionState.status) {
        if( hasRequestedPermission){
            permissionRequestCompleted = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when(val status = notificationPermissionState.status){
            is PermissionStatus.Granted -> {
                HomeScreen(stopwatchService)
            }

            is PermissionStatus.Denied -> {
                if (permissionRequestCompleted){
                    if (status.shouldShowRationale){
                        PermissionScreen(
                            windowSize = windowSize,
                            primaryText = permissionString,
                            errorText = errorPermissionString,
                            onClickGranted = {
                                notificationPermissionState.launchPermissionRequest()
                                hasRequestedPermission = true
                            },
                            onClickAppSettings = {
                                context.startActivity(intent)
                            }
                        )
                    }else{
                        PermissionScreen(
                            windowSize = windowSize,
                            primaryText = null,
                            errorText = "Notification Permission is denied completely. Please enable it from the settings menu",
                            onClickGranted = {
                                notificationPermissionState.launchPermissionRequest()
                                hasRequestedPermission = true
                            },
                            onClickAppSettings = {
                                context.startActivity(intent)
                            }
                        )
                    }
                }else{
                    PermissionScreen(
                        windowSize = windowSize,
                        primaryText = permissionString,
                        errorText = null,
                        onClickGranted = {
                            notificationPermissionState.launchPermissionRequest()
                            hasRequestedPermission = true
                        },
                        onClickAppSettings = {
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}