package io.joshatron.tak.server.exceptions;

import org.springframework.http.HttpStatus;

public class GameServerException extends Exception {

    private ErrorCode code;

    public GameServerException(ErrorCode code) {
        super("The server encountered an error of type: " + code.name());
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return code.getStatus();
    }

    public String getJsonMessage() {
        return "{\"reason\": \"" + code.name() + "\"}";
    }
}
