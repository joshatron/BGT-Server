package io.joshatron.bgt.server.database.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMessage {
    private UUID id;
    private User sender;
    private User recipient;
    private String message;
    private long sent;
    private boolean opened;
}
