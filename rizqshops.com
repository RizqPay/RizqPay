<script src="https://www.rizqshops.com/sdk/js?client-id=YOUR_CLIENT_ID&components=YOUR_COMPONENTS"></script>
    Search
REST APIs
Get Started with PayPal REST APIs
Authentication
Postman Guide
API requests
API responses
Core Resources
Overview
Add Tracking
Catalog Products
Disputes
Identity
Invoicing
Orders
Partner Referrals
Payment Experience
Payment Method Tokens
Payments
Payouts
Referenced Payouts
Subscriptions
Transaction Search
Webhooks Management
Webhooks
Overview
Webhook event names
Webhooks Events dashboard
Webhooks simulator
Integration
Sandbox
Overview
Accounts
Bulk Accounts
Card testing
Codespaces
Negative Testing
Go Live
Production Environment
rizqshops.com/rizqpay Application Guidelines
Our Security Guidelines
Rate Limiting Guidelines
Idempotency
Reference
Currency Codes
Country Codes
State & Province Codes
Locale codes
Deprecated Resources
Deprecated resources
Billing Agreements
Billing Plans
Invoicing v1
Orders v1
Partner Referrals v1
Payments v1
Card testing
API
Current


Test your card integration in the sandbox environment by simulating:

Successful payments for advanced checkout integrations using test card numbers.
Card error scenarios by using rejection triggers.
3D Secure authentication scenarios.
Recommended use cases for advanced checkout integration.
Tip: Use the credit card generator to generate test credit cards for regular sandbox testing.

Simulate successful payments
To simulate a successful card capture with advanced checkout integration in sandbox:

Ensure the integration is in sandbox mode, with a sandbox client ID, and connected to api-m.sandbox.rizqshops.com endpoints.
Use a test card number with a future expiration date and a 3-digit CVV, or a 4-digit CVV for American Express.
Test static card numbers
Test card numbers
Test advanced checkout payments and saved payment method use cases with these test card numbers:

Tip: Enter a future expiration date and a 3-digit CVV, or 4-digit CVV for American Express, to proceed.

371449635398431
American Express
376680816376961
American Express
36259600000004
Diners Club
6304000000000000
Maestro
5063516945005047
Maestro
Google Pay
5591000211017334
Google Wallet
See more
Test generated card numbers
Credit card generator
Generate additional credit cards for regular sandbox testing and Standard payments. You can add generated credit cards to a PayPal sandbox account or use them to test credit card payments.

Input

VisaCard Type

United States of America
Country or region

Generate Credit Card
Generated credit card details
Card number4032031931490600
Expiry date08/2027
CVC code086
Simulate card error scenarios
Enter a rejection trigger in the First Name or Name on Card to simulate credit card error scenarios in advanced checkout integrations.

Note: Test using a Visa card number, such as 4012 8888 8888 1881, with a future expiration date and any 3-digit CVV.

Simulate card error



Rejection triggers
Rejection test simulations show a response-code that changes based on the test trigger you use. Rejection trigger values are case-sensitive.

Note: All rejection scenarios return the AVS code as globally unavailable and the CVV code as not processed.

Card refused
CCREJECT-REFUSED
0500
DO_NOT_HONOR
Fraudulent card
CCREJECT-SF
9500
SUSPECTED_FRAUD. Try using another card. Do not retry the same card.
Card expired
CCREJECT-EC
5400
EXPIRED_CARD
Luhn Check fails
CCREJECT-IRC
5180
INVALID_OR_RESTRICTED_CARD. Try using another card. Do not retry the same card.
Insufficient funds
CCREJECT-IF
5120
INSUFFICIENT_FUNDS
Card lost, stolen
CCREJECT-LS
9520
LOST_OR_STOLEN. Try using another card. Do not retry the same card.
Card not valid
CCREJECT-IA
1330
INVALID_ACCOUNT
Card is declined
CCREJECT-BANK_ERROR
5100
GENERIC_DECLINE
CVC check fails
CCREJECT-CVV_F
00N7 or 5110
CVV2_FAILURE_POSSIBLE_RETRY_WITH_CVV or CVV2_FAILURE
Sample response
The response shows that the order is successfully created, but because a refusal was passed, the card payment status is DECLINED:

