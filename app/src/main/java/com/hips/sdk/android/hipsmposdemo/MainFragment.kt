package com.hips.sdk.android.hipsmposdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hips.sdk.hips.common.model.HipsTransactionRequest
import com.hips.sdk.hips.common.model.HipsTransactionResult
import com.hips.sdk.hips.common.model.TipFlowType
import com.hips.sdk.hips.common.model.TransactionType
import com.hips.sdk.hips.ui.CallbackManager
import com.hips.sdk.hips.ui.HipsUi
import com.hips.sdk.hips.ui.HipsUiBuilder
import com.hips.sdk.hips.ui.HipsUiCallback
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment() {

    private val TAG = this::class.java.simpleName

    private lateinit var hipsUi: HipsUi

    // Create a callbackManager instance
    private val callbackManager = CallbackManager.Factory.create()

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create instance of HipsUi
        hipsUi = HipsUiBuilder()
            .appContext(requireContext())
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Register a callback to retrieve transaction results
        hipsUi.registerCallback(callbackManager, object : HipsUiCallback<HipsTransactionResult> {
            override fun onResult(hipsTransactionResult: HipsTransactionResult) {
                Log.v(TAG, "onResult: $hipsTransactionResult")
                main_title.text = "onResult: $hipsTransactionResult"
            }

            override fun onError(errorCode: String, errorMessage: String?) {
                Log.v(TAG, "onError: $errorCode")
                main_title.text = "Transaction error: $errorCode $errorMessage"
            }
        })

        settings_button.setOnClickListener {
            hipsUi.openSettings(fragment = this)
        }

        payment_button.setOnClickListener {
            hipsUi.startSession(
                hipsTransactionRequest = HipsTransactionRequest.Payment(
                    amountInCents = 100,
                    vatInCents = 0,
                    reference = "This is a test payment",
                    transactionType = TransactionType.PURCHASE,
                    currencyIso = "SEK",
                    tipFlowType = TipFlowType.NONE,
                    isOfflinePayment = false,
                    isTestMode = true
                ),
                requestCode = 12345,
                fragment = this
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hipsUi.unregisterCallback(callbackManager)
    }
}