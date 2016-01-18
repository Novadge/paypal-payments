package com.novadge.paypal

import com.paypal.base.ConfigManager;
import com.paypal.base.Constants;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalResource;
import com.paypal.base.rest.OAuthTokenCredential; 
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.base.rest.PayPalResource;
import grails.converters.JSON
import com.paypal.api.payments.CreditCardToken;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Refund;
import com.paypal.api.payments.Sale;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentHistory;
//import com.paypal.api.payments.util.GenerateAccessToken;
//import com.paypal.api.payments.util.ResultPrinter;
import com.paypal.api.payments.Address; 
import com.paypal.api.payments.Amount; 
import com.paypal.api.payments.Authorization; 
import com.paypal.api.payments.Capture; 
import com.paypal.api.payments.CreditCard;
import com.paypal.api.payments.Details;
import com.paypal.api.payments.FundingInstrument; 
import com.paypal.api.payments.Payer; 
 
import com.paypal.api.payments.Transaction; 
//import com.paypal.api.payments.util.GenerateAccessToken; 
//import com.paypal.api.payments.util.ResultPrinter; 

import com.paypal.api.payments.Item;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Links;

import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;

// rest

import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils
import org.apache.http.HttpEntity

import java.net.URLEncoder

//

import grails.transaction.Transactional
import groovy.json.JsonSlurper

@Transactional
class PaypalService {

    /**
     * AccessToken
     * Retrieve the access token from
     * OAuthTokenCredential by passing in
     * ClientID and ClientSecret
     * @param clientId : client id
     * @param clientSecret: client secret
     * @param configurationMap : map of configuration properties 
     * eg ["service.EndPoint":"https://api.sandbox.paypal.com"] or 
     * ['mode':'sandbox']
     * @returns String : access token
     **/
    public static String getAccessToken(String clientID,String clientSecret, Map configurationMap) throws PayPalRESTException {
    
        return new OAuthTokenCredential(clientID, clientSecret,configurationMap).getAccessToken();
 
    }
    
    
    
    /**
     * APIContext
     * Retrieve the api context 
     * @param accessToken : Your access token gotten from getAccessToken()
     * @param sdkConfig : map of sdk config 
     * @returns APIContext : api context
     **/
    APIContext getAPIContext(String accessToken,Map sdkConfig){
        APIContext apiContext = new APIContext(accessToken);
        apiContext.setConfigurationMap(sdkConfig);
        return apiContext
    }
    
    /**
     * APIContext
     * Retrieve the api context 
     * @param accessToken : Your access token gotten from getAccessToken()
     * @param sdkConfig :
     * @param requestId : something meaningful to your application
     * @returns APIContext : api context
     **/
    APIContext createAPIContext(String accessToken,String requestId){
        return new APIContext(accessToken,requestId);
        
    }
     
    /**
     * Address
     * Retrieve an address object
     * @param props : map of properties
     * eg ["Line1":"34 valley crescent","city":"Enugu",....]
     * @returns Address : address
     **/
    Address createAddress(Map props){
        Address address = new Address();
        address.setLine1(props['line1']);
        address.setCity(props['city']);
        address.setCountryCode(props['countryCode']);
        address.setPostalCode(props['postalCode']);
        address.setState(props['state']);
        return address
    }
    
    /**
     *  Let's you specify details of a payment amount.
     * Create details for a payment
     * @param props : map of properties
     * eg ["shipping":"10.00","subTotal":"10.00","tax":"0.00"]
     * @returns Details : details object
     **/
    Details createDetails(Map props){
        Details details = new Details();
        details.setShipping(props['shipping']);
        details.setSubtotal(props['subTotal']);
        details.setTax(props['tax']);
        return details
                    
    }
   
