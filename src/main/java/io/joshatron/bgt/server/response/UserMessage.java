package io.joshatron.bgt.server.response;

import io.joshatron.bgt.server.database.model.Message;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserMessage {
    private String sender;
    private String recipient;
    private long timestamp;
    private String body;
    private String id;
    private boolean opened;

    public UserMessage(Message message) {
        this.sender = message.getSender().getId().toString();
        this.recipient = message.getRecipient().getId().toString();
        this.timestamp = message.getSent().getTime();
        this.body = message.getBody();
        this.id = message.getId().toString();
        this.opened = message.isOpened();
    }
}
