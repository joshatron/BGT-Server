package io.joshatron.bgt.server.database.model;

import io.joshatron.bgt.engine.player.PlayerIndicator;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
public class PlayerAndIndicator {
    @Column
    private UUID player;
    @Column
    private PlayerIndicator color;
}
