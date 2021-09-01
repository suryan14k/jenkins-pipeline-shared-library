package org.ci.api.clients

import com.cloudbees.groovy.cps.NonCPS

//@Grab('org.codehaus.groovy:groovy-json:2.0.1')

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.io.FileType

class ApiDesignCenterClient {



static void main(String[] args) {
        def username = ""
        def password = ""
        def organizationId = ""
        def ownerId = ""
        def projectName = ""
        def branch = "master"
        def apiDirPath = ""


        def props = ['username': username,'password': password ]
        def token = getAnypointToken(props)
        println("token is : " + token)

        props = ['organizationId': organizationId]
        def projectId = getProjectID(props, token, projectName)
        println("project id is: " + projectId)

        props = ['organizationId': organizationId, 'ownerId': ownerId]
        def acquireLockStatus = acquireLockOnProject(props, token, projectId, branch)
        println("acquire lock status is: " + acquireLockStatus)

        def releaseLockStatus = releaseLockOnProject(props, token,projectId, branch)
        println("release lock status is: " + releaseLockStatus)

        def lockStatus = checkProjectLockStatus(props, token,projectId, branch)
        println("check lock status is: " + lockStatus)

        def saveFilesStatus = saveProjectFiles(props, token,projectId, branch, apiDirPath)
        println("saved files " + saveFilesStatus)
    }

    static def getAnypointToken(props)
    {
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
            return token
        }else
        {
            throw new Exception("Failed to get the login token!")
        }

    }

    static def getProjectID(props, token, projectName)
    {
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "Authorization": "Bearer " + token]
        def connection = ApiClient.get(urlString, headers)
        if (connection.responseCode == 200) {
            def projectDetails = new JsonSlurper().parseText(connection.getInputStream().getText())
            def filteredProject = projectDetails.find { it -> it.name == projectName}
            return filteredProject.id

        } else {
            throw new Exception("Failed to retrieve project details!")
        }
    }

    static def acquireLockOnProject(props, token, projectId, branch)
    {
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches/" + branch + "/acquireLock"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "x-owner-id":props.ownerId, "Authorization": "Bearer " + token]
        def connection = ApiClient.post(urlString, null, headers)
        if (connection.responseCode == 200) {
            def locked = new JsonSlurper().parseText(connection.getInputStream().getText()).locked
            return locked
        } else {
            throw new Exception("unable to obtain lock.")
        }

    }

    static def releaseLockOnProject(props, token, projectId, branch)
    {
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches/" + branch + "/releaseLock"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "x-owner-id":props.ownerId, "Authorization": "Bearer " + token]
        def connection = ApiClient.post(urlString, null, headers)
        if (connection.responseCode == 200) {
            def locked = new JsonSlurper().parseText(connection.getInputStream().getText()).locked
            return locked
        } else {
            throw new Exception("unable to release lock.")
        }
    }

    static def checkProjectLockStatus(props, token, projectId, branch)
    {
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches/" + branch + "/status"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "x-owner-id":props.ownerId, "Authorization": "Bearer " + token]
        def connection = ApiClient.post(urlString, null, headers)
        if (connection.responseCode == 200) {
            def status = new JsonSlurper().parseText(connection.getInputStream().getText())
            return status
        } else {
            throw new Exception("unable to release lock.")
        }
    }


    static def saveProjectFiles(props, token, projectId, branch, apiDirPath)
    {
        def boundary = "*****"
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches/" + branch + "/save/v2"
        def headers= ["x-organization-id": props.organizationId, "x-owner-id": props.ownerId, "Authorization": "Bearer " + token, "Content-Type" : "multipart/form-data;boundary=" + boundary]
        def apiClient = new ApiMultiPartDataClient()
        def connection = apiClient.getConnection(urlString, headers)
        File apiBaseDir = new File(apiDirPath)

        addFilesIntoMultiPartClient(apiBaseDir, apiBaseDir, apiClient)

        //Extra Custom Function to avoid NonCPS Issue.
        /*apiBaseDir.eachFileRecurse(FileType.FILES)  {
            def fileName = getModifiedFileName(apiBaseDir, it)
            apiClient.addFilePart(fileName, it)
        }*/

        apiClient.finish()
        def acquireLockStatus = acquireLockOnProject(props, token, projectId, branch)
        if (connection.responseCode == 200) {
            def savedFilesStatus = new JsonSlurper().parseText(connection.getInputStream().getText())
            releaseLockOnProject(props, token, projectId, branch)
            return savedFilesStatus
        } else {
            releaseLockOnProject(props, token, projectId, branch)
            throw new Exception("unable to save files.")
        }
    }

    static void addFilesIntoMultiPartClient(File apiBaseDir, File apiBaseDirCopy, apiClient) {
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
        return relFileName.replace('\\',"/")
    }
}