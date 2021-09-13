package org.devops.api.clients

import groovy.json.JsonSlurper

class ApiExchangeClient {

    private def step
    private def props

    ApiExchangeClient(step, props){
        this.step = step
        this.props=props
    }
    def assetsSearch(token, assetName){
        assetName = assetName.toLowerCase().replace(" ","-")
        def urlString = "https://anypoint.mulesoft.com/exchange/api/v2/assets/search?search=${assetName}&organizationId=${props.organizationId}"
        def headers = ["Authorization": "Bearer ${token}"]
        def connection = ApiClient.get(urlString, headers)
        if (connection.responseCode == 200) {
            def response = new JsonSlurper().parseText(connection.getInputStream().getText())
            if(response.iterator().size() > 0){
               return response
           }
            else{
               throw new Exception("asset_not_found")
           }
        }
        else{
            throw new Exception("unknown error")
        }
    }
}
