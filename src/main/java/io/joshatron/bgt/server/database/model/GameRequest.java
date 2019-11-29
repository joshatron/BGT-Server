package io.joshatron.bgt.server.database.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.util.*;

@Entity
@Data
@AllArgsConstructor
public class GameRequest {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    @ManyToOne
    private User requester;
    @ElementCollection
    @CollectionTable(name = "PlayerAndIndicator",
            joinColumns = {@JoinColumn(name = "requestId", referencedColumnName = "id")})
    @MapKeyColumn(name = "opponent")
    @Column
    private List<PlayerAndIndicator> players;
    @ElementCollection
    @CollectionTable(name = "GameParameters",
            joinColumns = {@JoinColumn(name = "requestId", referencedColumnName = "id")})
    @MapKeyColumn(name = "paramName")
    @Column
    private Map<String,String> parameters;

    public GameRequest() {
        id = null;
        requester = null;
        players = new ArrayList<>();
        parameters = new HashMap<>();
    }
}