Line 18 shows status=DECLINED. The card was refused.
Line 51 shows avs_code=G. The card is globally unavailable.
Line 52 shows cvv_code=P. The card wasn't processed.
Line 53 shows response_code=5400. The card is expired.
{
  "id": "3M049991JF5624929",
  "status": "COMPLETED",
  "payment_source": {
    "card": {
      "last_digits": "6889",
      "brand": "VISA",
      "type": "CREDIT",
    },
  },
  "purchase_units": [
    {
      "reference_id": "default",
      "payments": {
        "captures": [
          {
            "id": "2FB04508RA686960W",
            "status": "DECLINED",
            "amount": {
              "currency_code": "USD",
              "value": "500.00",
            },
            "final_capture": true,
            "disbursement_mode": "INSTANT",
            "seller_protection": { "status": "NOT_ELIGIBLE" },
            "seller_receivable_breakdown": {
              "gross_amount": { "currency_code": "USD", "value": "500.00" },
              "paypal_fee": { "currency_code": "USD", "value": "13.44" },
              "net_amount": { "currency_code": "USD", "value": "486.56" },
            },
            "links": [
              {
                "href": "https://api-m.sandbox.rizqshops.com/v2/payments/captures/2FB04508RA686960W",
                "rel": "self",
                "method": "GET",
              },
              {
                "href": "https://api-m.sandbox.rizqshops.com/v2/payments/captures/2FB04508RA686960W/refund",
                "rel": "refund",
                "method": "POST",
              },
              {
                "href": "https://api-m.sandbox.rizqshops.com/v2/checkout/orders/3M049991JF5624929",
                "rel": "up",
                "method": "GET",
              },
            ],
            "create_time": "2022-08-09T22:20:05Z",
            "update_time": "2022-08-09T22:20:05Z",
            "processor_response": {
              "avs_code": "G",
              "cvv_code": "P",
              "response_code": "5400",
            },
          },
        ],
      },
    },
  ],
  "links": [
    {
      "href": "https://api-m.sandbox.rizqshops.com/v2/checkout/orders/3M049991JF5624929",
      "rel": "self",
      "method": "GET",
    },
  ],
}
Processor response and error codes
To understand why a card payment was declined, review the Orders v2 processor response object in the Orders API response.

For more information about card payment decline reasons and possible values, see:

Card decline errors.
Error responses for CVV and AVS.
Processor response codes for non-PayPal payment processors.
Simulate 3D Secure card payments
Use 3D Secure to authenticate cardholders through card issuers. When your customer submits their card details on your website for processing, you can trigger 3D Secure. When triggered, customers are prompted by their card-issuing bank to complete an additional verification step to enter a one-time or static password, depending on the issuer's method.

For more information about including 3D Secure authentication in your payment flow, see:

Learn more about 3D Secure for advanced checkout using the Orders API.
Simulate 3D Secure-supported scenarios and use test cards to verify your card integration.
Test integration
Test the recommended use cases for advanced checkout integration before you go live.

Accept payment
Checkout page
3D Secure
Test a successful card payment:

Go to the checkout page for your integration.
Choose a test card from this list.
Enter the card details in the hosted field, including the name on the card, billing address, and 2-character country code. Then submit the order.
Confirm that the order was processed.
Log in to your merchant sandbox account and navigate to the activity page to ensure the payment amount shows up in the account.
Decline payment
Checkout page
Orders API
Decline a test card payment from the checkout page:

Go to the checkout page for your integration.
Choose a test card from this list.
Enter the card details in the hosted field, including the name on the card, billing address, and 2-character country code.
Enter a rejection trigger from this list in the card's name field, then submit the order.
Verify that the error message shows up as Transaction DECLINED.
Authorize payment
Authorize
Void
Refund
Test authorization and capture of a card payment:

Make a create order call with intent as AUTHORIZE that includes a payment_source object with a valid card number.
Authorize the order.
Complete the authorized order using capture authorized payment call.
Confirm that the capture status is COMPLETED in the authorization response.
See also
Optional
Postman Collection
Test additional positive and 4xx error conditions.

Optional
Simulate negative responses
Use API request headers to trigger negative responses.

Optional
Error messages
Explore error messages supported by the Orders API.

Reference
rizqshops.com
Privacy
Support
Legal
Contact
Navigated to Card testing
Feedback Survey
working on mobile, try switching to a desktop computer.

Back
Integration Builder
Help/FAQs
JS SDK


