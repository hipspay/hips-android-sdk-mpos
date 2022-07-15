package com.hips.sdk.android.hipsmposdemo

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.hips.sdk.android.hipsmposdemo.databinding.FragmentMainBinding
import com.hips.sdk.hips.common.model.HipsLoyaltyCardReadRequest
import com.hips.sdk.hips.common.model.HipsTransactionRequest
import com.hips.sdk.hips.common.model.TipFlowType
import com.hips.sdk.hips.common.model.TransactionType
import com.hips.sdk.hips.ui.internal.contracts.*
import com.hips.sdk.hips.ui.internal.model.*

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

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
        TipFlowType.TOP_CENTS,
        TipFlowType.ASK_CENTS
    )

    private val transactionList = listOf(
        TransactionType.PURCHASE,
        TransactionType.PREAUTHORIZATION
    )

    // Add Hips Payment Transaction Launcher to handle payment results
    private val paymentTransactionLauncher = registerForActivityResult(
        HipsUiPaymentTransactionContract()
    ) { result ->
        when (result) {
            is HipsUiPaymentTransactionContractResult.Transaction -> onResult(result.formatResult())
            is HipsUiPaymentTransactionContractResult.Error -> onError(result.code, result.message)
            is HipsUiPaymentTransactionContractResult.Cancelled -> onCancelled()
        }
    }

    // Add Hips Refund Transaction Launcher to handle refund results
    private val refundTransactionLauncher = registerForActivityResult(
        HipsUiRefundTransactionContract()
    ) { result ->
        when (result) {
            is HipsUiRefundTransactionContractResult.Transaction -> onResult(result.formatResult())
            is HipsUiRefundTransactionContractResult.Error -> onError(result.code, result.message)
            is HipsUiRefundTransactionContractResult.Cancelled -> onCancelled()
        }
    }

    // Add Hips Capture Transaction Launcher to handle capture results
    private val captureTransactionLauncher = registerForActivityResult(
        HipsUiCaptureTransactionContract()
    ) { result ->
        when (result) {
            is HipsUiCaptureTransactionContractResult.Transaction -> onResult(result.formatResult())
            is HipsUiCaptureTransactionContractResult.Error -> onError(result.code, result.message)
            is HipsUiCaptureTransactionContractResult.Cancelled -> onCancelled()
        }
    }

    // Add Hips Offline batch upload Launcher to handle offline sync results
    private val offlineBatchUploadLauncher = registerForActivityResult(
        HipsUiOfflineBatchUploadContract()
    ) { result ->
        when (result) {
            is HipsUiOfflineBatchUploadContractResult.Success -> onResult(result.formatResult())
            is HipsUiOfflineBatchUploadContractResult.Error -> onError(result.code, result.message)
            is HipsUiOfflineBatchUploadContractResult.Cancelled -> onCancelled()
        }
    }

    // Add Hips Loyalty Launcher to handle loyalty card results
    private val loyaltyLauncher = registerForActivityResult(
        HipsUiLoyaltyContract()
    ) { result ->
        when (result) {
            is HipsUiLoyaltyCardReadContractResult.Success -> onResult(result.formatResult())
            is HipsUiLoyaltyCardReadContractResult.Error -> onError(result.code, result.message)
            is HipsUiLoyaltyCardReadContractResult.Cancelled -> onCancelled()
        }
    }

    // Add Hips Activate Terminal Launcher to handle activation results
    private val activateTerminalLauncher = registerForActivityResult(
        HipsUiActivateTerminalContract()
    ) { result ->
        when (result) {
            is HipsUiActivateTerminalContractResult.Success -> onResult(result.formatResult())
            is HipsUiActivateTerminalContractResult.Error -> onError(result.code, result.message)
            is HipsUiActivateTerminalContractResult.Cancelled -> onCancelled()
        }
    }

    // Add Hips Update Terminal Launcher to handle parameter update results
    private val updateTerminalLauncher = registerForActivityResult(
        HipsUiUpdateTerminalContract()
    ) { result ->
        when (result) {
            is HipsUiUpdateTerminalContractResult.Success -> onResult(result.formatResult())
            is HipsUiUpdateTerminalContractResult.Error -> onError(result.code, result.message)
            is HipsUiUpdateTerminalContractResult.Cancelled -> onCancelled()
        }
    }

    private val requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Implement permission handling
        }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            // Implement permission handling
        }

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermissions()

        val currencyAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            currencyList.map { it })
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.demoCurrencyList.adapter = currencyAdapter

        val tipFlowAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            tipFlowList.map { it.name })
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.demoTipFlowList.adapter = tipFlowAdapter

        val transactionAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            transactionList.map { it.type.uppercase() })
        transactionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.demoTransactionList.adapter = transactionAdapter

        binding.demoPaymentBtn.setOnClickListener {
            launchPayment()
        }

        binding.demoNonPaymentMagSwipeBtn.setOnClickListener {
            launchReadMagNonPayment()
        }

        binding.demoSettingsBtn.setOnClickListener {
            launchSettings()
        }

        binding.demoRefundBtn.setOnClickListener {
            launchRefundLastTransaction()
        }
        binding.demoCaptureBtn.setOnClickListener {
            launchCaptureLastTransaction()
        }

        binding.demoActivationBtn.setOnClickListener {
            launchActivation()
        }

        binding.demoUpdateBtn.setOnClickListener {
            launchParamUpdate()
        }

        binding.demoOfflineBatchBtn.setOnClickListener {
            launchOfflineBatch()
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }

    private fun launchPayment() {
        val hipsTransactionRequest = HipsTransactionRequest.Payment(
            amountInCents = binding.demoAmount.text.trim().toString().toIntOrNull() ?: 0,
            reference = binding.demoReference.text.trim().toString(),
            currencyIso = currencyList[binding.demoCurrencyList.selectedItemPosition],
            tipFlowType = tipFlowList[binding.demoTipFlowList.selectedItemPosition],
            transactionType = transactionList[binding.demoTransactionList.selectedItemPosition],
            isOfflinePayment = binding.offlineSwitch.isChecked,
            isTestMode = binding.testSwitch.isChecked
        )

        hipsUi.startPaymentSession(
            paymentTransactionLauncher = paymentTransactionLauncher,
            transactionRequest = hipsTransactionRequest,
        )
    }

    private fun launchRefundLastTransaction() {
        val hipsTransactionRequest = HipsTransactionRequest.Refund(
            amountInCents = binding.demoAmount.text.trim().toString().toIntOrNull(),
            transactionId = "Use a transaction id received from a purchase or pre auth",
            isTestMode = binding.testSwitch.isChecked
        )

        hipsUi.startRefundSession(
            refundTransactionLauncher = refundTransactionLauncher,
            transactionRequest = hipsTransactionRequest,
        )
    }

    private fun launchCaptureLastTransaction() {
        val hipsTransactionRequest = HipsTransactionRequest.Capture(
            amountInCents = binding.demoAmount.text.trim().toString().toIntOrNull(),
            transactionId = "Use a transaction id received from a pre auth",
            isTestMode = binding.testSwitch.isChecked
        )

        hipsUi.startCaptureSession(
            captureTransactionLauncher = captureTransactionLauncher,
            transactionRequest = hipsTransactionRequest,
        )
    }

    private fun launchOfflineBatch() {
        hipsUi.startOfflineBatchUploadSession(
            offlineBatchUploadLauncher = offlineBatchUploadLauncher
        )
    }

    private fun launchReadMagNonPayment() {
        hipsUi.startLoyaltySession(
            loyaltyLauncher = loyaltyLauncher,
            loyaltyRequest = HipsLoyaltyCardReadRequest.MagSwipe(
                displayText = binding.demoNonPaymentMagSwipeText.text.trim().toString()
            ),
        )
    }

    private fun launchActivation() {
        hipsUi.startActivateTerminalSession(
            activateTerminalLauncher = activateTerminalLauncher
        )
    }

    private fun launchParamUpdate() {
        hipsUi.startUpdateTerminalSession(
            updateTerminalLauncher = updateTerminalLauncher
        )
    }

    private fun launchSettings() {
        hipsUi.openSettings(
            fragment = this
        )
    }

    private fun onResult(result: String) {
        binding.demoTransactionResultText.text = result
    }

    private fun onError(
        errorCode: String,
        errorMessage: String
    ) {
        binding.demoTransactionResultText.text = "$errorCode: $errorMessage"
    }

    private fun onCancelled() {
        binding.demoTransactionResultText.text = "Cancelled"
    }

    private fun Any.formatResult(): String = toString().replace(",", "\n")

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}