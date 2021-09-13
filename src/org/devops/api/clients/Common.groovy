package org.devops.api.clients

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class Common {

    def step
    def props

    Common(step, props)
    {
        this.step = step
        this.props = props
    }
    def getAnypointToken()
    {
        step.println("get anypoint token")
        def requestTemplate = '{"username" : null,"password" : null }'
        def request = new JsonSlurper().parseText(requestTemplate)
        request.username = props.username
        request.password = props.password
        def body = JsonOutput.toJson(request)
        def urlString = "https://anypoint.mulesoft.com/accounts/login"
        def headers=["Content-Type": "application/json","Accept": "application/json","Cookie": "..."]
        def connection = ApiClient.post(urlString, body, headers)
        if (connection.responseCode == 200)
        {
            def token = new JsonSlurper().parseText(connection.getInputStream().getText()).access_token
            step.println("login success")
            return token
        }else
        {
            step.println("failed - status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("Failed to get the login token!")
        }

    }
}
