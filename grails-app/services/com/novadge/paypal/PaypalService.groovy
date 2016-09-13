package com.novadge.paypal

import grails.converters.JSON
import groovy.transform.CompileStatic

import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder

import com.paypal.api.payments.Address
import com.paypal.api.payments.Amount
import com.paypal.api.payments.Authorization
import com.paypal.api.payments.Capture
import com.paypal.api.payments.CreditCard
import com.paypal.api.payments.CreditCardToken
import com.paypal.api.payments.Details
import com.paypal.api.payments.FundingInstrument
import com.paypal.api.payments.Payer
import com.paypal.api.payments.Payment
import com.paypal.api.payments.PaymentExecution
import com.paypal.api.payments.RedirectUrls
import com.paypal.api.payments.Refund
import com.paypal.api.payments.RelatedResources
import com.paypal.api.payments.Sale
import com.paypal.api.payments.Transaction
import com.paypal.base.Constants
import com.paypal.base.rest.APIContext
import com.paypal.base.rest.OAuthTokenCredential
import com.paypal.base.rest.PayPalRESTException

@CompileStatic
class PaypalService {

    static transactional = false

    /**
     * AccessToken
     * Retrieve the access token from
     * OAuthTokenCredential by passing in
     * ClientID and ClientSecret
     * @param clientId client id
     * @param clientSecret: client secret
     * @param configurationMap map of configuration properties
     * eg ["service.EndPoint":"https://api.sandbox.paypal.com"] or
     * ['mode':'sandbox']
     * @return access token
     */
    static String getAccessToken(String clientID, String clientSecret, Map configurationMap) throws PayPalRESTException {
        return new OAuthTokenCredential(clientID, clientSecret, configurationMap).accessToken
    }

    /**
     * APIContext
     * Retrieve the api context
     * @param accessToken Your access token from getAccessToken()
     * @param sdkConfig sdk config
     * @return api context
     */
    APIContext getAPIContext(String accessToken, Map sdkConfig) {
        APIContext apiContext = new APIContext(accessToken)
        apiContext.configurationMap = sdkConfig
        return apiContext
    }

    /**
     * APIContext
     * Retrieve the api context
     * @param accessToken Your access token from getAccessToken()
     * @param requestId something meaningful to your application
     * @return api context
     */
    APIContext createAPIContext(String accessToken, String requestId) {
        return new APIContext(accessToken, requestId)
    }

    /**
     * Address
     * Retrieve an address object
     * @param props properties
     * eg ["Line1":"34 valley crescent","city":"Enugu",....]
     * @return address
     */
    Address createAddress(Map<String, String> props) {
        new Address(line1: props.line1, city: props.city, countryCode: props.countryCode,
                    postalCode: props.postalCode, state: props.state)
    }

    /**
     * Lets you specify details of a payment amount.
     * Create details for a payment
     * @param props properties
     * eg ["shipping":"10.00","subTotal":"10.00","tax":"0.00"]
     * @return details object
     */
    Details createDetails(Map<String, String> props) {
        //new Details(shipping: props.shipping, subtotal: props.subTotal, tax: props.tax)
        
		Details details = new Details();
		details.setShipping("${props.shipping}");
		details.setSubtotal("${props.subTotal}");
		details.setTax("${props.tax}")
                return details
    }

    /**
     * Lets you specify a payment amount.
     * @param props properties
     * props.currency: Currency code eg "USD"
     * props.total: Total amount eg 10.00 // 2 decimal places and positive
     * props.details: details
     * @return Amount object
     */
    Amount createAmount(Map props) {
        // Total must be equal to sum of shipping, tax and subtotal.
//        new Amount(currency: (String)props.currency, total: (String)props.total, details: )
        
        Amount amount = new Amount();
		amount.setCurrency("${props?.currency}");
		// Total must be equal to sum of shipping, tax and subtotal.
		amount.setTotal("${props.total}");
		amount.setDetails((Details)props.details);
                return amount;
    }

    /**
     * Lets you create a transaction
     * A transaction defines the contract of a
     * payment - what is the payment for and who
     * is fulfilling it. Transaction is created with
     * a `Payee` and `Amount` types
     * @param props properties
     */
    Transaction createTransaction(Map props) {
       // new Transaction(amount: (Amount)props.amount, description: (String)props.description)
        
        Transaction transaction = new Transaction();
		transaction.setAmount((Amount)props.amount);
		transaction.setDescription("${props.description}");
                return transaction;
    }

