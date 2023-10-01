GooglePay


Google Pay for Payments
Android
Manage integrations in console

GooglePay
Overview
Payments
More
Home
Guides
Reference
Samples
Support
Wallet
Pakistan
More
Payment Card Recognition
Spot Platform
Get started
Overview
Guides
Setup
Tutorial
Brand guidelines
UX best practices
Test and deploy
Integration checklist
Request production access
Deploy your application
Integration recipes
Pay from a notification
Resources
Strong Customer Authentication
Update to new payment button
Payment data cryptography
Test with sample credit cards
Test with sample tokens
Troubleshooting
Prepare for Google Play data disclosure requirements
Update to latest version
Payment button static assets
Home
Products
Google Pay
Google Pay for Payments
Android
Was this helpful?

Tutorialbookmark_border
On this page
Step 1: Define your Google Pay API version
Step 2: Request a payment token for your payment provider
Step 3: Define supported payment card networks
Step 4: Describe your allowed payment methods
Step 5: Create a PaymentsClient instance

If your Android application is distributed through the Google Play store, you can integrate it with the Google Pay API. You can also configure it to accept payment cards. To integrate your application and configure it to accept payment cards, follow the steps in this tutorial.

Step 1: Define your Google Pay API version.
Step 2: Request a payment token for your payment provider.
Step 3: Define supported payment card networks.
Step 4: Describe your allowed payment methods.
Step 5: Create a PaymentsClient instance.
Step 6: Determine readiness to pay with the Google Pay API.
Step 7: Add a Google Pay payment button.
Step 8: Create a PaymentDataRequest object.
Step 9: Register event handler for user gesture.
Step 10: Handle the response object.
Put it all together.
Step 1: Define your Google Pay API version
Declare the version of the Google Pay API that your application uses. The major and minor versions affect the fields expected in each passed object and are included in the response.

Create a base request object that contains properties that are present in all other request objects.

KotlinJava

    private val baseRequest = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
    }
PaymentsUtil.kt

Step 2: Request a payment token for your payment provider
Google encrypts information about a payer's selected card for secure processing by a payment provider.

KotlinJava

private fun gatewayTokenizationSpecification(): JSONObject {
    return JSONObject().apply {
        put("type", "PAYMENT_GATEWAY")
        put("parameters", JSONObject(mapOf(
                "gateway" to "example",
                "gatewayMerchantId" to "exampleGatewayMerchantId")))
    }
}
Replace example and exampleGatewayMerchantId with the appropriate values for your payment provider. Use the following table to find the specific gateway and gatewayMerchantId values for your payment provider:

Gateway	Parameters and documents
accept.blue	

  "gateway": "acceptblue"
  "gatewayMerchantId": "YOUR_GATEWAY_MERCHANT_ID"
Developer docs

ACI	

  "gateway": "aciworldwide"
  "gatewayMerchantId": "YOUR_ENTITY_ID"
Developer docs

ACpay	

  "gateway": "acpay"
  "gatewayMerchantId": "YOUR_GATEWAY_MERCHANT_ID"
Developer docs

Acquired.com	

  "gateway": "acquired"
  "gatewayMerchantId": "YOUR_GATEWAY_MERCHANT_ID"
Developer docs

Adyen	

  "gateway": "adyen"
  "gatewayMerchantId": "YOUR_MERCHANT_ACCOUNT_NAME"
Developer docs


12345…69

Tip: example is a valid gateway name in the test environment. If you want to try out the API, you can leave the code sample above as-is. When you use the example gateway, your site doesn't receive card data capable of a transaction, but the user flow looks the same.
The PAYMENT_GATEWAY tokenization type is the most common merchant implementation of the card payment method in the Google Pay API. If your payment provider isn't supported, you might be able to accept Google Pay by a DIRECT integration. For more information, see the Direct tokenization documentation.

Step 3: Define supported payment card networks
Define the card networks that your application accepts.

KotlinJava

private val allowedCardNetworks = JSONArray(listOf(
        "AMEX",
        "DISCOVER",
        "INTERAC",
        "JCB",
        "MASTERCARD",
        "VISA"))
The Google Pay API might return cards on file on Google.com (PAN_ONLY) or a device token on an Android-powered device authenticated with a 3-D Secure cryptogram (CRYPTOGRAM_3DS). You can control whether both methods or just one of them can be returned.

Before you set allowedCardAuthMethods, confirm with your payment processor and acquirer on whether device tokens (CRYPTOGRAM_3DS) are supported in your region.

KotlinJava

