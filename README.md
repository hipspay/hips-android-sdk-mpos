# Hips UI Android SDK v0.9.2
Hips Android SDK is a library that provides the native In-App interaction of performing the Hips MPOS payment directly from an app on the Android device.

# Project Status
---
Supported terminals:
- Miura M010
- Miura M020

Supported payment schemes:
- Cards: Visa, Mastercard

Unsupported:
- Offline

# Usage
----
In the example below, the SDK is initialised and setup to receive callbacks

To authorise dependency downloads, add your provided token to ´hipsAuthToken´ in ´gradle.properties´. Please ask your account manager or Hips Support for your hipsAuthToken.

- Add the token to $HOME/.gradle/gradle.properties 
```
hipsAuthToken=AUTHENTICATION_TOKEN
```
In your build root folder, add:

```kotlin
allprojects {
    repositories {
        ...
        maven {
            url "https://jitpack.io"
            credentials { username hipsAuthToken }
        }
    }
}
```
- Add dependencies in your apps ´build.gradle´

```kotlin
    implementation 'org.bitbucket.hipspay.hips-android-sdk-mpos:hips-common:LATEST-VERSION'
    implementation 'org.bitbucket.hipspay.hips-android-sdk-mpos:hips-core:LATEST-VERSION'
    implementation 'org.bitbucket.hipspay.hips-android-sdk-mpos:hips-ui:LATEST-VERSION'
```

- Create you own instance of HipsUI SDK.

```kotlin
    val hipsUi = HipsUiBuilder()
            .appContext(applicationContext)
            .isTestMode(true)
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
                        amount = 1000, // Pass zero (0) to show Hips Ui Keyboard. Amount in lowest denomination 10.00 = 1000
                        vat = 800, // Part of amount that is VAT/Tax (Value added Tax). Amount in lowest denomination 8.00 = 800
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
