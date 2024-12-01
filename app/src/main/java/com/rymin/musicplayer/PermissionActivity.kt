package com.rymin.musicplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class PermissionActivity : ComponentActivity() {

    private val requiredPermissions = mutableListOf<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_AUDIO)
            add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }.toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PermissionScreen(
                permissions = requiredPermissions,
                onPermissionsGranted = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onPermissionsDenied = {
                    finish()
                }
            )
        }
    }
}
@Composable
fun PermissionScreen(
    permissions: Array<String>,
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit
) {
    var permissionGranted by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionGranted = results.all { it.value }
        if (permissionGranted) {
            onPermissionsGranted()
        } else {
            onPermissionsDenied()
        }
    }

    LaunchedEffect(Unit) {
        permissionGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        // 모든 권한이 승인된 경우 바로 MainActivity로 이동
        if (permissionGranted) {
            onPermissionsGranted()
        }
    }

    if (!permissionGranted) {
        // 권한 요청 UI
        PermissionRequestContent(onRequestPermissions = {
            permissionLauncher.launch(permissions)
        })
    }
}


@Composable
fun PermissionRequestContent(onRequestPermissions: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("앱을 사용하려면 권한 승인이 필요합니다.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRequestPermissions) {
                Text("권한 승인하기")
            }
        }
    }
}

