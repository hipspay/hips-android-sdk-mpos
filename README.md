# Hips UI Android SDK v0.9.26
Hips Android SDK is a library that provides the native In-App interaction of performing the Hips MPOS payment directly from an app on the Android device.

# Project Status
---
Supported terminals:
- Miura M010
- Miura M020

Supported payment schemes:
- Cards: Visa, Mastercard

# Demo app
----
This git repository contains a demo app for development reference. If you need test cards and test terminals, they can be ordered here: [Hips Store](https://hips.com/store)

# Usage
----
In the example below, the SDK is initialised and setup to receive callbacks

To authorise dependency downloads, add your provided token to `hipsAuthToken` in `gradle.properties`. Please ask your account manager or Hips Support for your hipsAuthToken.

- Add the token to $HOME/.gradle/gradle.properties or $PROJECT_ROOT/gradle.properties
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
- Add dependencies in your apps `build.gradle`

```kotlin
    implementation 'org.bitbucket.hipspay.hips-android-sdk-mpos:hips-common:LATEST-VERSION'
    implementation 'org.bitbucket.hipspay.hips-android-sdk-mpos:hips-core:LATEST-VERSION'
    implementation 'org.bitbucket.hipspay.hips-android-sdk-mpos:hips-ui:LATEST-VERSION'
    implementation 'org.bitbucket.hipspay.hips-android-sdk-mpos:hips-terminal-miura:LATEST-VERSION'
```
## Kotlin
```kotlin
    // Create you own instance of HipsUI SDK. 
    val hipsUi = HipsUiBuilder()
            .appContext(applicationContext)
            .build()

    // Create a CallbackManager handler and register it with the Hips SDK
    val callbackManager = CallbackManager.Factory.create()

    // Register CallbackManager with the Hips SDK
    hipsUi.registerCallback(callbackManager, object : HipsUiCallback<HipsTransactionResult> {
        override fun onResult(hipsTransactionResult: HipsTransactionResult) {
            if (hipsTransactionResult.transactionApproved) {
                hipsTransactionResultText.text = "Approved!"
            } else {
                hipsTransactionResultText.text = "Declined!"
            }
        }

        override fun onError(errorCode: String, errorMessage: String?) {
            hipsTransactionResultText.text = "$errorCode: $errorMessage"
        }
    })

    // In the activity which launched the SDK, override the `onActivityResult()` to handle the SDK result:
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    // Create a new payment request with a transaction type
    val hipsTransactionRequest = HipsTransactionRequest.Payment(
                amountInCents = 100,
                vatInCents = 0,
                cashbackInCents = 0,
                currencyIso = "SEK",
                transactionType = TransactionType.PURCHASE,
                tipFlowType = TipFlowType.TOP,
                reference = "This is a test payment",
                employeeNumber = "1234",
                cashierToken = "", // Optional param
                metadata1 = "", // Optional param
                metadata2 = "", // Optional param
                webHook = "",  // Optional param
                isOfflinePayment = true,
                isTestMode = testSwitch.isChecked
            )

    // Start a new session 
    hipsUi.startSession(
        hipsTransactionRequest = hipsTransactionRequest,
        requestCode = 12345,
        fragment = this
    )

    // Launch SDK settings from activity or fragment
    settings_button.setOnClickListener {
        hipsUi.openSettings(this)
    }

    // Unregister CallbackManager
    override fun onDestroyView() {
        super.onDestroyView()
        hipsUi.unregisterCallback(callbackManager)
    }
```
## Java
```java

    // Create you own instance of HipsUI SDK. 
    HipsUi hipsUi = new HipsUiBuilder()
             .appContext(applicationContext)
             .build();

    // Create a CallbackManager handler
    CallbackManager callbackManager = CallbackManager.Factory.INSTANCE.create();

    // Register CallbackManager with the Hips SDK
    hipsUi.registerCallback(callbackManager, new HipsUiCallback<HipsTransactionResult>() {
        @Override
        public void onResult(@NotNull HipsTransactionResult hipsTransactionResult) {
            if (hipsTransactionResult.getTransactionApproved()) {
                hipsTransactionResultText.setText("Approved!");
            } else {
                hipsTransactionResultText.setText("Declined!");
            }
        }
        @Override
        public void onError(@NotNull String errorCode, @org.jetbrains.annotations.Nullable String errorMessage) {
            hipsTransactionResultText.setText(errorCode + ":  " + errorMessage);
        }
    });

    // In the activity/fragment which launched the SDK, override the `onActivityResult()` to handle the SDK result:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Create a new payment request with a transaction type
    HipsTransactionRequest hipsTransactionRequest = new HipsTransactionRequest.Payment(
            100, // amountInCents
            0, // vatInCents
            0, // cashbackInCents
            "This is a test payment", // reference
            "1234", // employeeNumber
            null, // cashierToken, Optional
            null, // metadata1, Optional
            null, // metadata2, Optional
            null, // webHook, Optional
            "SEK", // currencyIso
            TipFlowType.TOP, // tipFlowType
            TransactionType.PURCHASE, // transactionType
            false, // isOfflinePayment
            true // isTestMode
    );

    // Start a new session from actvity or fragment
    hipsUi.startSession(
            hipsTransactionRequest,
            1337,
            this
    );

    // Launch SDK settings from activity or fragment
    hipsUi.openSettings(this)

    // Unregister CallbackManager
    @Override
    protected void onDestroyView() {
        super.onDestroyView();
        hipsUi.unregisterCallback(callbackManager);
    }
```
## Transaction status
Check status for approved or declined transactions  in `HipsTransactionResult`

## Response and Error Codes
Find all available codes at [Hips Docs](https://docs.hips.com/reference#errors)
