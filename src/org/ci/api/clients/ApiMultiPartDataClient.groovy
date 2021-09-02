package org.ci.api.clients

class ApiMultiPartDataClient{

    private OutputStreamWriter writer
    private final String boundary =  "*****"
    private final String crlf = "\r\n"
    private final String twoHyphens = "--"

    def getConnection( urlString, LinkedHashMap headers)
    {
        headers.put("Content-Type", "multipart/form-data;boundary=" + boundary)
        def connection = ApiClient.getUrlConnectionWithHeaders(urlString, headers, "POST")
        connection.doOutput = true
        writer =  new OutputStreamWriter(connection.getOutputStream())
        return connection
    }

    def addFilePart( fieldName,  File uploadFile)
    {
        def fileName = uploadFile.getName()
        writer.write(this.twoHyphens + this.boundary + this.crlf)
        writer.write("Content-Disposition: form-data; name=\"" + fieldName + "\";filename=\"" + fileName + "\"" + this.crlf)
        writer.write(this.crlf)
        writer.write(uploadFile.getText())
        writer.write(this.crlf)
    }

    def finish() {
        writer.write(this.crlf)
        writer.write(this.twoHyphens + this.boundary + this.twoHyphens + this.crlf)
        writer.flush()
        writer.close()
    }
}