    /*
     * Let's you specify a payment amount.
     * @param props : map of properties
     * props.currency : Currency code eg "USD"
     * props.total : Total amount eg 10.00 // 2 decimal places and positive
     * props.details : details
     * @returns Amount : Amount object
     * */
    Amount createAmount(Map props){
        Amount amount = new Amount();
        amount.setCurrency(props['currency']);
        // Total must be equal to sum of shipping, tax and subtotal.
        amount.setTotal(props['total']);
        amount.setDetails(props['details']);
        return amount
    }                
    
    /* A transaction defines the contract of a
     *payment - what is the payment for and who
     * is fulfilling it. Transaction is created with
     * a `Payee` and `Amount` types
     * */
    Transaction createTransaction(Map props){
        Transaction transaction = new Transaction();
        transaction.setAmount(props['amount']);
        transaction.setDescription(props['description']);
        return transaction
    }
    
    /* ##Payer: A resource representing a Payer that funds a payment
     * Let's you create a payer object.
     * @param props : map of properties
     * props.paymentMethod: payment method eg credit_card,paypal
     * props.fundingInstrumentList : A list of funding instruments
     * @returns Payer : Payer object
     * */
    Payer createPayer(Map props){
        Payer payer = new Payer();
        payer.setPaymentMethod(props['paymentMethod']);
        payer.setFundingInstruments(props['fundingInstrumentList']);//List
        return payer
    }
     
    /* 
     * Create a payment
     * @param props : Map of payment properties
     * @props.intent : payment intent eg 'sale', etc
     * @props.payer : payer
     * @props.transactionList : transaction list
     * @props.redirectUrls : redirect urls
     * @props.apiContext : ApiContext
     * @returns Payment : payment object
     * */
    Payment createPayment(Map props){
        Payment payment = new Payment();
        payment.setIntent(props['intent']);
        payment.setPayer(props['payer']);
        payment.setTransactions(props['transactionList']);// list
        payment.setRedirectUrls(props['redirectUrls']);
        Payment createdPayment = payment.create(props['apiContext']);
        return createdPayment
    }
    
    /* 
     * Create a payment
     * @param props : Map of payment properties
     * @props.intent : payment intent eg 'sale', etc
     * @props.payer : payer
     * @props.transactionList : transaction list
     * @props.redirectUrls : redirect urls
     * @props.apiContext : ApiContext
     * @returns Payment : payment object
     * */
    Refund createRefund(Map props){
        Refund refund = new Refund();
	refund.setAmount(props['amount']);
        return refund
    }

    /*
     * Execute a payment
     * @params props : Map of properties
     * @params apiContext : ApiContext 
     * @props.paymentId : Payment id
     * @props.payerId : Payer id (usually returned by paypal )
     * @returns Payment : executed Payment
     * */
    Payment createPaymentExecution(Map props,APIContext apiContext){
        Payment payment = Payment.get(apiContext, props['paymentId']);
        PaymentExecution paymentExecute = new PaymentExecution();
        paymentExecute.setPayerId(props['payerId']);
        return payment.execute(apiContext, paymentExecute);
    }
        
     
    /*
     * Create redirect urls
     * @params props : Map of properties
     * @props.cancelUrl : Url to redirect the user if the transaction is canceled 
     * @props.returnUrl : Url to rediect the user if the transaction is successful
     * @returns : RedirectUrl : an instance of RedirectUrls
     * */
    RedirectUrls createRedirectUrls(Map props){
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(props['cancelUrl']);
        redirectUrls.setReturnUrl(props['returnUrl']);
        return redirectUrls
       
    }
        
