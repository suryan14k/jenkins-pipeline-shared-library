import org.devops.api.clients.ApiDesignCenterClient

def call(step, props, projectName, branch, apiDirPath){
    step.println("Step is working")
    def token = ApiDesignCenterClient.getAnypointToken(props)
    def projectId = ApiDesignCenterClient.getProjectID(props, token, projectName)
    ApiDesignCenterClient.saveProjectFiles(props, token,projectId, branch, apiDirPath)
}
