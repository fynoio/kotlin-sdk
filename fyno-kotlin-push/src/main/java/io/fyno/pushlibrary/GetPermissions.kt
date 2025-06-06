package io.fyno.pushlibrary

import android.Manifest
import android.content.pm.PackageManager
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import io.fyno.core.FynoUser
import io.fyno.core.utils.Logger

class GetPermissions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Logger.d("PermissionDialog", "Notifications permission granted")
            updateUserPermissionStatus(true)
            finish()
        } else {
            Logger.d("PermissionDialog", "Can't post notifications without POST_NOTIFICATIONS permission")
            finish()
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun updateUserPermissionStatus(isGranted: Boolean) {
        if (isGranted) {
            FynoUser.getFcmToken()?.let {
                FynoUser.setFcmToken(it)
            }
            FynoUser.getMiToken()?.let {
                FynoUser.setMiToken(it)
            }
        }
    }
}