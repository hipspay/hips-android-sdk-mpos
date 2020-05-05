package com.hips.sdk.android.hipsmposdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hips.sdk.hips.common.exception.HipsException
import com.hips.sdk.hips.common.model.CurrencyType
import com.hips.sdk.hips.common.model.HipsTransactionResult
import com.hips.sdk.hips.common.model.PriceDetails
import com.hips.sdk.hips.common.model.TipFlow
import com.hips.sdk.hips.common.result.HipsUiTransaction
import com.hips.sdk.hips.ui.CallbackManager
import com.hips.sdk.hips.ui.HipsUi
import com.hips.sdk.hips.ui.HipsUiBuilder
import com.hips.sdk.hips.ui.HipsUiCallback
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val TAG = this::class.java.simpleName

    private lateinit var hipsUi: HipsUi

    // Create a callbackManager instance
    private val callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create instance of HipsUi
        hipsUi = HipsUiBuilder()
            .appContext(applicationContext)
            .isTestMode(true)
            .build()

        // Register a callback to retrieve transaction results
        hipsUi.registerCallback(callbackManager, object : HipsUiCallback<HipsTransactionResult> {
            override fun onSuccess(hipsTransactionResult: HipsTransactionResult) {
                Log.v(TAG, "onSuccess: $hipsTransactionResult")
                main_title.text = "Success: $hipsTransactionResult"
            }

            override fun onCanceled() {
                Log.v(TAG, "onCanceled")
                main_title.text = "Transaction canceled"

            }

            override fun onError(exception: HipsException?) {
                Log.v(TAG, "onError: $exception")
                main_title.text = "Error: Transaction error ${exception?.message}"
            }
        })

        settings_button.setOnClickListener {
            hipsUi.openSettings(this)
        }

        payment_button.setOnClickListener {
            /**
             * PriceDetails is nullable. If no object is passed Hips Ui i will provide controls
             * to fill in the required properties.
             *
             * Currently supported in SDK:
             * - Amount via Hips Keyboard, pass 0.0 to trigger keyboard
             * - Description
             * - VAT
             * - Currency
             *
             * @param priceDetails
             * @param isOfflinePayment
             * @param requestCode
             * @param activity
             */
            hipsUi.startPayment(
                priceDetails = PriceDetails(
                    amountInCents = 0, // Pass zero to trigger Hips Ui Keyboard
                    vatInCents = 0,
                    description = "This is a test payment",
                    currencyType = CurrencyType.SEKrona,
                    tipFlow = TipFlow.TOP
                ),
                isOfflinePayment = false,
                requestCode = 12345,
                activity = this
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
