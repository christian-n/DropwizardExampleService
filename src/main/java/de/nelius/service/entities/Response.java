package de.nelius.service.entities;

/**
 * Response entity with an http status.
 *
 * @author Christian Nelius
 */
public class Response {

    private int httpStatus;

    public Response(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }
}
