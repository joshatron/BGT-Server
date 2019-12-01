package io.joshatron.bgt.server.database.model;

import io.joshatron.bgt.engine.player.PlayerIndicator;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class PlayerAndIndicator {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    @ManyToOne
    private GameRequest gameRequest;
    @Column(nullable = false)
    private UUID player;
    @Column(nullable = false)
    private PlayerIndicator indicator;

    public PlayerAndIndicator(GameRequest request, UUID player, PlayerIndicator indicator) {
        this.gameRequest = request;
        this.player = player;
        this.indicator = indicator;
    }
}
