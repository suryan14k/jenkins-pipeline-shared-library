package org.devops.api.clients

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class ApiManagerClient {

    private def step
    private def props
    ApiManagerClient(step, props)
    {
        this.step = step
        this.props = props
    }
    def searchApi(token, assetName){
        assetName = assetName.toLowerCase().replace(" ", "-")
        step.println("search api")
        def urlString = "https://anypoint.mulesoft.com/apimanager/api/v1/organizations/${props.organizationId}/environments/${props.environmentId}/apis?assetId=${assetName}"
        def headers=["Content-Type": "application/json","Accept": "application/json","Authorization": "Bearer " + token]
        def connection = ApiClient.get(urlString, headers)
        if (connection.responseCode == 200) {
            def response = new JsonSlurper().parseText(connection.getInputStream().getText())
            if(response.total == 1){
                step.println("success: api details retrived: ${response}")
                return response
            }else{
                return "api_not_found"
            }
        } else {
            step.println("failed - status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("failed to create api!")
        }
    }
    def createApi(token, assetName, assetVersion){
        assetName = assetName = assetName.toLowerCase().replace(" ", "-")
        step.println("create api")
        def requestTemplate = "{\n" +
                "   \"endpoint\": {\n" +
                "       \"type\": null,\n" +
                "       \"uri\": null,\n" +
                "       \"proxyUri\": null,\n" +
                "       \"muleVersion4OrAbove\": null,\n" +
                "       \"isCloudHub\": null\n" +
                "   },\n" +
                "   \"instanceLabel\": null,\n" +
                "   \"spec\": {\n" +
                "       \"assetId\": null,\n" +
                "       \"version\": null,\n" +
                "       \"groupId\": null\n" +
                "    }\n" +
                "}"
        def request = new JsonSlurper().parseText(requestTemplate)
        request.endpoint.type = "rest-api"
        request.endpoint.muleVersion4OrAbove = true
        request.endpoint.isCloudHub = true

        request.spec.assetId = assetName
        request.spec.version = assetVersion
        request.spec.groupId = props.organizationId

        def body = JsonOutput.toJson(request)
        def urlString = "https://anypoint.mulesoft.com/apimanager/api/v1/organizations/${props.organizationId}/environments/${props.environmentId}/apis"
        def headers=["Content-Type": "application/json","Accept": "application/json","Authorization": "Bearer " + token]
        def connection = ApiClient.post(urlString, body, headers)
        step.println("api create request ${body}, url : ${urlString}")
        if (connection.responseCode == 201) {
            def api = new JsonSlurper().parseText(connection.getInputStream().getText())
            step.println("success: api created: ${api}")
            return api
        } else {
            step.println("failed - status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("failed to create api!")
        }
    }
}