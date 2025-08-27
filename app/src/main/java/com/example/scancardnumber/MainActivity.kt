package com.example.scancardnumber

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.scancardnumber.camera.CardScanActivity
import com.example.scancardnumber.databinding.ActivityMainBinding
import com.example.scancardnumber.utils.Utils
import com.processout.sdk.core.onFailure
import com.processout.sdk.core.onSuccess
import com.processout.sdk.ui.card.scanner.POCardScannerConfiguration
import com.processout.sdk.ui.card.scanner.POCardScannerLauncher
import com.squareup.moshi.internal.Util
import io.card.payment.CardIOActivity
import io.card.payment.CreditCard

class MainActivity : AppCompatActivity() {

   private lateinit var binding : ActivityMainBinding

    private lateinit var cardScannerLauncher: POCardScannerLauncher

    private val cardScanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        val data = result.data
        if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)){
            val scanResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT, CreditCard::class.java)
            } else {
                @Suppress("DEPRECATION")
                data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT)
            }
            binding.etCcNumber2.setText(Utils.formatCardNumber(" ", scanResult?.cardNumber ?: ""))
        }
    }

    private val cardScanNumberLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == Activity.RESULT_OK) {
            val cardNumber = result.data?.getStringExtra("CARD_NUMBER") ?: return@registerForActivityResult
            binding.etCcNumber3.setText(
                Utils.formatCardNumber(" ", cardNumber)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cardScannerLauncher = POCardScannerLauncher.create(from = this){ result ->
            result.onSuccess { card ->
                val regexNumber = card.number.filter { it.isDigit() }.take(16)
                val formattedCard = Utils.formatCardNumber(" ", regexNumber)
                binding.etCcNumber.setText(formattedCard)
            }.onFailure {
                Toast.makeText(this, "Scan failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.apply {
            btnCamera.setOnClickListener {
                cardScannerLauncher.launch(
                    POCardScannerConfiguration()
                )
            }

            btnCamera2.setOnClickListener {
                val scanIntent = Intent(this@MainActivity, CardIOActivity::class.java)
                scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, false)
                scanIntent.putExtra(CardIOActivity.EXTRA_USE_CARDIO_LOGO, false)
                scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, true)
                scanIntent.putExtra(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, true)
                scanIntent.putExtra(CardIOActivity.EXTRA_USE_PAYPAL_ACTIONBAR_ICON, false)
                scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, true)
                scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false)
                scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false)
                cardScanLauncher.launch(scanIntent)
            }

            btnCamera3.setOnClickListener {
                val intent = Intent(this@MainActivity, CardScanActivity::class.java)
                cardScanNumberLauncher.launch(intent)
            }

            btnClear.setOnClickListener{
                etCcNumber.setText("")
                etCcNumber2.setText("")
                etCcNumber3.setText("")
            }
        }
    }
}