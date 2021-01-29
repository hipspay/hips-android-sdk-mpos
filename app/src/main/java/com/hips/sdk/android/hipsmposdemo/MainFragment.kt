package com.hips.sdk.android.hipsmposdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.hips.sdk.hips.common.model.*
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

    private val currencyList = listOf(
        "EUR",
        "GBP",
        "USD",
        "PLN",
        "sek",
        "foo"
    )

    private val tipFlowList = listOf(
        TipFlowType.NONE,
        TipFlowType.TOP,
        TipFlowType.ASK,
        TipFlowType.TOP_CENTS
    )

    private val transactionList = listOf(
        TransactionType.PURCHASE,
        TransactionType.PREAUTHORIZATION
    )

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

        // Register a callback to retrieve results
        hipsUi.registerCallback(
            callbackManager = callbackManager,
            hipsUiCallback = object : HipsUiCallback<HipsResult> {
                override fun onResult(hipsResult: HipsResult) {

                    when (hipsResult) {
                        is HipsResult.Transaction -> {
                            Log.v(TAG, "onResult: ${hipsResult.hipsTransactionResult}")
                            val result = hipsResult.toString().replace(",", "\n")
                            hipsTransactionResultText.text = result
                        }
                        is HipsResult.NonPayment -> {
                            Log.v(TAG, "onResult: ${hipsResult.hipsNonPaymentMagSwipeResult}")
                            val result = hipsResult.toString().replace(",", "\n")
                            hipsTransactionResultText.text = result
                        }
                    }
                }

                override fun onError(errorCode: String, errorMessage: String?) {
                    hipsTransactionResultText.text = "$errorCode: $errorMessage"
                }
            })

        val currencyAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            currencyList.map { it })
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        demoCurrencyList.adapter = currencyAdapter

        val tipFlowAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            tipFlowList.map { it.name })
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        demoTipFlowList.adapter = tipFlowAdapter

        val transactionAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            transactionList.map { it.type.toUpperCase() })
        transactionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        demoTransactionList.adapter = transactionAdapter

        hipsPaymentBtn.setOnClickListener {
            launchPayment()
        }

        hipsNonPaymentMagSwipeBtn.setOnClickListener {
            launchReadMagNonPayment()
        }

        hipsSettingsBtn.setOnClickListener {
            launchSettings()
        }

        hipsRefundBtn.setOnClickListener {
            launchRefundLastTransaction()
        }
        hipsCaptureBtn.setOnClickListener {
            launchCaptureLastTransaction()
        }
    }

    // Let the Callback manager handle the fragment result from Hips UI SDK
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun launchPayment() {
        val hipsTransactionRequest = HipsTransactionRequest.Payment(
            amountInCents = demoAmount.text.trim().toString().toIntOrNull() ?: 0,
            reference = demoReference.text.trim().toString(),
            currencyIso = currencyList[demoCurrencyList.selectedItemPosition],
            tipFlowType = tipFlowList[demoTipFlowList.selectedItemPosition],
            transactionType = transactionList[demoTransactionList.selectedItemPosition],
            isOfflinePayment = offlineSwitch.isChecked,
            isTestMode = testSwitch.isChecked
        )

        hipsUi.startSession(
            hipsTransactionRequest = hipsTransactionRequest,
            requestCode = 1337,
            fragment = this
        )
    }

    private fun launchSettings() {
        val cashierToken = demoCashierToken.text.trim().toString()

        hipsUi.openSettings(
            hipsSettingsRequest = HipsSettingsRequest(cashierToken = cashierToken), // Optional param
            fragment = this
        )
    }

    private fun launchRefundLastTransaction() {
        val hipsTransactionRequest = HipsTransactionRequest.Refund(
            amountInCents = demoAmount.text.trim().toString().toIntOrNull(),
            transactionId = "Use a transaction id received from a purchase or pre auth",
            isTestMode = testSwitch.isChecked
        )

        hipsUi.startSession(
            hipsTransactionRequest = hipsTransactionRequest,
            requestCode = 1338,
            fragment = this
        )
    }

    private fun launchCaptureLastTransaction() {
        val hipsTransactionRequest = HipsTransactionRequest.Capture(
            amountInCents = demoAmount.text.trim().toString().toIntOrNull(),
            transactionId = "Use a transaction id received from a pre auth",
            isTestMode = testSwitch.isChecked
        )

        hipsUi.startSession(
            hipsTransactionRequest = hipsTransactionRequest,
            requestCode = 1339,
            fragment = this
        )
    }

    private fun launchReadMagNonPayment() {
        hipsUi.startNonPaymentRequest(
            hipsNonPaymentRequest = HipsNonPaymentRequest.MagSwipe(
                displayText = demoNonPaymentMagSwipeText.text.trim().toString()
            ),
            requestCode = 1340,
            fragment = this
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hipsUi.unregisterCallback(callbackManager)
    }
}