private val allowedCardAuthMethods = JSONArray(listOf(
        "PAN_ONLY",
        "CRYPTOGRAM_3DS"))
For more information, see CardParameters in our JSON object reference. For Android device tokens support, check with your gateway or processor for the card networks supported.

Important:
Make sure that you also apply your existing risk checks and controls for payment transactions to Google Pay transactions. Google Pay validation and fraud checks aren't intended to replace your risk management processes.
If you support PAN_ONLY, ensure that you trigger 3D Secure (step-up authentication) for Google Pay PAN PAN_ONLY transactions in the same way that you trigger 3D Secure for normal card transactions.
Step 4: Describe your allowed payment methods
In order to describe your application's support for the CARD payment method, combine your supported authentication methods and supported card networks.

KotlinJava

    private fun baseCardPaymentMethod(): JSONObject {
        return JSONObject().apply {

            val parameters = JSONObject().apply {
                put("allowedAuthMethods", allowedCardAuthMethods)
                put("allowedCardNetworks", allowedCardNetworks)
                put("billingAddressRequired", true)
                put("billingAddressParameters", JSONObject().apply {
                    put("format", "FULL")
                })
            }

            put("type", "CARD")
            put("parameters", parameters)
        }
    }
PaymentsUtil.kt

Extend the base card payment method object to describe information expected to be returned to your application, which must include tokenized payment data.

KotlinJava

    private fun cardPaymentMethod(): JSONObject {
        val cardPaymentMethod = baseCardPaymentMethod()
        cardPaymentMethod.put("tokenizationSpecification", gatewayTokenizationSpecification())

        return cardPaymentMethod
    }
PaymentsUtil.kt

For more information about supported parameters, see CardParameters in our JSON object reference.

CARD is currently the only supported payment method.

Step 5: Create a PaymentsClient instance
Create a PaymentsClient instance. The PaymentsClient is used for interaction with the Google Pay API.

KotlinJava

    fun createPaymentsClient(activity: Activity): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
                .setEnvironment(Constants.PAYMENTS_ENVIRONMENT)
                .build()

        return Wallet.getPaymentsClient(activity, walletOptions)
    }
PaymentsUtil.kt

Step 6: Determine readiness to pay with the Google Pay API
Add your allowed payment methods to your base request object with the following code snippet:

KotlinJava

    fun isReadyToPayRequest(): JSONObject? {
        return try {
            baseRequest.apply {
                put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod()))
            }

        } catch (e: JSONException) {
            null
        }
    }
PaymentsUtil.kt

Before you display the Google Pay button, call the isReadyToPay API to determine if the user can make payments with the Google Pay API. For a full list of configuration properties, see the IsReadyToPayRequest JSON object documentation.

KotlinJava

    private fun possiblyShowGooglePayButton() {

        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest() ?: return
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString()) ?: return

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            try {
                completedTask.getResult(ApiException::class.java)?.let(::setGooglePayAvailable)
            } catch (exception: ApiException) {
                // Process error
                Log.w("isReadyToPay failed", exception)
            }
        }
    }
CheckoutActivity.kt


GooglePay


Google Pay for 

GooglePay


Google Pay for Payments
Android
Manage integrations in console

GooglePay
Overview
Payments
More
Home
Guides
Reference
Samples
Support
Wallet
Pakistan
More
Payment Card Recognition
Spot Platform
Get started
Overview
Guides
Setup
Tutorial
Brand guidelines
UX best practices
Test and deploy
Integration checklist
Request production access
Deploy your application
Integration recipes
Pay from a notification
Resources
Strong Customer Authentication
Update to new payment button
Payment data cryptography
Test with sample credit cards
Test with sample tokens
Troubleshooting
Prepare for Google Play data disclosure requirements
Update to latest version
Payment button static assets
Home
Products
Google Pay
Google Pay for Payments
Android
Was this helpful?

Tutorialbookmark_border
On this page
Step 1: Define your Google Pay API version
Step 2: Request a payment token for your payment provider
Step 3: Define supported payment card networks
Step 4: Describe your allowed payment methods
Step 5: Create a PaymentsClient instance

If your Android application is distributed through the Google Play store, you can integrate it with the Google Pay API. You can also configure it to accept payment cards. To integrate your application and configure it to accept payment cards, follow the steps in this tutorial.

