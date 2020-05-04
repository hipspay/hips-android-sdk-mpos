# Hips UI Android SDK v0.9.2
Hips Android SDK is a library that provides the native In-App interaction of performing the Hips MPOS payment directly from an app on the Android device.

# Project Status
---
Supported payment methods:
- Cards: Visa, Mastercard

Unsupported payment methods:
- Offline

# Usage
----
In the example below, the SDK is initialised and setup to receive callbacks
- Create you own instance of HipsUI SDK.

```kotlin
    val hipsUi = HipsUiBuilder()
            .appContext(applicationContext)
            .isTestMode(true)
            .merchantId("YOUR_MERCHANT_ID")
            .build()
```

- Create a CallbackManager.Factory handler and register it with the Hips SDK

```kotlin
    val callbackManager = CallbackManager.Factory.create()
    ...

    hipsUi.registerCallback(callbackManager, object : HipsUiCallback<HipsTransactionResult> {
        override fun onSuccess(hipsTransactionResult: HipsTransactionResult) {
            Log.v(TAG, "onSuccess: $hipsTransactionResult")
        }

        override fun onCanceled() {
            Log.v(TAG, "onCanceled")
        }

        override fun onError(
                exception: HipsException?,
                hipsUiTransactionRequest: HipsUiTransaction.Request?
        ) {
            Log.v(TAG, "onError: $exception")
        }
    })
```

- In the activity which launched the SDK, override the `onActivityResult()` to handle the SDK result:

```kotlin
...
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
...
```

- Launch SDK to perform payment:

```kotlin
    payment_button.setOnClickListener {
        hipsUi.startPayment(
                activity = this,
                priceDetails = PriceDetails(
                        amount = 0.0, // Pass zero to show Hips Ui Keyboard
                        vat = 0.0,
                        description = "This is a test payment",
                        currencyType = CurrencyType.USDollar
                ),
                isOfflinePayment = false
        )
    }
```

- Launch SDK settings to setup your terminal:

```kotlin
    settings_button.setOnClickListener {
        hipsUi.openSettings(this)
    }
```
