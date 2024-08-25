package com.mnvpatni.teamsync.scanner

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.mnvpatni.teamsync.adapter.TeamRestRoomAdapter
import com.mnvpatni.teamsync.databinding.ActivityScannerBinding
import com.mnvpatni.teamsync.models.Participant
import com.mnvpatni.teamsync.models.RestRoomParticipant
import com.mnvpatni.teamsync.models.UpdateRestRoomRequest
import com.mnvpatni.teamsync.models.UpdateRestRoomResponse
import com.mnvpatni.teamsync.network.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors

class RestRoomScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private lateinit var participantAdapter: TeamRestRoomAdapter
    private lateinit var progressDialog: ProgressDialog
    private var analysisUseCase: ImageAnalysis? = null
    private var lastCapturedBarcode: String? = null

    private var lastApiCallTime: Long = 0
    private val apiCallDelayMs = 2000 // 2 seconds

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                startCamera()
            } else {
                showSnackbar("Permission denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupCameraPermissions()
    }

    private fun setupUI() {
        progressDialog = createProgressDialog()
        binding.apply {
            btnBack.text = "Rest Room Scanner"
            spinnerDay.visibility = View.GONE
            spinnerFood.visibility = View.GONE

            btnBack.setOnClickListener { finish() }

            participantAdapter = TeamRestRoomAdapter()
            rvTeamMembers.apply {
                adapter = participantAdapter
                layoutManager = LinearLayoutManager(this@RestRoomScannerActivity)
            }

            btnForceSearch.setOnClickListener {
                val teamUID = etTeamUid.text?.toString()
                if (teamUID.isNullOrBlank()) {
                    showSnackbar("Enter team UID.")
                } else {
                    getDetails(teamUID)
                }
            }

            btnUpdate.setOnClickListener { handleUpdate() }
        }
    }

    private fun setupCameraPermissions() {
        if (hasPermissions()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    private fun createProgressDialog(): ProgressDialog {
        return ProgressDialog(this).apply {
            setMessage("Please wait...")
            setCancelable(false)
        }
    }

    private fun getDetails(teamUID: String) {
        if (System.currentTimeMillis() - lastApiCallTime < apiCallDelayMs) return
        lastApiCallTime = System.currentTimeMillis()

        progressDialog.show()
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getTeamRestRoomStatus(teamUID)
                if (response.statusCode == 200) {
                    response.body?.let { participants ->
                        binding.tvTeamName.text = participants.team_name
                        participantAdapter.updateData(participants.details)
                    }
                } else {
                    showSnackbar("Failed to fetch data: ${response.statusCode}")
                }
            } catch (e: Exception) {
                showToast("An unexpected error occurred: ${e.localizedMessage}")
                Log.e(TAG, "Error fetching data", e)
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    private fun handleUpdate() {
        val participants = participantAdapter.getParticipants()
        // Map the participants to the required format
        val participantList = participants.map { participant ->
            RestRoomParticipant(name = participant.name, isInRestRoom = participant.isInRestRoom)
        }

        val totalParticipants = participants.size
        val participantsInRestRoom = participants.count { it.isInRestRoom == 1 }

        val percentageInRestRoom = (participantsInRestRoom.toDouble() / totalParticipants) * 100

        if (percentageInRestRoom <= 50) {
            updateStatus(participantList)
        } else {
            showWarningDialog(participantList)
        }
    }

    private fun showWarningDialog(participants: List<RestRoomParticipant>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            triggerWarningVibration()
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Warning")
            .setMessage("More than 50% of participants are in the restroom. This may affect the team's progress in the hackathon. Do you want to continue?")
            .setPositiveButton("Continue Anyways") { _, _ -> updateStatus(participants) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateStatus(participants: List<RestRoomParticipant>) {
        val teamUid = binding.etTeamUid.text.toString()

        RetrofitInstance.api.updateRestRoomDetails(UpdateRestRoomRequest(team_uid = teamUid, participants = participants))
            .enqueue(object : Callback<UpdateRestRoomResponse> {
                override fun onResponse(call: Call<UpdateRestRoomResponse>, response: Response<UpdateRestRoomResponse>) {
                    if (response.isSuccessful) {
                        showToast(response.body()?.message ?: "Update successful, but no message was returned.")
                    } else {
                        showToast("Update failed with status code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<UpdateRestRoomResponse>, t: Throwable) {
                    showToast("Network error: ${t.message}")
                    Log.e(TAG, "Error updating status", t)
                }
            })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val previewUseCase = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            analysisUseCase = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().apply {
                    setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            try {
                cameraProvider.bindToLifecycle(this, cameraSelector, previewUseCase, analysisUseCase)
            } catch (e: Exception) {
                Log.e(TAG, "Camera binding error: ${e.message}", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val image = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
        BarcodeScanning.getClient().process(inputImage)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { value ->
                    if (value != lastCapturedBarcode) {
                        lastCapturedBarcode = value
                        binding.etTeamUid.setText(value)
                        getDetails(value)
                    }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Barcode processing failed: ${it.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun triggerWarningVibration() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun hasPermissions() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val TAG = "RestRoomScannerActivity"
    }
}
