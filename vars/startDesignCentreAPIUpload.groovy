import org.devops.api.clients.ApiDesignCenterClient

def call(step, props, projectName, apiDirPath) {
    step.println("Design Centre API Upload Started")
    step.println("working branch is master")
    def branch = "master"
    def apiDesignCenterClient = new ApiDesignCenterClient(this, props)
    def token = apiDesignCenterClient.getAnypointToken()
    def project = apiDesignCenterClient.getProjects(token, projectName)
    if (project == "not_found") {
        step.println("project not found, create new project.")
        def projectDetails = apiDesignCenterClient.createProject(token, projectName)
        def projectId = projectDetails.id
        def branchId = projectDetails.initialWorkingDirectory.id
        apiDesignCenterClient.acquireLockOnProject(token, projectId, branch)
        try {
            apiDesignCenterClient.uploadExchangeDependencyArtifacts(token, projectId, branch, apiDirPath)
            apiDesignCenterClient.uploadArtifacts(token, projectId, branch, apiDirPath)
        } catch (Exception e) {
            apiDesignCenterClient.releaseLockOnProject(token, projectId, branch)
            throw new Exception("Design Centre API Upload failed")
        }
    } else {
        step.println("project found.")
        def projectId = project.id
        def branchId = apiDesignCenterClient.getBranchCommitId(token, projectId, branch)
        apiDesignCenterClient.branchBackUp(token, projectId, branch, branchId)
        apiDesignCenterClient.acquireLockOnProject(token, projectId, branch)
        try {
            apiDesignCenterClient.branchCleanUp(token, projectId, branch)
            apiDesignCenterClient.uploadExchangeDependencyArtifacts(token, projectId, branch, apiDirPath)
            apiDesignCenterClient.uploadArtifacts(token, projectId, branch, apiDirPath)
        } catch (Exception e) {
            apiDesignCenterClient.releaseLockOnProject(token, projectId, branch)
            throw new Exception("Design Centre API Upload failed")
        }
    }
    step.println("Design Centre API Upload completed")
}