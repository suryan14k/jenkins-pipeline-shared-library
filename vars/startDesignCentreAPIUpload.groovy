import org.devops.api.clients.ApiDesignCenterClient

def call(step, props, projectName, branch, apiDirPath){
    step.println("Design Centre API Upload Started")
    def apiDesignCenterClient = new ApiDesignCenterClient(this, props)
    def token = apiDesignCenterClient.getAnypointToken()
    def projectId = apiDesignCenterClient.getProjectID(token, projectName)
    def branchId = apiDesignCenterClient.getBranchCommitId(token, projectId, branch)
    apiDesignCenterClient.branchBackUp(token, projectId, branch, branchId)
    apiDesignCenterClient.branchCleanUp(token, projectId, branch)
    apiDesignCenterClient.uploadExchangeDependencyArtifacts(token,projectId, branch, apiDirPath)
    apiDesignCenterClient.uploadArtifacts(token,projectId, branch, apiDirPath)
    step.println("Design Centre API Upload completed")
}
