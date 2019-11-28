package io.joshatron.bgt.server.database.model;

import io.joshatron.bgt.engine.player.PlayerIndicator;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    @CollectionTable(name = "player_indicator_map",
            joinColumns = {@JoinColumn(name = "request_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "opponent")
    @Column
    private Map<User, PlayerIndicator> players;
    @ElementCollection
    @CollectionTable(name = "game_parameters",
            joinColumns = {@JoinColumn(name = "request_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "param_name")
    @Column
    private Map<String,String> parameters;

    public GameRequest() {
        id = null;
        requester = null;
        players = new HashMap<>();
        parameters = new HashMap<>();
    }
}