Step 1: Define your Google Pay API version.
Step 2: Request a payment token for your payment provider.
Step 3: Define supported payment card networks.
Step 4: Describe your allowed payment methods.
Step 5: Create a PaymentsClient instance.
Step 6: Determine readiness to pay with the Google Pay API.
Step 7: Add a Google Pay payment button.
Step 8: Create a PaymentDataRequest object.
Step 9: Register event handler for user gesture.
Step 10: Handle the response object.
Put it all together.
Step 1: Define your Google Pay API version
Declare the version of the Google Pay API that your application uses. The major and minor versions affect the fields expected in each passed object and are included in the response.

Create a base request object that contains properties that are present in all other request objects.

KotlinJava

    private val baseRequest = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
    }
PaymentsUtil.kt

Step 2: Request a payment token for your payment provider
Google encrypts information about a payer's selected card for secure processing by a payment provider.

KotlinJava

private fun gatewayTokenizationSpecification(): JSONObject {
    return JSONObject().apply {
        put("type", "PAYMENT_GATEWAY")
        put("parameters", JSONObject(mapOf(
                "gateway" to "example",
                "gatewayMerchantId" to "exampleGatewayMerchantId")))
    }
}
Replace example and exampleGatewayMerchantId with the appropriate values for your payment provider. Use the following table to find the specific gateway and gatewayMerchantId values for your payment provider:

Gateway	Parameters and documents
accept.blue	

  "gateway": "acceptblue"
  "gatewayMerchantId": "YOUR_GATEWAY_MERCHANT_ID"
Developer docs

ACI	

  "gateway": "aciworldwide"
  "gatewayMerchantId": "YOUR_ENTITY_ID"
Developer docs

ACpay	

  "gateway": "acpay"
  "gatewayMerchantId": "YOUR_GATEWAY_MERCHANT_ID"
Developer docs

Acquired.com	

  "gateway": "acquired"
  "gatewayMerchantId": "YOUR_GATEWAY_MERCHANT_ID"
Developer docs

Adyen	

  "gateway": "adyen"
  "gatewayMerchantId": "YOUR_MERCHANT_ACCOUNT_NAME"
Developer docs


12345…69

Tip: example is a valid gateway name in the test environment. If you want to try out the API, you can leave the code sample above as-is. When you use the example gateway, your site doesn't receive card data capable of a transaction, but the user flow looks the same.
The PAYMENT_GATEWAY tokenization type is the most common merchant implementation of the card payment method in the Google Pay API. If your payment provider isn't supported, you might be able to accept Google Pay by a DIRECT integration. For more information, see the Direct tokenization documentation.

Step 3: Define supported payment card networks
Define the card networks that your application accepts.

KotlinJava

private val allowedCardNetworks = JSONArray(listOf(
        "AMEX",
        "DISCOVER",
        "INTERAC",
        "JCB",
        "MASTERCARD",
        "VISA"))
The Google Pay API might return cards on file on Google.com (PAN_ONLY) or a device token on an Android-powered device authenticated with a 3-D Secure cryptogram (CRYPTOGRAM_3DS). You can control whether both methods or just one of them can be returned.

Before you set allowedCardAuthMethods, confirm with your payment processor and acquirer on whether device tokens (CRYPTOGRAM_3DS) are supported in your region.

KotlinJava

private val allowedCardAuthMethods = JSONArray(listOf(
        "PAN_ONLY",
        "CRYPTOGRAM_3DS"))
For more information, see CardParameters in our JSON object reference. For Android device tokens support, check with your gateway or processor for the card networks supported.

Important:
Make sure that you also apply your existing risk checks and controls for payment transactions to Google Pay transactions. Google Pay validation and fraud checks aren't intended to replace your risk management processes.
If you support PAN_ONLY, ensure that you trigger 3D Secure (step-up authentication) for Google Pay PAN PAN_ONLY transactions in the same way that you trigger 3D Secure for normal card transactions.
Step 4: Describe your allowed payment methods
In order to describe your application's support for the CARD payment method, combine your supported authentication methods and supported card networks.

KotlinJava

    private fun baseCardPaymentMethod(): JSONObject {
        return JSONObject().apply {

            val parameters = JSONObject().apply {
                put("allowedAuthMethods", allowedCardAuthMethods)
                put("allowedCardNetworks", allowedCardNetworks)
                put("billingAddressRequired", true)
                put("billingAddressParameters", JSONObject().apply {
                    put("format", "FULL")
                })
            }

            put("type", "CARD")
            put("parameters", parameters)
        }
    }
PaymentsUtil.kt

Extend the base card payment method object to describe information expected to be returned to your application, which must include tokenized payment data.

