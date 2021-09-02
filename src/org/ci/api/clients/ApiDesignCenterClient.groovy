package org.ci.api.clients

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.io.FileType
import groovy.util.logging.Slf4j

@Slf4j
class ApiDesignCenterClient {

    static void main(String[] args) {

        def username = ""
        def password = ""
        def organizationId = "c8a97a61-f4c4-4e40-a2b6-ba13718b421c"
        def ownerId = "2cc24e16-4c9c-4ce5-ab0a-346f1d3ed80c"
        def projectName = "Jenkins"
        def branch = "master"
        def apiDirPath = "C:\\ci\\api"


        def props = ['username': username,'password': password ]
        def token = getAnypointToken(props)
        //println("token is : " + token)

        props = ['organizationId': organizationId]
        def projectId = getProjectID(props, token, projectName)
       // println("project id is: " + projectId)

        props = ['organizationId': organizationId, 'ownerId': ownerId]
        //def acquireLockStatus = acquireLockOnProject(props, token, projectId, branch)
        //println("acquire lock status is: " + acquireLockStatus)

        //def releaseLockStatus = releaseLockOnProject(props, token,projectId, branch)
        //println("release lock status is: " + releaseLockStatus)

        //def lockStatus = checkProjectLockStatus(props, token,projectId, branch)
        //println("check lock status is: " + lockStatus)

        def saveFilesStatus = saveProjectFiles(props, token,projectId, branch, apiDirPath)
        //println("saved files " + saveFilesStatus)
    }

    static def getAnypointToken(props)
    {
        log.info("get anypoint token")
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
            log.info("login success")
            return token
        }else
        {
            log.error("failed - status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("Failed to get the login token!")
        }

    }

    static def getProjectID(props, token, projectName)
    {
        log.info("get project id")
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "Authorization": "Bearer " + token]
        def connection = ApiClient.get(urlString, headers)
        if (connection.responseCode == 200) {
            def projectDetails = new JsonSlurper().parseText(connection.getInputStream().getText())
            def filteredProject = projectDetails.find { it -> (it.name == projectName) }
            log.info("success: retrieved project id: ${filteredProject.id}")
            return filteredProject.id
        } else {
            log.error("failed - status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("Failed to retrieve project details!")
        }
    }

    static def acquireLockOnProject(props, token, projectId, branch)
    {
        log.info("acquire project lock")
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches/" + branch + "/acquireLock"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "x-owner-id":props.ownerId, "Authorization": "Bearer " + token]
        def connection = ApiClient.post(urlString, null, headers)
        if (connection.responseCode == 200) {
            log.info("success: project lock acquired")
            def lockStatus = new JsonSlurper().parseText(connection.getInputStream().getText()).locked
            return lockStatus
        } else {
            log.error("failed - status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("unable to obtain lock.")
        }

    }

    static def releaseLockOnProject(props, token, projectId, branch)
    {
        log.info("release project lock")
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches/" + branch + "/releaseLock"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "x-owner-id":props.ownerId, "Authorization": "Bearer " + token]
        def connection = ApiClient.post(urlString, null, headers)
        if (connection.responseCode == 200) {
            log.info("success: project lock released")
            def lockStatus = new JsonSlurper().parseText(connection.getInputStream().getText()).locked
            return lockStatus
        } else {
            log.error("failed - status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("unable to release lock.")
        }
    }

    static def checkProjectStatus(props, token, projectId, branch)
    {
        log.info("get project status")
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches/" + branch + "/status"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "x-owner-id":props.ownerId, "Authorization": "Bearer " + token]
        def connection = ApiClient.post(urlString, null, headers)
        if (connection.responseCode == 200) {
            def status = new JsonSlurper().parseText(connection.getInputStream().getText())
            log.info("success: retrieved project status ${status}")
            return status
        } else {
            log.error("status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("unable to retrieve project status.")
        }
    }

    static def saveProjectFiles(props, token, projectId, branch, apiDirPath)
    {
        log.info("save project files")
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

        acquireLockOnProject(props, token, projectId, branch)
        if (connection.responseCode == 200) {
            def savedFilesStatus = new JsonSlurper().parseText(connection.getInputStream().getText())
            log.info("success: saved project files are ${savedFilesStatus}")
            releaseLockOnProject(props, token, projectId, branch)
            return savedFilesStatus
        } else {
            log.error("failed - status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            releaseLockOnProject(props, token, projectId, branch)
            throw new Exception("unable to save project files.")
        }
    }

    static addFilesIntoMultiPartClient(File apiBaseDir, File apiBaseDirCopy, apiClient) {
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