package org.ci.api.clients

import groovy.util.logging.Slf4j

@Slf4j
class ApiClient {

    static def get(String urlString, LinkedHashMap headers) {
        def connection = getUrlConnectionWithHeaders(urlString, headers, "GET")
        connection.connect()
        return connection
    }
    static def post(urlString, body, headers) {
        def connection = getUrlConnectionWithHeaders(urlString, headers, "POST")
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
        def connection = getUrlConnectionWithHeaders(urlString, headers, "PUT" )
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
        def connection = getUrlConnectionWithHeaders(urlString, headers, "DELETE")
        connection.connect()
        return connection
    }
    private static def getUrlConnectionWithHeaders(urlString, headers, method){
        log.info("calling URL: ${urlString}, headers: ${headers}, method: ${method}")
        def url = new URL(urlString)
        def connection = (HttpURLConnection)url.openConnection()
        headers.each {it -> connection.setRequestProperty(it.key, it.value) }
        connection.setRequestMethod(method)
        return connection
    }
}