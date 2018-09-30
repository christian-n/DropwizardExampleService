package de.nelius.service.resource;

import org.eclipse.jetty.http.HttpStatus;

public class Status {

    private int httpStatus;

    public Status(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }
}
