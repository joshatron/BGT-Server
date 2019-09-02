package io.joshatron.bgt.server.database.model;

public class UserMessage {
    private long id;
    private User sender;
    private User recipient;
    private String message;
    private long sent;
    private boolean opened;
}
