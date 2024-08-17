package com.mnvpatni.teamsync.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.mnvpatni.teamsync.R
import com.mnvpatni.teamsync.databinding.ActivityScannerBinding

class FoodScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding

    // Permission request launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value) permissionGranted = false
            }
            if (!permissionGranted) {
                // At least one permission is denied
                Snackbar.make(binding.root, "Permission denied", Snackbar.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request camera permissions
        if (!hasPermissions(this)) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            startCamera()
        }

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Set up the preview use case
            val previewUseCase = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            // Select the back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Bind the use cases to the camera
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    previewUseCase
                )
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
        private const val TAG = "BarcodeScannerActivity"

        // Check if all required permissions are granted
        fun hasPermissions(context: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

}