    /** ##Payer: A resource representing a Payer that funds a payment
     * Lets you create a payer object.
     * @param props properties
     * props.paymentMethod: payment method eg credit_card,paypal
     * props.fundingInstrumentList : A list of funding instruments
     * @return Payer object
     */
    Payer createPayer(Map props) {
        new Payer(paymentMethod: (String)props.paymentMethod,
                  fundingInstruments: (List<FundingInstrument>)props.fundingInstrumentList)
    }

    /**
     * Create a payment
     * @param props payment properties
     * props.intent: payment intent eg 'sale', etc
     * props.payer: payer
     * props.transactionList : transaction list
     * props.redirectUrls : redirect urls
     * props.apiContext : ApiContext
     * @return payment object
     */
    Payment createPayment(Map props) {
        new Payment(intent: (String)props.intent,
                    payer: (Payer)props.payer,
                    transactions: (List<Transaction>)props.transactionList,
                    redirectUrls: (RedirectUrls)props.redirectUrls).create((APIContext)props.apiContext)
    }

    /**
     * Create a refund
     * @param props refund properties
     * props.amount : amount
     * @return refund resource
     */
    Refund createRefund(Map props) {
        new Refund(amount: (Amount)props.amount)
    }

    /**
     * Execute a payment
     * @params props properties
     * @params apiContext ApiContext
     * @props.paymentId : Payment id
     * @props.payerId : Payer id (usually returned by paypal )
     * @return executed Payment
     */
    Payment createPaymentExecution(Map<String, String> props, APIContext apiContext) {
        return Payment.get(apiContext, props.paymentId).execute(
            apiContext, new PaymentExecution(payerId: props.payerId))
    }

    /**
     * Create redirect urls
     * @params props properties
     * props.cancelUrl Url to redirect the user if the transaction is canceled
     * props.returnUrl Url to rediect the user if the transaction is successful
     * @return an instance of RedirectUrls
     */
    RedirectUrls createRedirectUrls(Map<String, String> props) {
        new RedirectUrls(cancelUrl: props.cancelUrl, returnUrl: props.returnUrl)
    }

