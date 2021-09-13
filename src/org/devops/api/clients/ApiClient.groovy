package org.devops.api.clients

class ApiClient {

    static def get(String urlString, LinkedHashMap headers) {
        def connection = getUrlConnectionWithHeaders(urlString, headers, "GET")
        connection.connect()
        return connection
    }

    static def post(urlString, body, headers) {
        def connection = getUrlConnectionWithHeaders(urlString, headers, "POST")
        writeData(connection, body)
        connection.connect()
        return connection
    }
    static def put(urlString, body, headers) {
        def connection = getUrlConnectionWithHeaders(urlString, headers, "PUT" )
        writeData(connection, body)
        connection.connect()
        return connection
    }
    static def patch(urlString, body, headers) {
        def connection = getUrlConnectionWithHeaders(urlString, headers, "PATCH" )
        writeData(connection, body)
        connection.connect()
        return connection
    }
    static def delete(urlString, body, headers) {
        def connection = getUrlConnectionWithHeaders(urlString, headers, "DELETE")
        writeData(connection, body)
        connection.connect()
        return connection
    }

    static def getUrlConnectionWithHeaders(urlString, headers, method){
        def url = new URL(urlString)
        def connection = (HttpURLConnection)url.openConnection()
        headers.each {it -> connection.setRequestProperty(it.key, it.value) }
        connection.setRequestMethod(method)
        return connection
    }

    static def writeData(connection, body){
        connection.doOutput = true
        if(body != null) {
            def writer = new OutputStreamWriter(connection.getOutputStream())
            writer.write(body)
            writer.flush()
            writer.close()
        }
    }
}