# paypal payments
Accept and process payments with Paypal REST Api

### Introduction to Paypal payments plugin.
The Paypal payments plugin simplifies the integration of Paypal into Grails applications. With this plugin, you are not required to be a Paypal sdk guru in order to accept payments.

This guide documents how to use the paypal payments plugin to process payments for Grails Applications. 

### Getting Started
I will assume that you already have your Grails application designed and ready to accept payments. A demo application will be sufficient for this exercise.  

####Create a paypal account
You will need to create a paypal account and obtain API keys for a Paypal app. The Paypal app will represent your Grails application. To create a paypal app, please visit [https://developer.paypal.com/developer/applications/](Paypal developer page) to create a sandbox account. 

The Sandbox account will allow you to play around with most of the API features available. It is also a good way to test your application integration before moving over to a live environment. 

####Obtain your client id and client secret
Obtain the API keys for your sandbox account/app and add it to your grails config file [Config.groovy for grails 2.x and application.groovy for grails 3.x]. Here's what my config file looks like: 

`
paypal.email="omasiri@novadge.com"
paypal.clientId = 'your client id'
paypal.sandbox.clientId = 'your client id'
paypal.clientSecret = 'your client secret'
paypal.sandbox.clientSecret ='your client secret'
paypal.endpoint = "https://api.paypal.com"
paypal.sandbox.endpoint = "https://api.sandbox.paypal.com"

`

Notice that I added config for sandbox and live environment. The reason is to be able to switch between both environments  during app development. 

####Create a grails controller and add required actions
Create a grails controller. Personally, I called my own controller PaypalController.


`grails create-controller com.mypackage.Paypal
`

####Add required actions

Normally, Paypal payments requires three steps to complete.
1. Make approval request to paypal.
2. Approval - Customer approves the payment
3. Execution - Process response from Paypal in order to capture the payment.

### Approval Step

For the approval step, here's my action inside my PaypalController . 
Inject paypalService into your controller like this...

`def paypalService`


And then create your Controller action 
`
import com.paypal.base.Constants; 
...
    def approve(){
       
        
        String clientId = grailsApplication.config.paypal.clientId
        String clientSecret = grailsApplication.config.paypal.clientSecret
        String endpoint = grailsApplication.config.paypal.endpoint
        Map sdkConfig = [:] 
        sdkConfig.put(Constants.CLIENT_ID,clientId)
        sdkConfig.put(Constants.CLIENT_SECRET,clientSecret)
        sdkConfig.put(Constants.ENDPOINT,endpoint)
        def accessToken = paypalService.getAccessToken(clientId,clientSecret,sdkConfig)
        def apiContext = paypalService.getAPIContext(accessToken,sdkConfig)
        
        
        
        BigDecimal total = formatNumber(number:params.amount,minFractionDigits:2) as BigDecimal
                
        def details = paypalService.createDetails(['subtotal':"12.50"])
        def amount = paypalService.createAmount(['currency':currencyCode,'total':"12.50",'details':details])
        
        def transaction = paypalService.createTransaction(['amount':amount,'description':"your description",details:details])
        def transactions = []
        transactions.add(transaction)
        
        def payer = paypalService.createPayer(['paymentMethod':'paypal'])
        def cancelUrl="http://myexampleurl/cancel";
        def returnUrl = "http://mypaypalController/execute";

        def redirectUrls = paypalService.createRedirectUrls(['cancelUrl':cancelUrl,'returnUrl':returnUrl])

  
        def payment
        try{
            // create the paypal payment
            payment = paypalService.createPayment(['payer':payer,'intent':'sale'
                    ,'transactionList':transactions,'redirectUrls':redirectUrls
                    ,'apiContext':apiContext])
            


        }
        catch(Exception ex){
            String msg = ex.getMessage()
            flash.message = "Could not complete the transaction because: ${msg? msg : ''}"  
            
            redirect controller:'bill', action:"show",id:params['refId']
            return
        }
       
        def approvalUrl = ""
        def retUrl = ""
        // retrieve links from returned paypal object
        payment?.links.each{ 
            if(it?.rel == 'approval_url'){
                approvalUrl = it.href
            }
            if(it?.rel == 'return_url'){
                retUrl = it.href

            }
        }


        redirect url:approvalUrl? approvalUrl:'/', method:'POST'

    }
`


### Approval
The customer will be redirected to the Paypal website for approval. After the customer approves or 
cancels the payment, Paypal will either call the returnUrl or cancelUrl you provided depending on 
what action the customer performs. 

`
    def execute(){
        
        String clientId = grailsApplication.config.paypal.clientId
        String clientSecret = grailsApplication.config.paypal.clientSecret
        String endpoint = grailsApplication.config.paypal.endpoint
        Map sdkConfig = [:] //= grailsApplication.config.paypal.sdkConfig//['mode':'live']
        //sdkConfig.put("grant-type","client_credentials")
        sdkConfig.put(Constants.CLIENT_ID,clientId)
        sdkConfig.put(Constants.CLIENT_SECRET,clientSecret)
        sdkConfig.put(Constants.ENDPOINT,endpoint)
        def accessToken = paypalService.getAccessToken(clientId,clientSecret,sdkConfig)
        def apiContext = paypalService.getAPIContext(accessToken,sdkConfig)
        //the paypal website will add params to the call to your app. Eg. PayerId, PaymentId
        // you will use the params to 'execute' the payment
        def paypalPayment = paypalService.createPaymentExecution(['paymentId':params.paymentId,'payerId':params?.PayerID],apiContext)
        
        JsonSlurper slurper = new JsonSlurper()
        def map = slurper.parseText(paypalPayment.toString())

        redirect(url:"to your url")
    }
`
### Authors and Contributors
Omasirichukwu Joseph Udeinya (@omasiri) 

### Support or Contact
Please feel free to reach out to us for assistance with this plugin and we’ll help you sort it out.
