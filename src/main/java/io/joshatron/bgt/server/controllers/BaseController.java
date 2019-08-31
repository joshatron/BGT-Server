package io.joshatron.bgt.server.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BaseController {

    @Value("${project.version}")
    private String version;

    @GetMapping(value = "/health")
    public ResponseEntity healthCheck() {
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/version")
    public ResponseEntity versionCheck() {
        return new ResponseEntity<>(version, HttpStatus.OK);
    }
}
