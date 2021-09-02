import org.devops.api.clients.ApiDesignCenterClient

def call(step, props, projectName, branch, apiDirPath){
    def apiDesignCenterClient = new ApiDesignCenterClient(this, props)
    def token = apiDesignCenterClient.getAnypointToken()
    def projectId = apiDesignCenterClient.getProjectID(token, projectName)
    apiDesignCenterClient.saveProjectFiles(token,projectId, branch, apiDirPath)
}
