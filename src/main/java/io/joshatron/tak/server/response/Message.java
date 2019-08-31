package io.joshatron.tak.server.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class Message {
    private String sender;
    private String recipient;
    private long timestamp;
    private String message;
    private String id;
    private boolean opened;
}
