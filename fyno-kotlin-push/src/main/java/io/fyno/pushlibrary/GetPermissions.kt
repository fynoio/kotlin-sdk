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

internal class GetPermissions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()
        finish()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permissionResultManager(isGranted)
    }

    public fun permissionResultManager(result: Boolean) {
        if (result) {
            FynoUser.getFcmToken()?.let {
                FynoUser.setFcmToken(it)
            }
            FynoUser.getMiToken()?.let {
                FynoUser.setMiToken(it)
            }
            Log.d("PermissionDialog", "Notifications permission granted")
//            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT)
//                .show()
        } else {
            Log.d("PermissionDialog", "Can't post notifications without POST_NOTIFICATIONS permission")
//            Toast.makeText(
//                this, "Can't post notifications without POST_NOTIFICATIONS permission",
//                Toast.LENGTH_LONG).show()
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
}