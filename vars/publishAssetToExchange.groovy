import org.devops.api.clients.*

def call(step, props, projectName, apiVersion, assetVersion){
    step.println("Publishing asset to exchange.")
    step.println("working branch is master")
    def branch = "master"
    def apiDesignCenterClient = new ApiDesignCenterClient(this, props)
    def common = new Common(this, props)
    def token = common.getAnypointToken()
    def project = apiDesignCenterClient.getProjects(token, projectName)
    if (project == "not_found") {
        step.println("project not found, Check project in design centre.")
        throw new Exception("asset publish failed")
    } else {
        step.println("project found.")
        def projectId = project.id
        apiDesignCenterClient.acquireLockOnProject(token, projectId, branch)
        try {
            apiDesignCenterClient.publishToExchange(token, projectId, branch, projectName, apiVersion, assetVersion)
        } catch (Exception e) {
            apiDesignCenterClient.releaseLockOnProject(token, projectId, branch)
            throw new Exception("asset publish failed")
        }
    }
    step.println("Published asset to exchange successfully.")
}
