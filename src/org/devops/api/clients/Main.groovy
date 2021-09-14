package org.devops.api.clients

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class Main {

    static void main(String[] args) {
        def username = ""
        def password = ""
        def organizationId = "c8a97a61-f4c4-4e40-a2b6-ba13718b421c"
        def ownerId = "2cc24e16-4c9c-4ce5-ab0a-346f1d3ed80c"
        def environmentId = "2c6abb1e-23e9-4d25-8f8f-0d6b09c03be2"
        def projectName = "Test"
        def branch = "master"
        def apiDirPath = "C:\\ci\\api"

        def props = [
                'username': username,
                'password': password ,
                'organizationId': organizationId,
                'ownerId': ownerId,
                'environmentId': environmentId
        ]
        def assetVersion = "1.0.0"
        def assetName = "api-poc"
        def common = new Common(this, props)
        def token = common.getAnypointToken()
        def client =  new ApiManagerClient(this, props)
        def apiResponse = client.searchApi(token, assetName)
        if(apiResponse == "api_not_found"){
            client.createApi(token, assetName, assetVersion)
        }
    }
}
