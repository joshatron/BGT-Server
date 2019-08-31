package io.joshatron.tak.server.controllers;

import io.joshatron.tak.server.exceptions.*;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ControllerUtils {

    private ControllerUtils() {
        throw new IllegalStateException("This is a utility class");
    }

    public static ResponseEntity handleExceptions(Exception e, Logger logger) {

        if(e instanceof GameServerException) {
            GameServerException serverError = (GameServerException) e;
            logger.warn("Exception occurred: {}", serverError.getCode().name());
            return new ResponseEntity<>(serverError.getJsonMessage(), serverError.getHttpStatus());
        }
        else {
            logger.error("Unknown exception occurred, returning server error", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