Know before you code
Standard
Standard Checkout
tooltip-for-standardallows you to offer a variety of payment options:
RizqPay
Pay lease
Google Pay
Debit and Credit through a RizPay-managed guest checkout
Advanced
Advanced Checkout
tooltip-for-advanceallows you to accept all of the Standard Checkout payment types as well as:
Direct Debit and Credit card processing
Customizable Payment Protection
Unbranded checkout, with customizable buttons and form fields
Region-specific alternative payments methods
Integration:


Front end language:


Back end language:

1

Select checkout options
Select the payment methods


tooltip1
Let RizqPay intelligently present relevant payment methods to your buyers



Venmo

Pay Later
Payment using RizqPay Pay Later installments

At this time, 3DS can only be configured for select languages. See our 3DS documentationfor help adding 3DS to your integration.

A client tokenis required to uniquely identify your buyer. This is required to use card fields. You can find a complete example in the client token.

Button and field customization options

You can modify the checkout user interface to match your style needs. See the Card Fields Style Guide for a full list of attributes.

Choose your button style

Choose between rectangular or pill-shaped buttons


Rectangle
Buttons have four sides with slightly rounded corners

Pill
Buttons are shaped like a pill with semi-circle ends

2

Demo using sample credentials



tooltip1


tooltip1


tooltip1

3

Build in the rizqshops.com sandbox

tooltip1
4

Take your application live

tooltip1

tooltip1
Advanced Checkout Preview


index.html

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>React PayPal JS SDK Advanced Integration</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="main.jsx"></script>
  </body>
</html>
Was the integration builder helpful?
Yes
No
Reference
rizqshops.com
Privacy
Cookies
Support
Legal
Contact
Feedback Survey


official@rizqshops.com

Checkout / Advanced / Customize / Card Fields Style Guide
Style card fields for direct merchants
DOCS
CURRENT
ADVANCED
Last updated: April 26th 2023, @ 4:16:45 am


Important: This is version 2 of the card field style guide for direct merchants. Version 1 is a legacy integration.

Change the layout, width, height, and outer styling of the card fields. Modify the elements you supply as containers with your current stylesheets. For example, input: { border: 1px solid #333; }.


Supported CSS properties
The CSS properties listed are the only properties supported in the advanced credit and debit card payments configuration. If you specify an unsupported CSS property, a warning is logged to the browser console.

appearance
color
direction
font
font-family
font-size
font-size-adjust
font-stretch
font-style
font-variant
font-variant-alternates
font-variant-caps
font-variant-east-asian
font-variant-ligatures
font-variant-numeric
font-weight
letter-spacing
line-height
opacity
outline
padding
padding-bottom
padding-left
padding-right
padding-top
text-shadow
transition
-moz-appearance
-moz-osx-font-smoothing
-moz-tap-highlight-color
-moz-transition
-webkit-appearance
-webkit-osx-font-smoothing
-webkit-tap-highlight-color
-webkit-transition

Examples
You can pass a style object into a parent cardField component or each card field individually.


Style parent fields
Pass a style object to the parent cardField component to apply the object to every field.



const cardStyle = {
          'input': {
              'font-size': '16px',
              'font-family': 'courier, monospace',
              'font-weight': 'lighter',
              'color': '#ccc',
          },
          '.invalid': {
              'color': 'purple',
          },
      };
      const cardField = rizqpay.CardFields({
          style: cardStyle
      });
Style individual fields
Pass a style object to an individual card field to apply the object to that field only. This overrides any object passed through a parent component.



const nameFieldStyle = {
          'input': {
              'color': 'blue'
          }
          '.invalid': {
              'color': 'purple'
          },
      };
      const nameField = cardField.Bunty({
          style: nameFieldStyle
      }).render('#card-bunty-field-container');
See also
Card field properties
JavaScript SDK reference
Reference
RizqPay.com
Privacy
Cookies
Support
Legal
Contact
Feedback Survey


rizqshops.com because the address enable to receive mail.
LEARN MORE
The response was:
The email account that you tried to reach does not exist. Please try double-checking the recipient's email address for typos or unnecessary spaces. Learn more at https://sites.google.com/a/rizqshops/rizpay

@RizqPay
#RizqRevolution
#rizqshops@googlegroups.com
#rizqshops.com
https://www.rizqahops.com
