import org.devops.api.clients.*

def call(step, props, apiType, environmentApiId){
    step.println("applying policies")
    def apiManagerClient = new ApiManagerClient(this, props)
    def common = new Common(this, props)
    def token = common.getAnypointToken()
    if(apiType == "system"){
        //client-id-enforcement//
        def request = "{\n" +
                "   \"configurationData\":{\n" +
                "   \"credentialsOrigin\":\"customExpression\",\n" +
                "   \"clientIdExpression\":\"#[attributes.headers['clientId']]\",\n" +
                "   \"clientSecretExpression\":\"#[attributes.headers['clientSecret']]\"\n" +
                "   },\n" +
                "   \"pointcutData\":null,\n" +
                "   \"assetId\":\"client-id-enforcement\",\n" +
                "   \"assetVersion\":\"1.2.4\",\n" +
                "   \"groupId\":\"68ef9520-24e9-4cf2-b2f5-620025690913\"\n" +
                "}"
        apiManagerClient.applyPolicy(token, environmentApiId, request)
    }
    if(apiType == "process"){
        //client-id-enforcement//
        def request = "{\n" +
                "   \"configurationData\":{\n" +
                "   \"credentialsOrigin\":\"customExpression\",\n" +
                "   \"clientIdExpression\":\"#[attributes.headers['clientId']]\",\n" +
                "   \"clientSecretExpression\":\"#[attributes.headers['clientSecret']]\"\n" +
                "   },\n" +
                "   \"pointcutData\":null,\n" +
                "   \"assetId\":\"client-id-enforcement\",\n" +
                "   \"assetVersion\":\"1.2.4\",\n" +
                "   \"groupId\":\"68ef9520-24e9-4cf2-b2f5-620025690913\"\n" +
                "}"
        apiManagerClient.applyPolicy(token, environmentApiId, request)
    }
    if(apiType == "experience"){
        //client-id-enforcement//
        def request = "{\n" +
                "   \"configurationData\":{\n" +
                "   \"credentialsOrigin\":\"customExpression\",\n" +
                "   \"clientIdExpression\":\"#[attributes.headers['clientId']]\",\n" +
                "   \"clientSecretExpression\":\"#[attributes.headers['clientSecret']]\"\n" +
                "   },\n" +
                "   \"pointcutData\":null,\n" +
                "   \"assetId\":\"client-id-enforcement\",\n" +
                "   \"assetVersion\":\"1.2.4\",\n" +
                "   \"groupId\":\"68ef9520-24e9-4cf2-b2f5-620025690913\"\n" +
                "}"
        apiManagerClient.applyPolicy(token, environmentApiId, request)
    }
    step.println("applied policies successfully.")
}