    /*
     * Transfer funds to another paypal account
     * @param accessToken : paypal access token
     * @param endpont : url endpoint for payout
     * @param payoutProps
     * */
    CloseableHttpResponse createPayout(String accessToken,Map sdkConfig,Map payoutProps){
        
        
        String url = sdkConfig.get(Constants.ENDPOINT) 
        url += '/v1/payments/payouts?sync_mode=true' 
        
        
        CredentialsProvider provider = new BasicCredentialsProvider()
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(sdkConfig.get(Constants.CLIENT_ID), sdkConfig.get(Constants.CLIENT_SECRET))
        provider.setCredentials(AuthScope.ANY, credentials)
        HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build()
                        
        HttpPost httpPost = new HttpPost(url)
        httpPost.addHeader("Content-Type",'application/json')
        httpPost.addHeader("Authorization",accessToken)
        //        print "receiver = ${recipientInfo}"
        //        String recipientType = "EMAIL"
        
        //       Map params = ["sender_batch_header": 
        //           ["email_subject": subject ],"items": [ [ "recipient_type": 
        //                   recipientType, "amount": 
        //                   ["value": amount,"currency": currencyCode ],"receiver": 
        //                   recipientInfo,"note": note,"sender_item_id": itemId ]]]
       
        def jsonParam = payoutProps as JSON
       
        StringEntity se = new StringEntity(jsonParam.toString())
        httpPost.setEntity(se); 
        
        
        return client.execute(httpPost)
        
    }
    
    /* Return a map of transction fees and associated currency
     * @param jsonResponse : json response received from paypal
     * @return List of map of transaction fees eg [[currency:'usd', value:10.0]]
     * 
     * */
    List listTransactionFees(Payment payment){
//        JsonSlurper slurper = new JsonSlurper()
//        def map = slurper.parseText(jsonString);
        List fees = []

        payment['transactions']?.each{ val ->
            val['relatedResources']?.each{ resource ->
                fees.add(resource['sale']['transactionFee'])
    
            }
        }
        
        return fees
    }
    
    /* ###FundingInstrument
     * A resource representing a Payeer's funding instrument.
     * Use a Payer ID (A unique identifier of the payer generated
     * and provided by the facilitator. This is required when
     * creating or using a tokenized funding instrument)
     * and the `CreditCardDetails`
     * @param props : map of funding instrument properties.
     * @returns FundingInstrument
     * */
    FundingInstrument createFundingInstrument(Map props){
            
        FundingInstrument fundingInstrument = new FundingInstrument();
        fundingInstrument.setCreditCard(props['creditCard']);
        fundingInstrument.setCreditCardToken(props['creditCardToken']);
    }
    
    /* Retrieve the CreditCard object by calling the
     * static `get` method on the CreditCard class,
     * and pass the Access Token and CreditCard ID
     * @param: cardId : Credit card id
     * @accessToken : access token
     * @returns CreditCard : credit card
     * */
    CreditCard getCreditCard(String cardId,String accessToken){
        // Retrieve the CreditCard object by calling the
        // static `get` method on the CreditCard class,
        // and pass the Access Token and CreditCard ID
        return CreditCard.get(accessToken,cardId);
               
    }
     
