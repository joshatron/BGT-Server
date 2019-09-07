package io.joshatron.bgt.server.response;

import io.joshatron.bgt.server.database.model.UserMessage;
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

    public Message(UserMessage userMessage) {
        //TODO: implement
    }
}
