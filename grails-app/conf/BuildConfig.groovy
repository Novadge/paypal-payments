grails.project.work.dir = 'target'

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {
    inherits 'global'
    log 'warn'
    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        String httpcomponentsVersion = '4.3.6'
        for (name in ['fluent-hc', 'httpclient', 'httpclient-cache', 'httpmime']) {
            compile "org.apache.httpcomponents:$name:$httpcomponentsVersion"
        }

        runtime 'com.paypal.sdk:rest-api-sdk:LATEST'
    }

    plugins {
        build(':release:3.1.1', ':rest-client-builder:2.1.1') {
            export = false
        }
    }
}