    /* ###CreditCard
     * Create A resource representing a credit card that can be
     * used to fund a payment.
     * @param props : Map of card properties
     * @param apiContext : API Context
     * @props.ccv2 : int 
     * @props.expireMonth : int eg 8
     * @props.expireYear : int eg 2012
     * @props.firstName : First name as it appears on card
     * @props.lastName : Last name.......
     * @props.cardNumber : Card number
     * @props.type : Type of card eg
     * @props.billingAddress: Billing Address
     * @props.payerId : Payer id
     * @returns CreditCard : credit card instance
     * 
     * */
    CreditCard createCreditCard(Map props,APIContext apiContext){
        
        CreditCard creditCard = new CreditCard();
        
        creditCard.setCvv2(props['ccv2']);// int
        creditCard.setExpireMonth(props['expireMonth']); // int eg 8
        creditCard.setExpireYear(props['expireYear']); // int eg 2012
        creditCard.setFirstName(props['firstName']);
        creditCard.setLastName(props['lastName']);
        creditCard.setNumber(props['cardNumber']); // string eg 5554443344655
        creditCard.setType(props['type']); // string eg mastercard, visa, etc
        creditCard.setBillingAddress(props['billingAddress']);
        creditCard.setPayerId(props['payerId']);

            
        // ###Save
        // Creates the credit card as a resource
        // in the PayPal vault. The response contains
        // an 'id' that you can use to refer to it
        // in the future payments.
            
        CreditCard createdCreditCard = creditCard.create(apiContext);
            
        return createdCreditCard
    }

    
    /* Retrieve the Payment object by calling the
     * static `get` method on the Payment class,
     * and pass the Access Token and Payment ID
     * @param: paymentId : Payment id
     * @accessToken : access token
     * @returns Payment: payment
     * */
    Payment getPayment(String paymentId,String accessToken){
            
        // Retrieve the payment object by calling the
        // static `get` method
        // on the Payment class by passing a valid
        // AccessToken and Payment ID
        return Payment.get(accessToken,paymentId);
              
    }
    
    
    /* 
     * retrieve the details of a Capture resource
     * @param apiContext : APIContext
     * @param amount : amount
     * @param authorization : authorization
     * @returns Capture : capture
     * API used: /v1/payments/capture/{capture_id}
     * 
     * */
    private Capture getCapture(APIContext apiContext, Amount amount, Authorization authorization) throws PayPalRESTException{
        
        // ###Capture
        Capture capture = new Capture();
        capture.setAmount(amount);
		
        // ##IsFinalCapture
        // If set to true, all remaining 
        // funds held by the authorization 
        // will be released in the funding 
        // instrument. Default is �false�.
        capture.setIsFinalCapture(true);

        // Capture by POSTing to
        // URI v1/payments/authorization/{authorization_id}/capture
        Capture responseCapture = authorization.capture(apiContext, capture);
        return responseCapture;
    }
    
    /**
     * Get authorization
     * @param  payer : payer resource
     * @param transactions : list of transactions
     * @param apiContext : api context
     * 
     **/
    private Authorization getAuthorization(Payer payer, List<Transaction> transactions,APIContext apiContext)
    throws PayPalRESTException {
       
        Payment responsePayment = createPayment(['intent':'authorize','payer':payer,'transactionList':transactions,'apiContext':apiContext])
        return responsePayment.getTransactions().get(0).getRelatedResources()
        .get(0).getAuthorization();
    }
    
    
    /**
     * void an authorization
     * @param  authorization : authorization to void
   
     * @param apiContext : api context
     * 
     **/
    def voidAuthorization(Authorization authorization,APIContext apiContext){
        
        // Void an Authorization
        // by POSTing to 
        // URI v1/payments/authorization/{authorization_id}/void
        Authorization returnAuthorization = authorization.doVoid(apiContext);

    }  

        
    
    /* #RefundCapture Sample
     * This sample code demonstrate how you
     * can do a Refund on a Capture resource
     * API used: /v1/payments/capture/{capture_id}/refund
     * */
    def refundCapture(String accessToken,Refund refund){
        //TODO : Refund a capture
    }
    
    /* Refund a sale
     * @param sale : sale resource
     * param refund : refund resource
     * @param apiContext : Api context
     * API used: /v1/payments/capture/{capture_id}/refund
     * */
    def refundSale(Sale sale,Refund refund, APIContext apiContext){
        
        sale.refund(apiContext, refund);
             
    }
        
    /* ###Retrieve
     * Retrieve the PaymentHistory object
     * @param containerMap : map of query parameters for paginations and filtering
     * @param accessToken : AccessToken 
     * Refer the API documentation for valid values for container map keys
     **/
    def getPaymentHistory(Map containerMap, String accessToken){
            
        // Retrieve the PaymentHistory object by calling the
        // static `get` method
        // on the Payment class, and pass the AccessToken and a ContainerMap object
        return Payment.list(accessToken, containerMap); 
                
    }
    
    /* Get Sale By SaleID 
     *  retrieve details of completed Sale Transaction.
     *  @param saleId : sale id
     *  @param accessToken : access token
     * API used: /v1/payments/sale/{sale-id}
     **/
    def getSale(String saleId, String accessToken){
            
        // Pass an AccessToken and the ID of the sale
        // transaction from your payment resource.
        return Sale.get(accessToken, saleId); 
                
    }
}
