package io.joshatron.bgt.server.database.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    @ManyToOne
    private User sender;
    @ManyToOne
    private User recipient;
    @Column(nullable = false)
    private String body;
    @Column(nullable = false)
    private Timestamp sent;
    @Column(nullable = false)
    private boolean opened;

    public Message() {
        this.id = null;
        this.sender = null;
        this.recipient = null;
        this.body = "";
        this.sent = new Timestamp(new Date().getTime());
        this.opened = false;
    }
}
