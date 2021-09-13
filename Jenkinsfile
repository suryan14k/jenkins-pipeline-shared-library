@Library('myLibMaven') _

pipeline {
    agent any

    stages {
        stage('code_checkout') {
            steps {
                dir('C:\\ci') {
                    startCodeCheckOut("https://github.com/suryan14k/jenkins-pipeline-shared-library.git", "master")
                }
            }
        }
        stage('design_centre_api_upload') {
            steps {
                    script{
                        
                        def username = "suryan14k"
                                            def password = "Letmein_01"
                        def organizationId = "c8a97a61-f4c4-4e40-a2b6-ba13718b421c"
                        def ownerId = "2cc24e16-4c9c-4ce5-ab0a-346f1d3ed80c"
                        def projectName = "API POC"
                        def branch = "master"
                        def apiDirPath = "C:\\ci\\api"
                        
                        def props = [
                                     'username': username,
                                     'password': password ,
                                     'organizationId': organizationId, 
                                     'ownerId': ownerId
                                    ]
                        
                        startDesignCentreAPIUpload(this, props,projectName, apiDirPath)
                     }
             }
        }
        stage('publish_asset_to_exchange') {
                    steps {
                            script{

                                def username = "suryan14k"
                                            def password = "Letmein_01"
                                def organizationId = "c8a97a61-f4c4-4e40-a2b6-ba13718b421c"
                                def ownerId = "2cc24e16-4c9c-4ce5-ab0a-346f1d3ed80c"
                                def projectName = "API POC"
                                def branch = "master"
                                def props = [
                                             'username': username,
                                             'password': password ,
                                             'organizationId': organizationId,
                                             'ownerId': ownerId
                                            ]
                                def apiVersion = input message: 'Please enter the api version. eg: v1, v2',
                                                       parameters: [string(defaultValue: '',
                                                       description: '',
                                                       name: 'apiVersion')]
                                def assetVersion = input message: 'Please enter the asset version. eg: 1.0.0, 1.1.1',
                                                       parameters: [string(defaultValue: '',
                                                       description: '',
                                                       name: 'apiVersion')]
                                publishAssetToExchange(this, props,projectName, apiVersion, assetVersion)
                             }
                     }
        }
        stage('api_manager_deployment') {
            steps {
                                        script{

                                            def username = "suryan14k"
                                            def password = "Letmein_01"
                                            def organizationId = "c8a97a61-f4c4-4e40-a2b6-ba13718b421c"
                                            def environmentId = "2c6abb1e-23e9-4d25-8f8f-0d6b09c03be2"
                                            def ownerId = "2cc24e16-4c9c-4ce5-ab0a-346f1d3ed80c"
                                            def projectName = "API POC"
                                            def branch = "master"
                                            def props = [
                                                         'username': username,
                                                         'password': password ,
                                                         'organizationId': organizationId,
                                                         'environmentId': environmentId,
                                                         'ownerId': ownerId
                                                        ]
                                            def assetVersion = input message: 'Please enter the asset version. eg: 1.0.0, 1.1.1',
                                                                   parameters: [string(defaultValue: '',
                                                                   description: '',
                                                                   name: 'apiVersion')]
                                            createApiOnApiManager(this, props, projectName, assetVersion)
                              }
            }
        }
        stage('runtime_manager_deployment') {
             steps {echo "done"}
        }
    }
}

