package com.mnvpatni.teamsync.scanner

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
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
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.mnvpatni.teamsync.adapter.TeamMealAdapter
import com.mnvpatni.teamsync.databinding.ActivityScannerBinding
import com.mnvpatni.teamsync.models.MealUpdateRequest
import com.mnvpatni.teamsync.models.Participant
import com.mnvpatni.teamsync.models.UpdateResponse
import com.mnvpatni.teamsync.network.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors

class FoodScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private lateinit var participantAdapter: TeamMealAdapter
    private lateinit var progressDialog: ProgressDialog
    private var analysisUseCase: ImageAnalysis? = null
    private var lastCapturedBarcode: String? = null

    private var lastApiCallTime: Long = 0
    private val API_CALL_DELAY_MS = 2000 // 2 seconds

    // Permission request launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allPermissionsGranted = REQUIRED_PERMISSIONS.all { permissions[it] == true }
            if (allPermissionsGranted) {
                startCamera()
            } else {
                Snackbar.make(binding.root, "Permission denied", Snackbar.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this).apply {
            setMessage("Please wait...")
            setCancelable(false)
        }

        binding.btnBack.text = "Meal Scanner"
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Request camera permissions
        if (hasPermissions(this)) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }

        // Initialize adapter with an empty list
        participantAdapter = TeamMealAdapter()
        binding.rvTeamMembers.adapter = participantAdapter
        binding.rvTeamMembers.layoutManager = LinearLayoutManager(this)

        binding.btnForceSearch.setOnClickListener {
            val teamUID = binding.etTeamUid.text?.toString()
            if (!teamUID.isNullOrBlank()) {
                getDetails(teamUID)
            } else {
                Snackbar.make(binding.main, "Enter team UID.", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.btnUpdate.setOnClickListener {
            updateMealRecords()
        }
    }

    private fun getDetails(teamUID: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastApiCallTime < API_CALL_DELAY_MS) {
            return // Avoid calling the API if the last call was too recent
        }

        lastApiCallTime = currentTime
        progressDialog.show()
        val selectedDay = binding.spinnerDay.selectedItem.toString()
        val selectedFood = binding.spinnerFood.selectedItem.toString()
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getTeamMealStatus(teamUID, selectedDay, selectedFood)

                if (response.statusCode == 200) {
                    val participants = response.body
                    binding.tvTeamName.text = participants.team_name
                    participantAdapter.updateData(participants.details)
                } else {
                    Snackbar.make(binding.root, "Failed to fetch data: ${response.statusCode}", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching data: ${e.localizedMessage}", e)
                Toast.makeText(this@FoodScannerActivity, "An unexpected error occurred: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    private fun updateMealRecords() {
        val teamUid = binding.etTeamUid.text.toString()
        val selectedDate = binding.spinnerDay.selectedItem.toString()
        val selectedMealType = binding.spinnerFood.selectedItem.toString()

        val participantsFromAdapter = participantAdapter.getParticipants()

        // Map the participants to the required format
        val participantList = participantsFromAdapter.map { participant ->
            Participant(name = participant.name, attended = participant.attended)
        }

        val requestBody = MealUpdateRequest(
            team_uid = teamUid,
            date = selectedDate,
            meal_type = selectedMealType,
            participants = participantList
        )

        // Log the request payload before sending it
        Log.d(TAG, "Request Payload: ${Gson().toJson(requestBody)}")

        RetrofitInstance.api.updateMealRecords(requestBody).enqueue(object : Callback<UpdateResponse> {
            override fun onResponse(call: Call<UpdateResponse>, response: Response<UpdateResponse>) {
                if (response.isSuccessful) {
                    val updateResponse = response.body()
                    val message = updateResponse?.body
                    if (!message.isNullOrEmpty()) {
                        Toast.makeText(this@FoodScannerActivity, message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@FoodScannerActivity, "Update successful, but no message was returned.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@FoodScannerActivity, "Update failed with status code: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
                Log.d(TAG, "API Response: ${response.body()}")
                Log.d(TAG, "Request Body: $requestBody")
                val gson = Gson()
                val jsonRequestBody = gson.toJson(requestBody)
                Log.d("FoodScannerActivity", "Serialized JSON Request Body: $jsonRequestBody")
                Log.d(TAG, "API Response - UID: ${teamUid}")
                Log.d(TAG, "API Response - selectedDay: ${selectedDate}")
                Log.d(TAG, "API Response - selectedFood: ${selectedMealType}")
                Log.d(TAG, "API Response - participantList: ${participantList}")
            }

            override fun onFailure(call: Call<UpdateResponse>, t: Throwable) {
                Log.e(TAG, "Error updating meal records: ${t.message}", t)
                Toast.makeText(this@FoodScannerActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // setting up the preview use case
            val previewUseCase = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            // configure to use the back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // setting up the image analysis use case
            analysisUseCase = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysisUseCase?.setAnalyzer(
                Executors.newSingleThreadExecutor()
            ) { imageProxy ->
                processImageProxy(imageProxy)
            }

            try {
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    previewUseCase,
                    analysisUseCase
                )
            } catch (illegalStateException: IllegalStateException) {
                // If the use case has already been bound to another lifecycle or method is not called on main thread.
                Log.e(TAG, illegalStateException.message.orEmpty())
            } catch (illegalArgumentException: IllegalArgumentException) {
                // If the provided camera selector is unable to resolve a camera to be used for the given use cases.
                Log.e(TAG, illegalArgumentException.message.orEmpty())
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupMlKit() {
        val options = BarcodeScannerOptions.Builder()
            .enableAllPotentialBarcodes()
            .build()
        val scanner = BarcodeScanning.getClient(options)
        analysisUseCase?.setAnalyzer(
            Executors.newSingleThreadExecutor()
        ) { imageProxy ->
            processImageProxy(imageProxy)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val image = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            image, imageProxy.imageInfo.rotationDegrees
        )

        val scanner = BarcodeScanning.getClient()
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val value = barcode.rawValue
                    if (value != null && value != lastCapturedBarcode) {
                        lastCapturedBarcode = value
                        binding.etTeamUid.setText(value)
                        // Call getDetails() method only if the QR code is new
                        getDetails(value)
                    }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Barcode processing failed: ${it.message}")
            }
            .addOnCompleteListener {
                // Ensure that the imageProxy is closed after processing
                imageProxy.close()
            }
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val TAG = "FoodScannerActivity"

        // Check if all required permissions are granted
        fun hasPermissions(context: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}