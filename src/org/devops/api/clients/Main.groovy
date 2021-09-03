package org.devops.api.clients

class Main {
    static void main(String[] args) {
        def username = "***"
        def password = "***"
        def organizationId = "c8a97a61-f4c4-4e40-a2b6-ba13718b421c"
        def ownerId = "2cc24e16-4c9c-4ce5-ab0a-346f1d3ed80c"
        def projectName = "Hello"
        def branch = "master"
        def apiDirPath = "***"

        def props = [
                'username': username,
                'password': password ,
                'organizationId': organizationId,
                'ownerId': ownerId
        ]

        startDesignCentreAPIUpload(this, props,projectName, branch, apiDirPath)
    }
    def static startDesignCentreAPIUpload(step, props, projectName, branch, apiDirPath){
        step.println("Design Centre API Upload Started")
        def apiDesignCenterClient = new ApiDesignCenterClient(this, props)
        def token = apiDesignCenterClient.getAnypointToken()
        def projectId = apiDesignCenterClient.getProjectID(token, projectName)
        //def branchId = apiDesignCenterClient.getBranchCommitId(token, projectId, branch)
        //apiDesignCenterClient.branchBackUp(token, projectId, branch, branchId)
        //apiDesignCenterClient.branchCleanUp(token, projectId, branch)
        apiDesignCenterClient.addProjectFiles(token,projectId, branch, apiDirPath)
        step.println("Design Centre API Upload completed")
    }
}
