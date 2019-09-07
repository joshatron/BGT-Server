package io.joshatron.bgt.server.database.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
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
}
