package io.joshatron.bgt.server.logic.utils;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class Response {

    private int status;
    private String contents;

    public Response(HttpResponse response) throws IOException {
        this.status = response.getStatusLine().getStatusCode();
        if(response.getEntity() != null) {
            this.contents = EntityUtils.toString(response.getEntity());
        }
        else {
            this.contents = "";
        }
    }

    public Response(int status) {
        this.status = status;
        this.contents = "";
    }

    public Response(int status, String contents) {
        this.status = status;
        this.contents = contents;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
