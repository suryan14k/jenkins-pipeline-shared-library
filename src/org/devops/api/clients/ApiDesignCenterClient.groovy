package org.devops.api.clients

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

class ApiDesignCenterClient {

    def step
    def props

    ApiDesignCenterClient(step, props)
    {
        this.step = step
        this.props = props
    }
    def getAnypointToken()
    {
        step.println("get anypoint token")
        def requestTemplate = '{"username" : null,"password" : null }'
        def request = new JsonSlurper().parseText(requestTemplate)
        request.username = props.username
        request.password = props.password
        def body = JsonOutput.toJson(request)
        def urlString = "https://anypoint.mulesoft.com/accounts/login"
        def headers=["Content-Type": "application/json","Accept": "application/json","Cookie": "..."]
        def connection = ApiClient.post(urlString, body, headers)
        if (connection.responseCode == 200)
        {
            def token = new JsonSlurper().parseText(connection.getInputStream().getText()).access_token
            step.println("login success")
            return token
        }else
        {
            step.println("failed - status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("Failed to get the login token!")
        }

    }

    def getProjectID(token, projectName)
    {
        step.println("get project id")
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "Authorization": "Bearer " + token]
        def connection = ApiClient.get(urlString, headers)
        if (connection.responseCode == 200) {
            def projectDetails = new JsonSlurper().parseText(connection.getInputStream().getText())
            def filteredProject = projectDetails.find { it -> (it.name == projectName) }
            step.println("success: retrieved project id: ${filteredProject.id}")
            return filteredProject.id
        } else {
            step.println("failed - status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("Failed to retrieve project details!")
        }
    }

    def acquireLockOnProject(token, projectId, branch)
    {
        step.println("acquire project lock")
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches/" + branch + "/acquireLock"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "x-owner-id":props.ownerId, "Authorization": "Bearer " + token]
        def connection = ApiClient.post(urlString, null, headers)
        if (connection.responseCode == 200) {
            step.println("success: project lock acquired")
            def lockStatus = new JsonSlurper().parseText(connection.getInputStream().getText()).locked
            return lockStatus
        } else {
            step.println("failed - status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("unable to obtain lock.")
        }

    }

    def releaseLockOnProject(token, projectId, branch)
    {
        step.println("release project lock")
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches/" + branch + "/releaseLock"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "x-owner-id":props.ownerId, "Authorization": "Bearer " + token]
        def connection = ApiClient.post(urlString, null, headers)
        if (connection.responseCode == 200) {
            step.println("success: project lock released")
            def lockStatus = new JsonSlurper().parseText(connection.getInputStream().getText()).locked
            return lockStatus
        } else {
            step.println("failed - status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("unable to release lock.")
        }
    }

    def checkProjectStatus(token, projectId, branch)
    {
        step.println("get project status")
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches/" + branch + "/status"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "x-owner-id":props.ownerId, "Authorization": "Bearer " + token]
        def connection = ApiClient.post(urlString, null, headers)
        if (connection.responseCode == 200) {
            def status = new JsonSlurper().parseText(connection.getInputStream().getText())
            step.println("success: retrieved project status ${status}")
            return status
        } else {
            step.println("status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("unable to retrieve project status.")
        }
    }

    def saveProjectFiles(token, projectId, branch, apiDirPath)
    {
        step.println("save project files")
        def boundary = "*****"
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches/" + branch + "/save/v2"
        def headers= ["x-organization-id": props.organizationId, "x-owner-id": props.ownerId, "Authorization": "Bearer " + token]
        def apiMultiPartDataClient = new ApiMultiPartDataClient()
        def connection = apiMultiPartDataClient.getConnection(urlString, headers)
        File apiBaseDir = new File(apiDirPath)
        addFilesIntoMultiPartClient(apiBaseDir, apiBaseDir, apiMultiPartDataClient)
        apiMultiPartDataClient.finish()

        //eachFileRecurse not works in jenkins.
        /*apiBaseDir.eachFileRecurse(FileType.FILES)  {
            def fileName = getModifiedFileName(apiBaseDir, it)
            apiMultiPartDataClient.addFilePart(fileName, it)
        }*/

        acquireLockOnProject(token, projectId, branch)
        if (connection.responseCode == 200) {
            def savedFilesStatus = new JsonSlurper().parseText(connection.getInputStream().getText())
            step.println("success: saved project files are ${savedFilesStatus}")
            releaseLockOnProject(token, projectId, branch)
            return savedFilesStatus
        } else {
            step.println("failed - status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            releaseLockOnProject(token, projectId, branch)
            throw new Exception("unable to save project files.")
        }
    }

    private static addFilesIntoMultiPartClient(File apiBaseDir, File apiBaseDirCopy, apiClient) {
        for (File fileEntry : apiBaseDir.listFiles()) {
            if (fileEntry.isDirectory()) {
                addFilesIntoMultiPartClient(fileEntry, apiBaseDirCopy, apiClient)
            } else {
                def fileName = getModifiedFileName(apiBaseDirCopy, fileEntry)
                apiClient.addFilePart(fileName, fileEntry)
            }
        }
    }

    private static def getModifiedFileName(baseDir, filePath)
    {
        def rootLength = baseDir.getAbsolutePath().length()
        def absFileName = filePath.getAbsolutePath()
        def relFileName = absFileName.substring(rootLength + 1)
        return relFileName.replace(File.separator,"/")
    }
}