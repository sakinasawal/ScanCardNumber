package com.example.scancardnumber.camera

import android.app.Activity
import android.content.Intent
import androidx.camera.core.Camera
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.scancardnumber.R
import com.example.scancardnumber.databinding.ActivityCardScanBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CardScanActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCardScanBinding
    private var cameraExecutor: ExecutorService? = null
    private var camera : Camera? = null
    private var isTorchEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        initView()
        startCamera()
    }

    private fun initView(){
        binding.apply {
            btnTorchlight.setOnClickListener{
                isTorchEnabled = !isTorchEnabled
                camera?.cameraControl?.enableTorch(isTorchEnabled)

                if (isTorchEnabled){
                    btnTorchlight.setImageResource(R.drawable.ic_light_off)
                } else {
                    btnTorchlight.setImageResource(R.drawable.ic_light_on)
                }
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = binding.previewView.surfaceProvider
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor!!, CardAnalyzer { cardNumber ->
                        // When card detected → return to previous screen
                        val result = Intent().apply {
                            putExtra("CARD_NUMBER", cardNumber)
                        }
                        setResult(Activity.RESULT_OK, result)
                        finish()
                    })
                }

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer
                )

                camera?.cameraInfo?.torchState?.observe(this){ torchState ->
                    if (torchState == TorchState.ON) {
                        binding.btnTorchlight.setImageResource(R.drawable.ic_light_off)
                        isTorchEnabled = true
                    } else {
                        binding.btnTorchlight.setImageResource(R.drawable.ic_light_on)
                        isTorchEnabled = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
    }
}
