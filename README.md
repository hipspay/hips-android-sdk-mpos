# Hips UI Android SDK 1.1.0
Hips Android SDK is a library that provides the native In-App interaction of performing the Hips MPOS payment directly from an app on the Android device.

# Project Status
---
Supported terminals:
- Miura M010
- Miura M020

Supported payment schemes:
- Cards: Visa, Mastercard, Unionpay, American Express, Diners, Discover

Supported reading methods:
- Contact (online pin & offline pin)
- Contactless (online pin)
- Mag swipe (online pin)
- ApplePay
- Google Pay (GPay)
- Samsung Pay

Supported features
- Online authorizations
- Offline authorizations (deferred authorizations)
- Offline transaction
- Offline PIN
- Online PIN
- Tip (3 different flows)
- Loyalty card reading (magnetic)
- Refunds/Reversals
- Capture
- Localization

#### Change log
| Version | Description                                                                 | Date       |
|:--------|:----------------------------------------------------------------------------|:-----------|
| `1.1.0` | Added offline upload, terminal activation and param update APIs in `HipsUI` | 2021-02-05 |
| `1.0.1` | Offline payment hotfix                                                      | 2021-01-29 |

# Demo app
----
This git repository contains a demo app for development reference. If you need test cards and test terminals, they can be ordered here: [Hips Store](https://hips.com/store)

# Integration checklist
Please make sure you tick all on this integration checklist to be Hips Certified.
- [x] Make sure you pass any reference for the payment in the reference parameter or as meta data.
- [x] Make sure the data is passed to the server by logging in to the Hips dashboard and look in the API logs
- [x] If you get `requiresParameterDownload` = `true` in the response object you must run `HipsUi.updateTerminal()` function as soon as possible to make sure the terminal is up to date.
- [x] Before any transaction is performed, an activation must take place. It can be done via settings or by running `HipsUi.activateTerminal()`.
- [x] Before activation can take place, the device must be bluetooth paired.
- [x] Do not delete the app if you have stored offline transactions (`requiresTransactionUpload`) before they are posted to Hips.


# Usage
----
In the example below, the SDK is initialised and setup to receive callbacks

To enable dependency downloads, add the global token to `hipsAuthToken` in `gradle.properties`. Please ask your account manager or Hips Support for the hipsAuthToken.

- Add the token to $HOME/.gradle/gradle.properties or $PROJECT_ROOT/gradle.properties
```
hipsAuthToken=AUTHENTICATION_TOKEN
```
In your build root folder, add:

```kotlin
allprojects {
    repositories {
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
    hipsUi.registerCallback(callbackManager, object : HipsUiCallback<HipsResult> {
            override fun onResult(hipsResult: HipsResult) {
                when (hipsResult) {
                    is HipsResult.Transaction -> {
                        Log.v(TAG, "onResult: ${hipsResult.hipsTransactionResult}")
                    }
                    is HipsResult.NonPayment -> {
                        Log.v(TAG, "onResult: ${hipsResult.hipsNonPaymentMagSwipeResult}")
                    }
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
    hipsUi.registerCallback(callbackManager, new HipsUiCallback<HipsResult>() {
        @Override
        public void onResult(@NotNull HipsResult hipsResult) {
            if (hipsResult instanceof HipsResult.Transaction) {
                HipsTransactionResult hipsTransactionResult = ((HipsResult.Transaction) hipsResult).getHipsTransactionResult();
                if (hipsTransactionResult.getTransactionApproved()) {
                    hipsResultText.setText("Approved!");
                } else {
                    hipsResultText.setText("Declined!");
                }
            }

            if (hipsResult instanceof HipsResult.NonPayment) {
                HipsNonPaymentMagSwipeResult hipsNonPaymentMagSwipeResult = ((HipsResult.NonPayment) hipsResult).getHipsNonPaymentMagSwipeResult();
                Log.v("MagSwipeResult Track 1", hipsNonPaymentMagSwipeResult.getTrack1());
                Log.v("MagSwipeResult Track 2", hipsNonPaymentMagSwipeResult.getTrack2());
                Log.v("MagSwipeResult Track 3", hipsNonPaymentMagSwipeResult.getTrack3());
            }
        }
        @Override
        public void onError(@NotNull String errorCode, @org.jetbrains.annotations.Nullable String errorMessage) {
            hipsResultText.setText(errorCode + ":  " + errorMessage);
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
            "1234567890", // reference (ID that will follow the transactions in reports)
            null, // cashierToken, Optional
            null, // metadata1, Optional, any data you want to pass on with the transaction, example "&id=1&color=blue"
            null, // metadata2, Optional
            null, // webHook, Optional, must be formatted with "https://" if included
            "SEK", // currencyIso
            TipFlowType.TOP, // tipFlowType
            TransactionType.PURCHASE, // transactionType
            false, // isOfflinePayment
            true // isTestMode
    );
    
    // Unregister CallbackManager
    @Override
    protected void onDestroyView() {
        super.onDestroyView();
        hipsUi.unregisterCallback(callbackManager);
    }
```
## Hips Settings
The SDK provides a UI to handle terminal settings. Launch Hips Settings by calling `hipsUi.openSettings()`.

#### Select Default terminal
Make sure you have paired your Terminal with your Android device. You do this via your Bluetooth settings. IMPORTANT: Your can only see paired terminals in Hips Settings Device selection

Once your `Default Device` is saved, the SDK will make all connections to this device until the `Default Device` is changed. This allows you to have multiple terminals paired.

#### Activate terminal

Following are the steps to activate a terminal on a device.  
IMPORTANT: Make sure the terminal has external power and is recently rebooted before continuing with activation.

1. Launch Hips Settings and press ACTIVATE
2. If your terminal was pre-added in your merchant account, SKIP step 3, 4 and 5.
3. Upon receiving receiving your activation code, add it with you merchant account on [https://activate.hips.com](https://activate.hips.com)
4. Follow the instructions on hips.com and activate your terminal
5. Return to your app and the terminal will continue with the activation process
6. Once the activation completes, your device will be able to make authorized request to Hips API

#### Inject keys

Add encryption keys to your terminal
IMPORTANT: Make sure the terminal has external power and is recently rebooted before continuing with injection.

1. Launch Hips Settings and press INJECT KEYS

#### Parameter updates

Update terminal with new software, parameters and merchant settings.
IMPORTANT: Make sure the terminal has external power and is recently rebooted before continuing with updates.

1. Launch Hips Settings and press UPDATES AVAILABLE / CHECK UPDATES

#### Forget terminal

To deactivate your device, follow the instructions on Hips Settings by selecting Forget terminal.

IMPORTANT: You will not be able to make any authorized requests after this action. A new activation process must be launched, read above.


## Make EMV Payment

The SDK interacts by receiving and returning Request and Result types.

- Requires: `Default Device`, `TerminalApiKeyAuth`
- Request: `HipsTransactionRequest.Payment`
- Result: `HipsResult.Transaction.HipsTransactionResult`

To make a new payment, create your `HipsTransactionRequest.Payment` body.

| Parameter          | Description                                                                                                                                                                                                                                                                                                                                       | Type     |
|:-------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------|
| `amountInCents`    | This is the amount with vat/tax, but without tip and cashback. We express amounts in minor units according to the ISO 4217 standard. That means they are expressed in the smallest unit of currency. Examples are USD with 1000 representing $10, GBP with 500 representing £5, EUR with 50 representing €0.50 and SEK with 100 representing 1kr. |          |
| `cashbackInCents`  | This is the cashback amount.                                                                                                                                                                                                                                                                                                                      |          |
| `cashierToken`     | This field is reserved for HIPS                                                                                                                                                                                                                                                                                                                   | Optional |
| `currencyIso`      | We use the ISO 4217 standard for defining currencies.                                                                                                                                                                                                                                                                                             |          |
| `employeeNumber`   | This is a reference so you know which employee the tips belongs to or who initiated the transaction.                                                                                                                                                                                                                                              |          |
| `isOfflinePayment` | By enabling offline payments, the SDK will store the transactions until uploaded to HIPS servers. If network connection is available, the SDK will attempt to upload the offline transaction batch will automatically. To do this manually, read section **Offline Batch upload via SDK API**                                                     |          |
| `isTestMode`       | LIVE or TEST mode selected                                                                                                                                                                                                                                                                                                                        |          |
| `metadata1`        | Your metadata 1 for order (max 255 characters)                                                                                                                                                                                                                                                                                                    | Optional |
| `metadata2`        | Your metadata 2 for order (max 255 characters)                                                                                                                                                                                                                                                                                                    | Optional |
| `reference`        | Your reference for this transaction. This reference will pass through in the transaction chain all the way to the card issuer.                                                                                                                                                                                                                    |          |
| `tipFlowType`      | Select gratuity type; NONE, ASK, TOP, TOP_CENT                                                                                                                                                                                                                                                                                                    |          |
| `transactionType`  | Select transaction type; PURCHASE, PREAUTHORIZATION, CAPTURE                                                                                                                                                                                                                                                                                      |          |
| `vatInCents`       | This is the vat/tax amount. This amount is part of net_amount.                                                                                                                                                                                                                                                                                    |          |
| `webHook`          | URL - Webhook URL where HIPS will post all events related to this order                                                                                                                                                                                                                                                                           | Optional |

#### Payment Requests

Pass your `HipsTransactionRequest.Payment` along with your activity or fragment to `hipsUi.startSession()` to start a new HipsUI Payment session.
```kotlin
    val hipsTransactionRequest = HipsTransactionRequest.Payment(
                amountInCents = 100,
                currencyIso = "SEK",
                reference = "This is a test payment",
                tipFlowType = TipFlowType.NONE,
                transactionType = TransactionType.PURCHASE,
                isOfflinePayment = false,
                isTestMode = true
            )

    hipsUi.startSession(
        hipsTransactionRequest = hipsTransactionRequest,
        requestCode = 12345,
        fragment = this
    )
```

#### Payment Results
A payment session always completes by returning `HipsResult.Transaction`.  
Check status for approved or declined transactions in `HipsTransactionResult`, all available parameters are listed below:

| Parameter                   | Description                                                                                                                                                                                                                                                                                                     | Type |
|:----------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-----|
| `aid`                       | Application Identifier (AID) – terminal. Identifies the application as described in ISO/IEC 7816-5                                                                                                                                                                                                              |      |
| `amountCashback`            | This is the cashback amount. We express amounts in minor units according to the ISO 4217 standard. That means they are expressed in the smallest unit of currency. Examples are USD with 1000 representing $10, GBP with 500 representing £5, EUR with 50 representing €0.50 and SEK with 100 representing 1kr. |      |
| `amountCurrencyCode`        | Authorization Amount currency. ISO 4217 standard for defining currencies.                                                                                                                                                                                                                                       |      |
| `amountGratuity`            | Tip amount, if any. We express amounts in minor units according to the ISO 4217 standard.                                                                                                                                                                                                                       |      |
| `amountTransaction`         | Authorization amount. We express amounts in minor units according to the ISO 4217 standard.                                                                                                                                                                                                                     |      |
| `authorizationCode`         | Card authorization code                                                                                                                                                                                                                                                                                         |      |
| `authorizationMethod`       | ONLINE or OFFLINE                                                                                                                                                                                                                                                                                               |      |
| `createdAt`                 | DateTime when this transaction was created in the system                                                                                                                                                                                                                                                        |      |
| `errorCode`                 | Reason code for declines (see reference)                                                                                                                                                                                                                                                                        |      |
| `errorMessage`              | Reason message                                                                                                                                                                                                                                                                                                  |      |
| `merchantAddressLine1`      | Merchant location street address line 1                                                                                                                                                                                                                                                                         |      |
| `merchantAddressLine2`      | Merchant location street address line 2                                                                                                                                                                                                                                                                         |      |
| `merchantCity`              | Merchant location City                                                                                                                                                                                                                                                                                          |      |
| `merchantCompanyNumber`     | Merchant Legal Business Name                                                                                                                                                                                                                                                                                                                |      |
| `merchantCountry`           | Merchant location country code. ISO 3166-1 Alpha-2                                                                                                                                                                                                                                                              |      |
| `merchantId`                | Merchant ID for the merchant                                                                                                                                                                                                                                                                                    |      |
| `merchantLatitude`          | Latitude                                                                                                                                                                                                                                                                                                        |      |
| `merchantLongitude`         | Longitude                                                                                                                                                                                                                                                                                                       |      |
| `merchantName`              | Merchant Legal Business Name                                                                                                                                                                                                                                                                                    |      |
| `merchantPhone`             | Merchant location phone number in international format                                                                                                                                                                                                                                                          |      |
| `merchantRegion`            | Merchant region                                                                                                                                                                                                                                                                                                 |      |
| `merchantTaxVatNumber`      | Merchant V.A.T or Tax ID                                                                                                                                                                                                                                                                                        |      |
| `receiptData`               | Formatted receipt data                                                                                                                                                                                                                                                                                          |      |
| `receiptInfo`               | Application Cryptogram - Cryptogram returned by the ICC in response of the GENERATE AC command                                                                                                                                                                                                                  |      |
| `requiresParameterDownload` | Required terminal parameters are available for download                                                                                                                                                                                                                                                         |      |
| `requiresTransactionUpload` | Offline transactions are batched and ready for upload                                                                                                                                                                                                                                                           |      |
| `responder`                 | HIPS or SDK response code                                                                                                                                                                                                                                                                                       |      |
| `sdkCode`                   | Internal SDK code                                                                                                                                                                                                                                                                                               |      |
| `softwareVersion`           | SDK version                                                                                                                                                                                                                                                                                                     |      |
| `source`                    | Funding source used. Can be card, invoice, part_payment, swift, bitcoin, swish or paypal                                                                                                                                                                                                                        |      |
| `sourceAccountMasked`       | Masked Card PAN                                                                                                                                                                                                                                                                                                 |      |
| `sourceApplicationName`     | Application Label - Mnemonic associated with the AID according to ISO/IEC 7816-5                                                                                                                                                                                                                                |      |
| `sourceMethod`              | Transaction Source Method - can be purchase, refund, chargeback, credit, deprecated_void, chargeback_representation                                                                                                                                                                                             |      |
| `sourceName`                | Card Holder Name                                                                                                                                                                                                                                                                                                |      |
| `sourceScheme`              | Transaction Source Scheme - can be visa, mastercard, amex, unionpay, etc                                                                                                                                                                                                                                        |      |
| `statusCode`                | Transaction Status Code - according to DE 39 in ISO 8583. For all successful transactions the response code is set to ‘00’. All other response codes indicate an error condition.                                                                                                                               |      |
| `taxVat`                    | Vat/tax amount                                                                                                                                                                                                                                                                                                  |      |
| `terminalID`                | Terminal ID                                                                                                                                                                                                                                                                                                     |      |
| `test`                      | Test mode enabled                                                                                                                                                                                                                                                                                               |      |
| `transactionApproved`       | True if EMV status is SUCCESSFUL or AUTHORIZED                                                                                                                                                                                                                                                                  |      |
| `transactionCancelled`      | True if user canceled                                                                                                                                                                                                                                                                                           |      |
| `transactionID`             | Payment ID (store this for later referral to this payment. i.e for `HipsTransactionRequest.Capture` or `HipsTransactionRequest.Refund`                                                                                                                                                                          |      |
| `transactionShortId`        | ID (unique for merchant) for this order, also known as Humanized Token                                                                                                                                                                                                                                          |      |
| `transactionStatus`         | SUCCESSFUL or FAILED                                                                                                                                                                                                                                                                                            |      |
| `transactionType`           | HipsTransactionRequest type used                                                                                                                                                                                                                                                                                |      |
| `tsi`                       | Transaction Status Info                                                                                                                                                                                                                                                                                         |      |
| `tvr`                       | Terminal Verification Result                                                                                                                                                                                                                                                                                    |      |
| `verificationMethod`        | Transaction CVM                                                                                                                                                                                                                                                                                                 |      |
```kotlin
    
    hipsUi.registerCallback(callbackManager, object : HipsUiCallback<HipsResult> {
            override fun onResult(hipsResult: HipsResult) {
                when (hipsResult) {
                    is HipsResult.Transaction -> {
                        Log.v(TAG, "onResult: ${hipsResult.hipsTransactionResult}")
                    }
                }
            }

        override fun onError(errorCode: String, errorMessage: String?) {
            hipsTransactionResultText.text = "$errorCode: $errorMessage"
        }
    })
```
## Refund Payment

The SDK interacts by receiving and returning Request and Result types.

- Requires: `Default Device`, `TerminalApiKeyAuth`
- Request: `HipsTransactionRequest.Refund`
- Result: `HipsResult.Transaction.HipsTransactionResult`

To make a new Refund, create your `HipsTransactionRequest.Refund` body.

> A refund on an authorized payment (not captured) will result in an automatic reversal/void of the whole authorization. Also note that on POS transactions, all transactions (even purchases marked with direct capture) will be in `authorized` state for 10 minutes before they move over to `successful` state (captured). Should you do a refund during this 10 minute period, the authorization will be voided.
> You can refund a maximum amount of the original transaction. If you don't specify the amount; the whole transaction will be refunded.
> You can only refund if there are funds available on your merchant account.

| Parameter       | Description                                                                                                                                                                                                                                                                                                                                       | Type |
|:----------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-----|
| `amountInCents` | This is the amount with vat/tax, but without tip and cashback. We express amounts in minor units according to the ISO 4217 standard. That means they are expressed in the smallest unit of currency. Examples are USD with 1000 representing $10, GBP with 500 representing £5, EUR with 50 representing €0.50 and SEK with 100 representing 1kr. |      |
| `transactionId` | Payment ID, received from a Payment request                                                                                                                                                                                                                                                                                                       |      |
| `isTestMode`    | Test mode enabled                                                                                                                                                                                                                                                                                                                                 |      |

#### Refund Requests

Pass your `HipsTransactionRequest.Refund` along with your activity or fragment to `hipsUi.startSession()` to start a new HipsUI Refund session.
```kotlin
    val hipsTransactionRequest = HipsTransactionRequest.Refund(
                amountInCents = 100,
                transactionId = "1234567890",
                isTestMode = true
            )

    hipsUi.startSession(
        hipsTransactionRequest = hipsTransactionRequest,
        requestCode = 12345,
        fragment = this
    )
```

#### Refund Results
A Refund session always completes by returning `HipsResult.Transaction`.  
Check status for approved or declined transactions in `HipsTransactionResult`, all available parameters and results are listed above under Payment.

## Capture Payment

The SDK interacts by receiving and returning Request and Result types.

- Requires: `Default Device`, `TerminalApiKeyAuth`
- Request: `HipsTransactionRequest.Capture`
- Result: `HipsResult.Transaction.HipsTransactionResult`

To make a new payment, create your `HipsTransactionRequest.Capture` body.

> You can capture a maximum amount of the original transaction. If you want to capture a higher amount than the authorized amount; then we recommend you to refund (reverse) the original authorization and make a new authorization with the higher amount. Incremental authorizations are not yet supported byt the SDK.


| Parameter       | Description                                                                                                                                                                                                                                                                                                                                       | Type |
|:----------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-----|
| `amountInCents` | This is the amount with vat/tax, but without tip and cashback. We express amounts in minor units according to the ISO 4217 standard. That means they are expressed in the smallest unit of currency. Examples are USD with 1000 representing $10, GBP with 500 representing £5, EUR with 50 representing €0.50 and SEK with 100 representing 1kr. |      |
| `transactionId` | Payment ID, received from a Payment request                                                                                                                                                                                                                                                                                                                                                   |      |
| `isTestMode`    | Test mode enabled                                                                                                                                                                                                                                                                                                                                 |      |

#### Capture Requests

Pass your `HipsTransactionRequest.Capture` along with your activity or fragment to `hipsUi.startSession()` to start a new HipsUI Payment session.
```kotlin
    val hipsTransactionRequest = HipsTransactionRequest.Capture(
                amountInCents = 100,
                transactionId = "1234567890",
                isTestMode = true
            )

    hipsUi.startSession(
        hipsTransactionRequest = hipsTransactionRequest,
        requestCode = 12345,
        fragment = this
    )
```

#### Capture Results
A Capture session always completes by returning `HipsResult.Transaction`.  
Check status for approved or declined transactions in `HipsTransactionResult`, all available parameters and results are listed above under Payment.

## Make Non Payments - Loyalty cards

Launch a mag swipe session by calling `hipsUi.startNonPaymentRequest()`. Provide a text string to display on your terminal.
The SDK interacts by receiving and returning Request and Result types.



> ### **IMPORTANT!**
> The BIN (first 6 digits) of the non-payment card that you want to read via this function must be pre-registered as a non-payment BIN. 
> Non registered BINs will not return any track data. To register a non-payment BIN, please email a proof that this BIN is owned by you to support@hips.com

- Requires: `Default Device`
- Request: `HipsNonPaymentRequest.MagSwipe`
- Result: `HipsResult.NonPayment.HipsNonPaymentMagSwipeResult`

#### Non Payment Mag Swipe Requests
```kotlin
    hipsUi.startNonPaymentRequest(
        hipsNonPaymentRequest = HipsNonPaymentRequest.MagSwipe(
            displayText = "Please swipe your bonus card"
        ),
        requestCode = 12345,
        fragment = this
    )
```

#### Non Payment Mag Swipe Results
A payment session always completes by returning `HipsResult.NonPayment`.  
Check status for approved or declined transactions in `HipsNonPaymentMagSwipeResult`, all available parameters are listed below:

| Parameter         | Description                                              | Type |
|:------------------|:---------------------------------------------------------|:-----|
| `createdAt`       | DateTime when this transaction was created in the system |      |
| `errorCode`       | Reason code for declines (see reference)                 |      |
| `errorMessage`    | Reason message                                           |      |
| `softwareVersion` | SDK version                                              |      |
| `track1`          | Magnetic Stripe Track 1                                  |      |
| `track2`          | Magnetic Stripe Track 2                                  |      |
| `track3`          | Magnetic Stripe Track 3                                  |      |

```kotlin
    
    hipsUi.registerCallback(callbackManager, object : HipsUiCallback<HipsResult> {
            override fun onResult(hipsResult: HipsResult) {
                when (hipsResult) {
                    is HipsResult.NonPayment -> {
                        when (val nonPaymentResult = hipsResult.hipsNonPaymentResult) {
                            is HipsNonPaymentResult.MagSwipe -> {
                                Log.v(TAG, "onResult: ${nonPaymentResult.hipsNonPaymentMagSwipeResult}")
                            }
                        }
                    }
                }
            }

        override fun onError(errorCode: String, errorMessage: String?) {
            hipsTransactionResultText.text = "$errorCode: $errorMessage"
        }
    })
```
## Offline Batch Upload via HipsUI API

Trigger manual offline batch uploads by in invoking `HipsUi.startOfflineBatchUpload()`. This will upload any existing offline transactions stored in the SDK
- Requires: `TerminalApiKeyAuth`
- Result: `HipsResult.OfflineBatch.HipsOfflineBatchResult`

| Parameter        | Description                       | Type |
|:-----------------|:----------------------------------|:-----|
| `accepted`       | List of accepted `transactionID`s |      |
| `rejected`       | List of rejected `transactionID`s |      |
| `accepted_count` | Number of accepted transactions   |      |
| `rejected_count` | Number of rejected transactions   |      |

#### Trigger Offline Batch Upload
```kotlin
    hipsUi.startOfflineBatchUpload(
        requestCode = 1345,
        fragment = this
    )
```
#### Offline Batch Upload Results
```kotlin
    
    hipsUi.registerCallback(callbackManager, object : HipsUiCallback<HipsResult> {
            override fun onResult(hipsResult: HipsResult) {
                when (hipsResult) {
                    is HipsResult.OfflineBatch -> {
                        Log.v(TAG, "onResult: ${hipsResult.hipsOfflineBatchResult}")
                    }
                }
            }

        override fun onError(errorCode: String, errorMessage: String?) {
            hipsTransactionResultText.text = "$errorCode: $errorMessage"
        }
    })
```

## Terminal Activation via HipsUI API

Launch Activation UI by in invoking `HipsUi.activateTerminal()`. Select a paired device and start the activation process.  Read more about the steps involved in section **Hips Settings - Activate terminal**

- Result: `HipsResult.Activation.HipsActivationResult`

| Parameter   | Description                                   | Type |
|:------------|:----------------------------------------------|:-----|
| `authToken` | Authentication Key Type: `TerminalApiKeyAuth` |      |

#### Trigger Terminal Activation
```kotlin
    hipsUi.activateTerminal(
        requestCode = 1345,
        fragment = this
    )
```
#### Terminal Activation results
```kotlin
    
    hipsUi.registerCallback(callbackManager, object : HipsUiCallback<HipsResult> {
            override fun onResult(hipsResult: HipsResult) {
                when (hipsResult) {
                    is HipsResult.Activation -> {
                        Log.v(TAG, "onResult: ${hipsResult.hipsActivationResult.authToken}")
                    }
                }
            }

        override fun onError(errorCode: String, errorMessage: String?) {
            hipsTransactionResultText.text = "$errorCode: $errorMessage"
        }
    })
```

## Terminal Parameter Update via HipsUI API

Launch Parameter Update UI by in invoking `HipsUi.updateTerminal()`. The SDK will attempt to connect to your `Default Device`.  Read more about the steps involved in section **Hips Settings - Parameter updates**

- Requires: `Default Device`, `TerminalApiKeyAuth`
- Result: `HipsResult.ParamsUpdate.HipsParamsUpdateResult`

| Parameter      | Description                                        | Type |
|:---------------|:---------------------------------------------------|:-----|
| `isSuccessful` | Flag for successful or updated terminal parameters |      |

#### Trigger Terminal Parameter Update
```kotlin
    hipsUi.updateTerminal(
        requestCode = 1345,
        fragment = this
    )
```
#### Terminal Parameter Update results
```kotlin
    
    hipsUi.registerCallback(callbackManager, object : HipsUiCallback<HipsResult> {
            override fun onResult(hipsResult: HipsResult) {
                when (hipsResult) {
                    is HipsResult.ParamsUpdate -> {
                        Log.v(TAG, "onResult: ${hipsResult.hipsParamsUpdateResult.isSuccessful}")
                    }
                }
            }

        override fun onError(errorCode: String, errorMessage: String?) {
            hipsTransactionResultText.text = "$errorCode: $errorMessage"
        }
    })
```

## Response, Decline and Error Codes
Find all response codes at [Hips Docs](https://docs.hips.com/reference#errors)
