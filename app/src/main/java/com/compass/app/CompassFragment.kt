package com.compass.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.compass.app.databinding.FragmentCompassBinding

class CompassFragment : Fragment() {

    private var _binding: FragmentCompassBinding? = null
    private val binding get() = _binding!!
    private val sensorViewModel: SensorViewModel by activityViewModels()

    private var currentAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH
    private var currentPitch = 0f
    private var currentRoll = 0f

    private var isCameraOn = false
    private var cameraProvider: ProcessCameraProvider? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enableTrueNorthLocation()
        } else {
            binding.switchTrueNorth.isChecked = false
            Toast.makeText(requireContext(), "Coarse location permission required for True North", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            toggleCameraMode()
        } else {
            Toast.makeText(requireContext(), "Camera permission required for AR mode", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCompassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sensorViewModel.azimuth.observe(viewLifecycleOwner) { azimuth ->
            updateDisplay(azimuth)
        }

        sensorViewModel.pitch.observe(viewLifecycleOwner) { pitch ->
            currentPitch = pitch
            binding.compassView.pitch = pitch
            updateAlerts()
        }

        sensorViewModel.roll.observe(viewLifecycleOwner) { roll ->
            currentRoll = roll
            binding.compassView.roll = roll
            updateAlerts()
        }

        sensorViewModel.accuracy.observe(viewLifecycleOwner) { accuracy ->
            currentAccuracy = accuracy
            updateAlerts()
        }

        sensorViewModel.useTrueNorth.observe(viewLifecycleOwner) { useTrueNorth ->
            if (binding.switchTrueNorth.isChecked != useTrueNorth) {
                binding.switchTrueNorth.isChecked = useTrueNorth
            }
        }

        binding.switchTrueNorth.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableTrueNorthLocation()
            } else {
                sensorViewModel.setUseTrueNorth(false)
            }
        }

        binding.btnToggleCamera.setOnClickListener {
            Toast.makeText(requireContext(), "AR button tapped", Toast.LENGTH_SHORT).show()
            toggleCameraMode()
        }

        binding.btnShowCalibrationGuide.setOnClickListener {
            showCalibrationDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isCameraOn) {
            startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isCameraOn) {
            // Unbind camera usecases but keep isCameraOn = true to re-trigger onResume
            try {
                cameraProvider?.unbindAll()
            } catch (e: Exception) {}
        }
    }

    private fun enableTrueNorthLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            return
        }


        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            try {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            } catch (e: SecurityException) {
                // ignore
            }
        }

        bestLocation?.let {
            sensorViewModel.setLocation(it.latitude, it.longitude, it.altitude)
            sensorViewModel.setUseTrueNorth(true)
        } ?: run {
            try {
                locationManager.requestSingleUpdate(
                    LocationManager.NETWORK_PROVIDER,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            sensorViewModel.setLocation(location.latitude, location.longitude, location.altitude)
                            sensorViewModel.setUseTrueNorth(true)
                        }
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    },
                    null
                )
            } catch (e: Exception) {
                // ignore
            }
            sensorViewModel.setUseTrueNorth(true)
        }
    }

    private fun toggleCameraMode() {
        val hasPerm = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPerm) {
            Toast.makeText(requireContext(), "Requesting camera permission...", Toast.LENGTH_SHORT).show()
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        Toast.makeText(requireContext(), "Permission OK, toggling camera...", Toast.LENGTH_SHORT).show()

        if (isCameraOn) {
            stopCamera()
        } else {
            startCamera()
        }
    }

    private fun startCamera() {
        Toast.makeText(requireContext(), "Starting AR camera...", Toast.LENGTH_SHORT).show()

        // Make preview visible and let it lay out before binding
        binding.previewView.visibility = View.VISIBLE
        binding.scrollContent.background = ColorDrawable(Color.TRANSPARENT)
        binding.compassView.isCameraMode = true

        // Post to ensure the PreviewView is fully laid out before binding CameraX
        binding.previewView.post {
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
                cameraProviderFuture.addListener({
                    try {
                        cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build()
                        preview.surfaceProvider = binding.previewView.surfaceProvider

                        // Check for back camera, fallback to front camera
                        var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        if (cameraProvider?.hasCamera(cameraSelector) == false) {
                            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                        }
                        if (cameraProvider?.hasCamera(cameraSelector) == false) {
                            throw IllegalStateException("No camera found on this device")
                        }

                        cameraProvider?.unbindAll()
                        cameraProvider?.bindToLifecycle(
                            this@CompassFragment,
                            cameraSelector,
                            preview
                        )

                        isCameraOn = true
                        binding.btnToggleCamera.text = "Close AR"
                        Toast.makeText(requireContext(), "AR Camera active", Toast.LENGTH_SHORT).show()
                    } catch (exc: Exception) {
                        stopCamera()
                        Toast.makeText(requireContext(), "Camera error: ${exc.localizedMessage ?: exc.toString()}", Toast.LENGTH_LONG).show()
                    }
                }, ContextCompat.getMainExecutor(requireContext()))
            } catch (e: Exception) {
                stopCamera()
                Toast.makeText(requireContext(), "Camera init error: ${e.localizedMessage ?: e.toString()}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun stopCamera() {
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {}
        binding.previewView.visibility = View.GONE
        binding.compassView.isCameraMode = false
        binding.scrollContent.background = ColorDrawable(Color.parseColor("#0F0F1A"))
        isCameraOn = false
        binding.btnToggleCamera.text = "AR Camera"
    }

    private fun updateAlerts() {
        val isAccuracyLow = currentAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE ||
                            currentAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW

        val isTilted = kotlin.math.abs(currentPitch) > 15f || kotlin.math.abs(currentRoll) > 15f

        if (isAccuracyLow) {
            binding.layoutAlertContainer.visibility = View.VISIBLE
            binding.layoutAccuracyAlert.visibility = View.VISIBLE
            binding.layoutTiltAlert.visibility = View.GONE
        } else if (isTilted) {
            binding.layoutAlertContainer.visibility = View.VISIBLE
            binding.layoutAccuracyAlert.visibility = View.GONE
            binding.layoutTiltAlert.visibility = View.VISIBLE
        } else {
            binding.layoutAlertContainer.visibility = View.GONE
            binding.layoutAccuracyAlert.visibility = View.GONE
            binding.layoutTiltAlert.visibility = View.GONE
        }
    }

    private fun showCalibrationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("How to Calibrate Compass")
            .setMessage("1. Hold the phone firmly in your hand.\n\n" +
                        "2. Move your phone in the air in a figure-8 (infinity symbol ♾️) motion.\n\n" +
                        "3. Do this motion 2 or 3 times while rotating the phone slightly along its axes.\n\n" +
                        "This helps the magnetic sensor map and compensate for internal and external magnetic interference.")
            .setPositiveButton("Got it", null)
            .show()
    }

    private fun updateDisplay(azimuth: Float) {
        binding.compassView.azimuth = azimuth

        val names = VastuData.getNames(azimuth)
        binding.tvEnglish.text = names.english
        binding.tvTelugu.text  = names.telugu
        binding.tvTamil.text   = names.tamil

        val code   = VastuData.getCode(azimuth)
        val result = VastuData.mainEntranceVastu[code]!!
        val color  = Color.parseColor(result.rating.colorHex)

        binding.tvVastuStars.text      = result.rating.stars
        binding.tvVastuStars.setTextColor(color)
        binding.tvVastuLabel.text      = "  ${result.rating.label}"
        binding.tvVastuLabel.setTextColor(color)
        binding.tvVastuAdvice.text     = result.advice
        binding.vastuIndicator.setBackgroundColor(color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraProvider = null
        _binding = null
    }
}
