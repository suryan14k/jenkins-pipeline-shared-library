import org.devops.api.clients.*

def call(step, props, projectName, assetVersion){
    step.println("creating api in Api Manager.")
    def apiManagerClient = new ApiManagerClient(this, props)
    def common = new Common(this, props)
    def token = common.getAnypointToken()
    def api = apiManagerClient.searchApi(token, projectName)
    if (api == "api_not_found") {
        step.println("api not found")
        apiManagerClient.createApi(token, projectName, assetVersion)
    } else {
        step.println("api found, running update.")
    }
    step.println("Published asset to exchange successfully.")
}
