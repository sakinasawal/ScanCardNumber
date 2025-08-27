package com.example.scancardnumber.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class CardAnalyzer(
    private val onCardDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val text = visionText.text.replace(" ", "")
                val regex = Regex("\\d{16}")
                val match = regex.find(text)
                match?.let {
                    val cardNumber = it.value
                    if(isValidCardNumber(cardNumber)){
                        onCardDetected(cardNumber)
                    } else {
                        Log.w("OCR", "Invalid card number (failed Luhn): $cardNumber")
                    }
                }
            }
            .addOnFailureListener {
                Log.e("OCR", "Error: ${it.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    /**
     * checksum formula used to validate numbers
     */
    private fun isValidCardNumber(number: String): Boolean {
        val digits = number.map { it.toString().toIntOrNull() ?: return false }

        val sum = digits.reversed().mapIndexed { index, digit ->
            if (index % 2 == 1) {
                val doubled = digit * 2
                if (doubled > 9) doubled - 9 else doubled
            } else {
                digit
            }
        }.sum()

        return sum % 10 == 0
    }
}