    /**
     * Transfer funds to another paypal account
     * @param accessToken paypal access token
     * @param endpont url endpoint for payout
     * @param payoutProps
     * @return response
     */
    CloseableHttpResponse createPayout(String accessToken, Map<String, String> sdkConfig, Map payoutProps) {

        HttpPost httpPost = new HttpPost(sdkConfig[Constants.ENDPOINT].toString() + '/v1/payments/payouts?sync_mode=true')
        httpPost.addHeader("Content-Type", 'application/json')
        httpPost.addHeader("Authorization", accessToken)
        httpPost.setEntity(new StringEntity((payoutProps as JSON).toString()))

        CredentialsProvider provider = new BasicCredentialsProvider()
        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
            sdkConfig[Constants.CLIENT_ID],
            sdkConfig[Constants.CLIENT_SECRET]))

        return HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build().execute(httpPost)
    }

    /**
     * Return transction fees and associated currency
     * @param jsonResponse : json response received from paypal
     * @return List of map of transaction fees eg [[currency:'usd', value:10.0]]
     */
    List listTransactionFees(Payment payment) {
        List fees = []

        for (Transaction tx in payment.transactions) {
            for (RelatedResources resource in tx.relatedResources) {
                fees << resource.sale.transactionFee
            }
        }

        return fees
    }

    /**
     * Lets you create a FundingInstrument
     * A resource representing a Payeer's funding instrument.
     * @param props funding instrument properties.
     * props.creditCard : credit card
     * props.creditCardToken : credit card token
     * @return FundingInstrument
     */
    FundingInstrument createFundingInstrument(Map props) {
        new FundingInstrument(creditCard: (CreditCard)props.creditCard,
                              creditCardToken: (CreditCardToken)props.creditCardToken)
    }

    /**
     * Retrieve the CreditCard object by calling the
     * static `get` method on the CreditCard class,
     * and pass the Access Token and CreditCard ID
     * @param: cardId : Credit card id
     * @accessToken : access token
     * @return credit card
     */
    CreditCard getCreditCard(String cardId, String accessToken) {
        return CreditCard.get(accessToken, cardId)
    }

    /**
     * ###CreditCard
     * Create A resource representing a credit card that can be
     * used to fund a payment.
     * @param props card properties
     * @param apiContext : API Context
     * props.ccv2 : int
     * props.expireMonth : int eg 8
     * props.expireYear : int eg 2012
     * props.firstName : First name as it appears on card
     * props.lastName : Last name.......
     * props.cardNumber : Card number
     * props.type : Type of card eg
     * props.billingAddress: Billing Address
     * props.payerId : Payer id
     * @return CreditCard : credit card instance
     */
    CreditCard createCreditCard(Map props, APIContext apiContext) {
        // ###Save
        // Creates the credit card as a resource
        // in the PayPal vault. The response contains
        // an 'id' that you can use to refer to it
        // in the future payments.

        new CreditCard(cvv2: "${props.ccv2}", expireMonth: (int)props.expireMonth,
                       expireYear: (int)props.expireYear, firstName: props.firstName.toString(),
                       lastName: props.lastName.toString(), number: props.cardNumber.toString(),
                       type: props.type.toString(), billingAddress: (Address)props.billingAddress,
                            payerId: props.payerId.toString()).create(apiContext)
    }

    /**
     * Retrieve the Payment object by calling the
     * static `get` method on the Payment class,
     * and pass the Access Token and Payment ID
     * @param paymentId Payment id
     * @param accessToken access token
     * @return payment
     */
    Payment getPayment(String paymentId, String accessToken) {
        return Payment.get(accessToken, paymentId)
    }

    /**
     * retrieve the details of a Capture resource
     * @param apiContext : APIContext
     * @param amount : amount
     * @param authorization : authorization
     * @return Capture : capture
     * API used: /v1/payments/capture/{capture_id}
     */
    private Capture getCapture(APIContext apiContext, Amount amount, Authorization authorization) throws PayPalRESTException {

        // ##IsFinalCapture
        // If set to true, all remaining
        // funds held by the authorization
        // will be released in the funding
        // instrument. Default is �false�.

        // Capture by POSTing to
        // URI v1/payments/authorization/{authorization_id}/capture
        authorization.capture(apiContext, new Capture(amount: amount, isFinalCapture: true))
    }

    /**
     * Get authorization
     * @param  payer : payer resource
     * @param transactions : list of transactions
     * @param apiContext : api context
     * @return Authorization: authorization
     */
    private Authorization getAuthorization(Payer payer, List<Transaction> transactions, APIContext apiContext) throws PayPalRESTException {
        createPayment(intent: 'authorize', payer: payer,
                      transactionList: transactions,
                      apiContext: apiContext).transactions[0].relatedResources[0].authorization
    }

    /**
     * void an authorization
     * @param  authorization : authorization to void
     * @param apiContext : api context
     */
    def voidAuthorization(Authorization authorization, APIContext apiContext) {
        // Void an Authorization
        // by POSTing to
        // URI v1/payments/authorization/{authorization_id}/void
        Authorization returnAuthorization = authorization.doVoid(apiContext)
    }

    /**
     * Refund a capture
     * API used: /v1/payments/capture/{capture_id}/refund
     */
    def refundCapture(String accessToken, Refund refund) {
        //TODO : Refund a capture
    }

    /**
     * Refund a sale
     * @param sale sale resource
     * @param refund refund resource
     * @param apiContext Api context
     * API used: /v1/payments/capture/{capture_id}/refund
     */
    def refundSale(Sale sale, Refund refund, APIContext apiContext) {
        sale.refund(apiContext, refund)
    }

    /**
     * ###Retrieve
     * Retrieve the PaymentHistory object
     * @param containerMap query parameters for paginations and filtering
     * @param accessToken AccessToken
     * Refer the API documentation for valid values for container map keys
     */
    def getPaymentHistory(Map containerMap, String accessToken) {
        // Retrieve the PaymentHistory object by calling the static `get` method
        // on the Payment class, and pass the AccessToken and a ContainerMap object
        return Payment.list(accessToken, containerMap)
    }

    /**
     * Get Sale By SaleID
     * retrieve details of completed Sale Transaction.
     * @param saleId sale id
     * @param accessToken access token
     * API used: /v1/payments/sale/{sale-id}
     */
    def getSale(String saleId, String accessToken) {
        // Pass an AccessToken and the ID of the sale
        // transaction from your payment resource.
        return Sale.get(accessToken, saleId)
    }
}
