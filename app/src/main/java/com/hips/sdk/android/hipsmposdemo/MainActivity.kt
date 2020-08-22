package com.hips.sdk.android.hipsmposdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hips.sdk.hips.common.exception.HipsException
import com.hips.sdk.hips.common.model.*
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

            override fun onError(
                exception: HipsException?,
                hipsTransactionResult: HipsTransactionResult?
            ) {
                Log.v(TAG, "onError: $exception")
                main_title.text = "Error: Transaction error ${exception?.message}"
            }
        })

        settings_button.setOnClickListener {
            hipsUi.openSettings(this)
        }

        payment_button.setOnClickListener {
            hipsUi.startSession(
                hipsTransactionRequest = HipsTransactionRequest.Payment(
                    amountInCents = 0,
                    vatInCents = 0,
                    reference = "This is a test payment",
                    transactionType = TransactionType.PURCHASE,
                    currencyType = CurrencyType.SEKrona,
                    tipFlowType = TipFlowType.NONE,
                    isOfflinePayment = false,
                    isTestMode = true
                ),
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
