package com.novadge.paypal

import com.paypal.base.ConfigManager;
import com.paypal.base.Constants;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalResource;
import com.paypal.base.rest.OAuthTokenCredential; 
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.base.rest.PayPalResource;
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



import grails.transaction.Transactional

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
     * eg ["service.EndPoint":"https://api.sandbox.paypal.com"] or 
     * ['mode':'sandbox']
     * @returns APIContext : api context
     **/
    APIContext getAPIContext(String accessToken,Map sdkConfig){
        APIContext apiContext = new APIContext(accessToken);
        apiContext.setConfigurationMap(sdkConfig);
        return apiContext
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
     * Details
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
    
    /*
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
        
    Payment createPayment(Map props){
        Payment payment = new Payment();
        payment.setIntent(props['intent']);
        payment.setPayer(props['payer']);
        payment.setTransactions(props['transactionList']);// list
        payment.setRedirectUrls(props['redirectUrls']);
        Payment createdPayment = payment.create(props['apiContext']);
        return createdPayment
    }
	
	Payment createPaymentExecution(Map props,APIContext apiContext){
		Payment payment = Payment.get(apiContext, props['paymentId']);
		PaymentExecution paymentExecute = new PaymentExecution();
		paymentExecute.setPayerId(props['payerId']);
		return payment.execute(apiContext, paymentExecute);
	}
        
        
    RedirectUrls createRedirectUrls(Map props){
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(props['cancelUrl']);
        redirectUrls.setReturnUrl(props['returnUrl']);
        return redirectUrls
       
    }
        
    
    /* ###FundingInstrument
     * A resource representing a Payeer's funding instrument.
     * Use a Payer ID (A unique identifier of the payer generated
     * and provided by the facilitator. This is required when
     * creating or using a tokenized funding instrument)
     * and the `CreditCardDetails`
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
     * A resource representing a credit card that can be
     * used to fund a payment.
     * */
    CreditCard createCreditCard(Map cardProps,APIContext apiContext){
        
        CreditCard creditCard = new CreditCard();
        
        creditCard.setCvv2(cardProps['ccv2']);// int
        creditCard.setExpireMonth(cardProps['expireMonth']); // int eg 8
        creditCard.setExpireYear(cardProps['expireYear']); // int eg 2012
        creditCard.setFirstName(cardProps['firstName']);
        creditCard.setLastName(cardProps['lastName']);
        creditCard.setNumber(cardProps['cardNumber']); // string eg 5554443344655
        creditCard.setType(cardProps['type']); // string eg mastercard, visa, etc
        creditCard.setBillingAddress(cardProps['billingAddress']);
        creditCard.setPayerId(cardProps['payerId']);

            
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
    
    
    // #GetCapture Sample
    // This sample code demonstrate how you
    // can retrieve the details of a Capture
    // resource
    // API used: /v1/payments/capture/{capture_id}
    private Capture getCapture(APIContext apiContext, Amount amount, Authorization authorization) throws PayPalRESTException{
        // ###Amount
        // Let's you specify a capture amount.
        //		Amount amount = new Amount();
        //		amount.setCurrency("USD");
        //		amount.setTotal("4.54");

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
        
    private Authorization getAuthorization(CreditCard creditCard,Address billingAddress,Amount amount,APIContext apiContext)
    throws PayPalRESTException {
        //
        //		// ###Details
        //		// Let's you specify details of a payment amount.
        //		Details details = new Details();
        //		details.setShipping("0.03");
        //		details.setSubtotal("107.41");
        //		details.setTax("0.03");
        //
        //		// ###Amount
        //		// Let's you specify a payment amount.
        //		Amount amount = new Amount();
        //		amount.setCurrency("USD");
        //		amount.setTotal("107.47");
        //		amount.setDetails(details);

        // ###Transaction
        // A transaction defines the contract of a
        // payment - what is the payment for and who
        // is fulfilling it. Transaction is created with
        // a `Payee` and `Amount` types
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction
        .setDescription("This is the payment transaction description.");

        // The Payment creation API requires a list of
        // Transaction; add the created `Transaction`
        // to a List
        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(transaction);

        //		// ###Address
        //		// Base Address object used as shipping or billing
        //		// address in a payment. [Optional]
        //		Address billingAddress = new Address();
        //		billingAddress.setCity("Johnstown");
        //		billingAddress.setCountryCode("US");
        //		billingAddress.setLine1("52 N Main ST");
        //		billingAddress.setPostalCode("43210");
        //		billingAddress.setState("OH");

        //		// ###CreditCard
        //		// A resource representing a credit card that can be
        //		// used to fund a payment.
        //		CreditCard creditCard = new CreditCard();
        //		creditCard.setBillingAddress(billingAddress);
        //		creditCard.setCvv2(874);
        //		creditCard.setExpireMonth(11);
        //		creditCard.setExpireYear(2018);
        //		creditCard.setFirstName("Joe");
        //		creditCard.setLastName("Shopper");
        //		creditCard.setNumber("4417119669820331");
        //		creditCard.setType("visa");

        // ###FundingInstrument
        // A resource representing a Payeer's funding instrument.
        // Use a Payer ID (A unique identifier of the payer generated
        // and provided by the facilitator. This is required when
        // creating or using a tokenized funding instrument)
        // and the `CreditCardDetails`
        FundingInstrument fundingInstrument = new FundingInstrument();
        fundingInstrument.setCreditCard(creditCard);

        // The Payment creation API requires a list of
        // FundingInstrument; add the created `FundingInstrument`
        // to a List
        List<FundingInstrument> fundingInstruments = new ArrayList<FundingInstrument>();
        fundingInstruments.add(fundingInstrument);

        // ###Payer
        // A resource representing a Payer that funds a payment
        // Use the List of `FundingInstrument` and the Payment Method
        // as 'credit_card'
        Payer payer = new Payer();
        payer.setFundingInstruments(fundingInstruments);
        payer.setPaymentMethod("credit_card");

        // ###Payment
        // A Payment Resource; create one using
        // the above types and intent as 'authorize'
        Payment payment = new Payment();
        payment.setIntent("authorize");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        Payment responsePayment = payment.create(apiContext);
        return responsePayment.getTransactions().get(0).getRelatedResources()
        .get(0).getAuthorization();
    }
    
       

    def authorizationCapture(){
        try {
            accessToken = GenerateAccessToken.getAccessToken();

            // ### Api Context
            // Pass in a `ApiContext` object to authenticate
            // the call and to send a unique request id
            // (that ensures idempotency). The SDK generates
            // a request id if you do not pass one explicitly.
            apiContext = new APIContext(accessToken);
            // Use this variant if you want to pass in a request id
            // that is meaningful in your application, ideally
            // a order id.
            /*
             * String requestId = Long.toString(System.nanoTime(); APIContext
             * apiContext = new APIContext(accessToken, requestId ));
             */

            // ###Reauthorization
            // Retrieve a authorization id from authorization object
            // by making a `Payment Using PayPal` with intent
            // as `authorize`. You can reauthorize a payment only once 4 to 29
            // days after 3-day honor period for the original authorization
            // expires.
            Authorization authorization = Authorization.get(apiContext,
					"7GH53639GA425732B");

            // ###Amount
            // Let's you specify a capture amount.
            Amount amount = new Amount();
            amount.setCurrency("USD");
            amount.setTotal("4.54");

            authorization.setAmount(amount);
            // Reauthorize by POSTing to
            // URI v1/payments/authorization/{authorization_id}/reauthorize
            Authorization reauthorization = authorization
            .reauthorize(apiContext);

            LOGGER.info("Reauthorization id = " + reauthorization.getId()
                + " and status = " + reauthorization.getState());
            ResultPrinter.addResult(req, resp, "Reauthorized a Payment", Authorization.getLastRequest(), Authorization.getLastResponse(), null);
        } catch (PayPalRESTException e) {
            ResultPrinter.addResult(req, resp, "Reauthorized a Payment", Authorization.getLastRequest(), null, e.getMessage());
        }
		
    } 
    
    def voidAuthorization(){
        // ###AccessToken
        // Retrieve the access token from
        // OAuthTokenCredential by passing in
        // ClientID and ClientSecret
        APIContext apiContext = null;
        String accessToken = null;
            
        accessToken = GenerateAccessToken.getAccessToken();

        // ### Api Context
        // Pass in a `ApiContext` object to authenticate
        // the call and to send a unique request id
        // (that ensures idempotency). The SDK generates
        // a request id if you do not pass one explicitly.
        apiContext = new APIContext(accessToken);
        // Use this variant if you want to pass in a request id
        // that is meaningful in your application, ideally
        // a order id.
        /*
         * String requestId = Long.toString(System.nanoTime(); APIContext
         * apiContext = new APIContext(accessToken, requestId ));
         */

        // ###Authorization
        // Retrieve a Authorization object
        // by making a Payment with intent
        // as 'authorize'
        Authorization authorization = getAuthorization(apiContext);

        // Void an Authorization
        // by POSTing to 
        // URI v1/payments/authorization/{authorization_id}/void
        Authorization returnAuthorization = authorization.doVoid(apiContext);

    }
    
    // #RefundCapture Sample
    // This sample code demonstrate how you
    // can do a Refund on a Capture
    // resource
    // API used: /v1/payments/capture/{capture_id}/refund
    def refundCapture(){
        // ###AccessToken
        // Retrieve the access token from
        // OAuthTokenCredential by passing in
        // ClientID and ClientSecret
        APIContext apiContext = null;
        String accessToken = null;
        try {
            accessToken = GenerateAccessToken.getAccessToken();

            // ### Api Context
            // Pass in a `ApiContext` object to authenticate
            // the call and to send a unique request id
            // (that ensures idempotency). The SDK generates
            // a request id if you do not pass one explicitly.
            apiContext = new APIContext(accessToken);
            // Use this variant if you want to pass in a request id
            // that is meaningful in your application, ideally
            // a order id.
            /*
             * String requestId = Long.toString(System.nanoTime(); APIContext
             * apiContext = new APIContext(accessToken, requestId ));
             */

            // ###Authorization
            // Retrieve a Authorization object
            // by making a Payment with intent
            // as 'authorize'
            Authorization authorization = getAuthorization(apiContext);
			
            /// ###Capture
            // Create a Capture object
            // by doing a capture on
            // Authorization object
            Capture capture = getCapture(apiContext, authorization);
			
            /// ###Refund
            /// Create a Refund object
            Refund refund = new Refund(); 
			
            // ###Amount
            // Let's you specify a capture amount.
            Amount amount = new Amount();
            amount.setCurrency("USD").setTotal("1");
			
            refund.setAmount(amount);
			
            // Create new APIContext for 
            // Refund
            apiContext = new APIContext(accessToken);
            // Do a Refund by
            // POSTing to 
            // URI v1/payments/capture/{capture_id}/refund
            Refund responseRefund = capture.refund(apiContext, refund); 
			
            LOGGER.info("Refund id = " + responseRefund.getId()
                + " and status = " + responseRefund.getState());
            ResultPrinter.addResult(req, resp, "Refund a Capture", Refund.getLastRequest(), Refund.getLastResponse(), null);
			
        } catch (PayPalRESTException e) {
            ResultPrinter.addResult(req, resp, "Refund a Capture", Refund.getLastRequest(), null, e.getMessage());
        }
    }
    
    def saleRefund(String saleId,Amount amount, APIContext apiContext){
        // ###Sale
        // A sale transaction.
        // Create a Sale object with the
        // given sale transaction id.
        Sale sale = new Sale(); 
        sale.setId(saleId);

        // ###Refund
        // A refund transaction.
        // Use the amount to create
        // a refund object
        Refund refund = new Refund(); 
        // ###Amount
        // Create an Amount object to
        // represent the amount to be
        // refunded. Create the refund object, if the refund is partial
            
        refund.setAmount(amount);
           
			
        // Refund by posting to the APIService
        // using a valid AccessToken
        sale.refund(apiContext, refund);
             
    }
        
    
    /*
     *
     * eg containerMap.put("count", "10");
     **/
    def getPaymentHistory(Map containerMap, String accessToken){
            
        // ###Retrieve
        // Retrieve the PaymentHistory object by calling the
        // static `get` method
        // on the Payment class, and pass the
        // AccessToken and a ContainerMap object that contains
        // query parameters for paginations and filtering.
        // Refer the API documentation
        // for valid values for keys
        return Payment.list(accessToken, containerMap); 
                
    }
    
    // # Get Sale By SaleID  how to get details about a sale.
    // # retrieve 
    // details of completed Sale Transaction.
    // API used: /v1/payments/sale/{sale-id}
    def getSale(String saleId, String accessToken){
            
        // Pass an AccessToken and the ID of the sale
        // transaction from your payment resource.
        return Sale.get(accessToken, saleId); 
                
    }
}
