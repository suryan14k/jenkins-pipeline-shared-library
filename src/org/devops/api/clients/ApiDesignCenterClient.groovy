package org.devops.api.clients

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.text.SimpleDateFormat

class ApiDesignCenterClient {

    def step
    def props

    ApiDesignCenterClient(step, props)
    {
        this.step = step
        this.props = props
    }

    def getProjects(token, projectName)
    {
        step.println("get projects")
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "Authorization": "Bearer " + token]
        def connection = ApiClient.get(urlString, headers)
        if (connection.responseCode == 200) {
            def projectDetails = new JsonSlurper().parseText(connection.getInputStream().getText())
            def project = projectDetails.find { it -> (it.name == projectName && it.type == "raml") }
            if(project != null) {
                step.println("success: retrieved project id: ${project}")
                return project
            }
            else{
                return "not_found"
            }
        } else {
            step.println("failed - status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("Failed to retrieve project details!")
        }
    }

    def createProject(token, projectName){
        step.println("create project")
        def requestTemplate = '{"name" : null,"description" : null, "classifier" : null, "type" : null }'
        def request = new JsonSlurper().parseText(requestTemplate)
        request.name = projectName
        request.description = projectName
        request.classifier = "raml"
        request.type = "raml"
        def body = JsonOutput.toJson(request)
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId,  "x-owner-id":props.ownerId, "Authorization": "Bearer " + token]
        def connection = ApiClient.post(urlString, body, headers)
        if (connection.responseCode == 201) {
            def project = new JsonSlurper().parseText(connection.getInputStream().getText())
            step.println("success: project created: ${project}")
            return project
        } else {
            step.println("failed - status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("failed to create project!")
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

    def getBranchCommitId(token, projectId, branch)
    {
        step.println("get branch commit id")
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "x-owner-id":props.ownerId, "Authorization": "Bearer " + token]
        def connection = ApiClient.get(urlString, headers)
        if (connection.responseCode == 200) {
            def branches = new JsonSlurper().parseText(connection.getInputStream().getText())
            def filteredBranch = branches.find { it -> (it.name == branch) }
            step.println("success: retrieved branch commit id: ${filteredBranch.commitId}")
            return filteredBranch.id
        } else {
            step.println("status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("unable to retrieve branch commit id.")
        }
    }

    def branchBackUp(token, projectId, branch, commitId)
    {
        step.println("create branch back up")
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "x-owner-id":props.ownerId, "Authorization": "Bearer " + token]
        def requestTemplate = '{"name" : null,"commitId" : null }'
        def request = new JsonSlurper().parseText(requestTemplate)
        request.name = branch + "_bk_" + getDateTime()
        request.commitId = commitId
        def body = JsonOutput.toJson(request)
        def connection = ApiClient.post(urlString, body, headers)
        if (connection.responseCode == 201) {
            def status = new JsonSlurper().parseText(connection.getInputStream().getText())
            step.println("success: back up branch created: ${status}")
            return status
        } else {
            step.println("status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("unable to create back up.")
        }
    }

    def branchCleanUp(token, projectId, branch)
    {
        step.println("branch cleanup started.")
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches/" + branch + "/files"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "x-owner-id":props.ownerId, "Authorization": "Bearer " + token]
        def connection = ApiClient.get(urlString, headers)
        if (connection.responseCode == 200) {
            def projectArtifactList = new JsonSlurper().parseText(connection.getInputStream().getText())
            //step.println("success: List of files ${projectArtifactList}")
            def fileList = projectArtifactList.findAll { it -> (!it.path.contains("/")  && !it.path.contains("gitignore")  && !it.path.contains("exchange.json")  && it.type.equals("FILE")) }
            def folderList = projectArtifactList.findAll { it -> (!it.path.contains("/") && it.type.equals("FOLDER") && !it.path.contains("exchange_modules")) }
            def exchangeDependenciesList = projectArtifactList.findAll { it.path.contains("exchange_modules/") && it.type.equals("FOLDER") && (it.path.count("/") == 3)}
            //step.println("list of files to be deleted ${fileList} , list of folders to be deleted ${folderList} , list of exchange dependecies to be deleted ${exchangeDependenciesList}")
            if(fileList.size() > 0 || folderList.size() > 0 || exchangeDependenciesList.size() > 0 ){
            try {
                fileList.each { it -> deleteArtifact(token, projectId, branch, it.path) }
                folderList.each { it -> deleteArtifact(token, projectId, branch, it.path) }
                exchangeDependenciesList.each { it -> deleteExchangeDependencyArtifact(token, projectId, branch, it.path) }
            }catch(Exception e)
            {
                step.println("delete artifact stage failed."  + e.printStackTrace())
                throw new Exception("delete artifact stage failed.")
            }
            }
            step.println("branch cleanup completed.")

        } else {
            step.println("status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("unable to get project files list.")
        }
    }

    def deleteArtifact(token, projectId, branch, filePath)
    {
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches/" + branch + "/files/" + filePath
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "x-owner-id":props.ownerId, "Authorization": "Bearer " + token]
        def connection = ApiClient.delete(urlString, null, headers)
        if (connection.responseCode == 200) {
            //nothing.
        } else {
            step.println("status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("delete artifact stage failed.")
        }
    }

    def deleteExchangeDependencyArtifact(token, projectId, branch, filePath)
    {
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches/" + branch + "/exchange/dependencies"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "x-owner-id":props.ownerId, "Authorization": "Bearer " + token]
        def requestTemplate = '{"groupId" : null,"assetId" : null, "version" : null }'
        def request = new JsonSlurper().parseText(requestTemplate)
        def fileParts = filePath.split("/")
        request.groupId = fileParts[1]
        request.assetId = fileParts[2]
        request.version = fileParts[3]
        def body = JsonOutput.toJson(request)
        def connection = ApiClient.delete(urlString, body, headers)
        if (connection.responseCode == 200) {
            //ignore//
        } else {
            step.println("status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("delete dependency stage failed.")
        }
    }

    def uploadExchangeDependencyArtifacts(token, projectId, branch, apiDirPath){
        def apiBaseDir = new File(apiDirPath)
        for (File fileEntry : apiBaseDir.listFiles()) {
            if (fileEntry.isDirectory() && fileEntry.getName().equals("exchange_modules")) {
                step.println("adding exchange dependencies to project")
                try {
                    def createList = getExchangeDependencyFileListFilteredPath(apiBaseDir)
                    if(createList.size() > 0 ){
                        createList.each { it -> addExchangeDependency(token, projectId, branch, it)}
                     }
                 }catch(Exception e)
                {
                    step.println("adding exchange dependency stage failed."  + e.printStackTrace())
                    throw new Exception("adding exchange dependencies stage failed.")
                }
            }
        }
      }

    def addExchangeDependency(token, projectId, branch, filePath)
    {
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches/" + branch + "/exchange/dependencies"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "x-owner-id":props.ownerId, "Authorization": "Bearer " + token]
        def requestTemplate = '{"groupId" : null,"assetId" : null, "version" : null }'
        def request = new JsonSlurper().parseText(requestTemplate)
        def fileParts = filePath.replace(File.separator,"/").split("/")
        request.groupId = fileParts[1]
        request.assetId = fileParts[2]
        request.version = fileParts[3]
        def bodyTemp = JsonOutput.toJson(request)
        def body = "[${bodyTemp}]"
        step.println("adding dependency: ${body}")
        def connection = ApiClient.put(urlString, body, headers)
        if (connection.responseCode == 200) {
            def status = new JsonSlurper().parseText(connection.getInputStream().getText())
            return status
        } else {
            step.println("status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("add dependency stage failed.")
        }
    }

    def publishToExchange(token, projectId, branch, projectName, apiVersion,version )
    {
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches/" + branch + "/publish/exchange"
        def headers=["Content-Type": "application/json","Accept": "application/json","x-organization-id":props.organizationId, "x-owner-id":props.ownerId, "Authorization": "Bearer " + token]
        def requestTemplate = '{"main" : null,"apiVersion" : null, "version" : null, "assetId" : null, "groupId" : null, "classifier" : null  }'
        def request = new JsonSlurper().parseText(requestTemplate)
        request.main = projectName.toLowerCase().replace(" ","-").concat(".raml")
        request.apiVersion = apiVersion
        request.version = version
        request.assetId = projectName.toLowerCase().replace(" ","-")
        request.groupId = props.organizationId
        request.classifier = "raml"
        def body = JsonOutput.toJson(request)
        step.println("publishing asset to exchange: ${body}")
        def connection = ApiClient.post(urlString, body, headers)
        if (connection.responseCode == 200) {
            def status = new JsonSlurper().parseText(connection.getInputStream().getText())
            return status
        } else {
            step.println("status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("failed to publish asset into exchange.")
        }
    }

    def uploadArtifacts(token, projectId, branch, apiDirPath)
    {
        step.println("loading project artifacts")
        def urlString = "https://anypoint.mulesoft.com/designcenter/api-designer/projects/" + projectId + "/branches/" + branch + "/save/v2"
        def headers= ["x-organization-id": props.organizationId, "x-owner-id": props.ownerId, "Authorization": "Bearer " + token]
        def apiMultiPartDataClient = new ApiMultiPartDataClient()
        def connection = apiMultiPartDataClient.getConnection(urlString, headers)
        def apiBaseDir = new File(apiDirPath)
        addFilesIntoMultiPartClient(apiBaseDir, apiBaseDir, apiMultiPartDataClient)
        apiMultiPartDataClient.finish()

        //eachFileRecurse not works in jenkins.
        /*apiBaseDir.eachFileRecurse(FileType.FILES)  {
            def fileName = getModifiedFileName(apiBaseDir, it)
            apiMultiPartDataClient.addFilePart(fileName, it)
        }*/

        if (connection.responseCode == 200) {
            def savedFilesStatus = new JsonSlurper().parseText(connection.getInputStream().getText())
            step.println("success: saved project files are ${savedFilesStatus}")
            return savedFilesStatus
        } else {
            step.println("failed - status code: ${connection.responseCode}, message: ${connection.responseMessage}")
            throw new Exception("unable to save project files.")
        }
    }

    private def addFilesIntoMultiPartClient(File apiBaseDir, File apiBaseDirCopy, apiClient) {
        for (File fileEntry : apiBaseDir.listFiles()) {
            if(!fileEntry.getName().equals("exchange_modules")) {
                if (fileEntry.isDirectory()) {
                    addFilesIntoMultiPartClient(fileEntry, apiBaseDirCopy, apiClient)
                } else {
                    def fileName = getModifiedFileName(apiBaseDirCopy, fileEntry)
                    apiClient.addFilePart(fileName, fileEntry)
                }
            }
        }
    }

    private def getModifiedFileName(baseDir, filePath)
    {
        def rootLength = baseDir.getAbsolutePath().length()
        def absFileName = filePath.getAbsolutePath()
        def relFileName = absFileName.substring(rootLength + 1)
        return relFileName.replace(File.separator,"/")
    }

    private def getDateTime()
    {
        def formatter = new SimpleDateFormat("dd-MMM-yyyy")
        def date = new Date()
        String datePart = formatter.format(date)
        String timePart = date.getTime()
        return datePart + "_" + timePart
    }

   private def getExchangeDependencyFileListFilteredPath(apiBaseDir)
    {
        def filteredList = []
        getExchangeDependencyFileList(apiBaseDir,[])
                .each {
                    it -> L:{
                        def result = it.split("exchange_modules")
                        if(result.size() > 1)
                        {
                            filteredList.add(result[1])
                        }
                    }
                }
        return filteredList.findAll {it -> (it.count(File.separator) == 3)}
    }

    private def getExchangeDependencyFileList( apiBaseDir, filteredList) {
        for (File fileEntry : apiBaseDir.listFiles()) {
            if (fileEntry.isDirectory()) {
                filteredList.add(fileEntry.getPath())
                getExchangeDependencyFileList(fileEntry, filteredList)
            }
        }
        return filteredList
    }
}