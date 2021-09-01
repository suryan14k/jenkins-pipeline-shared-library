package org.ci.api.clients

//@Grab('org.codehaus.groovy:groovy-json:3.0.8')
class ApiClient {

    static def get(String urlString, LinkedHashMap headers) {
        def connection = getUrlConnectionWithHeaders(urlString, headers)
        connection.setRequestMethod("GET")
        connection.connect()
        return connection
    }
    static def post(urlString, body, headers) {
        def connection = getUrlConnectionWithHeaders(urlString, headers )
        connection.setRequestMethod("POST")
        connection.doOutput = true
        if(body != null) {
            def writer = new OutputStreamWriter(connection.getOutputStream())
            writer.write(body)
            writer.flush()
            writer.close()
        }
        connection.connect()
        return connection
    }
    static def put(urlString, body, headers) {
        def connection = getUrlConnectionWithHeaders(urlString, headers )
        connection.setRequestMethod("PUT")
        connection.doOutput = true
        if(body != null) {
            def writer = new OutputStreamWriter(connection.getOutputStream())
            writer.write(body)
            writer.flush()
            writer.close()
        }
        connection.connect()
        return connection
    }
    static def delete(urlString, headers) {
        def connection = getUrlConnectionWithHeaders(urlString, headers)
        connection.setRequestMethod("DELETE")
        connection.connect()
        return connection
    }
    static def getUrlConnectionWithHeaders(urlString, headers){
        def url = new URL(urlString)
        def connection = (HttpURLConnection)url.openConnection()
        headers.each {it -> connection.setRequestProperty(it.key, it.value) }
        return connection
    }
}