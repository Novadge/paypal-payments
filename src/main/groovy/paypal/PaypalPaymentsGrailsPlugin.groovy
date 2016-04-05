package paypal

import grails.plugins.Plugin

class PaypalPaymentsGrailsPlugin extends Plugin {
    def grailsVersion = "3.1.1 > *"
    def title = "Paypal Payments"
    def author = "Omasirichukwu Udeinya"
    def authorEmail = "omasiri@novadge.com"
    def description = 'Accept and process payments with Paypal REST API'
    def profiles = ['web']
    def documentation = "http://novadge.github.io/paypal-payments/"
    def license = "APACHE"
    def organization = [name: "Novadge", url: "http://www.novadge.com/"]
    def issueManagement = [url: "https://github.com/Novadge/paypal-payments/issues"]
    def scm = [url: "https://github.com/Novadge/paypal-payments/"]
}