KotlinJava

    private fun cardPaymentMethod(): JSONObject {
        val cardPaymentMethod = baseCardPaymentMethod()
        cardPaymentMethod.put("tokenizationSpecification", gatewayTokenizationSpecification())

        return cardPaymentMethod
    }
PaymentsUtil.kt

For more information about supported parameters, see CardParameters in our JSON object reference.

CARD is currently the only supported payment method.

Step 5: Create a PaymentsClient instance
Create a PaymentsClient instance. The PaymentsClient is used for interaction with the Google Pay API.

KotlinJava

    fun createPaymentsClient(activity: Activity): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
                .setEnvironment(Constants.PAYMENTS_ENVIRONMENT)
                .build()

        return Wallet.getPaymentsClient(activity, walletOptions)
    }
PaymentsUtil.kt

Step 6: Determine readiness to pay with the Google Pay API
Add your allowed payment methods to your base request object with the following code snippet:

KotlinJava

    fun isReadyToPayRequest(): JSONObject? {
        return try {
            baseRequest.apply {
                put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod()))
            }

        } catch (e: JSONException) {
            null
        }
    }
PaymentsUtil.kt

Before you display the Google Pay button, call the isReadyToPay API to determine if the user can make payments with the Google Pay API. For a full list of configuration properties, see the IsReadyToPayRequest JSON object documentation.

KotlinJava

    private fun possiblyShowGooglePayButton() {

        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest() ?: return
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString()) ?: return

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            try {
                completedTask.getResult(ApiException::class.java)?.let(::setGooglePayAvailable)
            } catch (exception: ApiException) {
                // Process error
                Log.w("isReadyToPay failed", exception)
            }
        }
    }
CheckoutActivity.kt



RizqPay
GooglePay


Google Pay for Payments
Android
Managed integrations in console

Pay
Overview
Payments
More
Home
Guides
Reference
Samples
Support
Wallet
Pakistan
More
Payment Card Recognition
Spot Platform
Get started receiving & 
sending Payments
Android
Manage integrations in console

RizqPay
GooglePay
Overview
Payments
More
Home
Guides
Reference
Samples
Support
Wallets
Pakistan
More
Payment Card Recognition
Spot Platform
Get started

Affiliates providing business services
These companies are part of the Google group of companies engaged in the provision of business services in the EU:

Google Austria
Google Belgium
Google Croatia
Google Czech Republic
Google Finland
Google France SARL
Google Germany GMBH
Google Greece EPE
Google Israel
Google Italy
Google Ireland Limited
Google Hungary
Google Lithuania, UAB
Google Netherlands
Google Norway
Google Poland
Google Portugal
Google Romania
Google Turkey
Google Slovakia, s.r.o.
Google Spain
Google Sweden
Google Switzerland
Google UK Ltd
Last update: 10 May 2018


https://rizqshops.com/
payments center

2

Mubashir Ali Mumtaz
arrow_drop_down_black_24dp


Payments users
info

Primary contact
Mubashir Ali Mumtaz
Receives all payments email
nmu094@gmail.com
Users
1
Manage payments users
Privacy settings
Learn more about the information Google shares and why, in the Google Payments Privacy Notice and the Google Pay/Google Payments Terms of Service. You can turn off the sharing below at any time by returning to this page and changing your preferences.
webShare that you have Google Pay with companies outside Google
Allow Google to share that you have a payments profile set up, so businesses you visit can offer Google Pay as a payment option for items you want to purchase.

A site or app you visit might show the Google Pay button during checkout.SHARING IS ON
googleShare the info below between Google Companiesinfo
Google Payment Corp. (GPC) stores and shares information associated with this payment profile (ID:4063-8375-0889) Sam Traders Smc Pvt Ltd Global Merchant Google US

https://myoctocat.com/

On Fri, Sep 29, 2023, 5:52 AM Mubashir Ali Mumtaz <nmu094@gmail.com> wrote:
Affiliates providing business services
These companies are part of the Google group of companies engaged in the provision of business services in the EU:

Google Austria
Google Belgium
Google Croatia
Google Czech Republic
Google Finland
Google France SARL
Google Germany GMBH
Google Greece EPE
Google Israel
Google Italy
Google Ireland Limited
Google Hungary
Google Lithuania, UAB
Google Netherlands
Google Norway
Google Poland
Google Portugal
Google Romania
Google Turkey
Google Slovakia, s.r.o.
Google Spain
Google Sweden
Google Switzerland
Google UK Ltd
Last update: 10 May 2023


https://rizqshops.com/
Sam Traders Smc Pvt Ltd
