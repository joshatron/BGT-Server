package io.joshatron.bgt.server.response;

import io.joshatron.bgt.engine.player.PlayerIndicator;
import io.joshatron.bgt.server.database.model.GameRequest;
import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.request.PlayerAndIndicator;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class RequestInfo {
    private UUID id;
    private UUID requester;
    private List<PlayerAndIndicator> players;
    private Map<String,String> parameters;

    public RequestInfo(GameRequest request) {
        this.id = request.getId();
        if(request.getRequester() != null) {
            this.requester = request.getRequester().getId();
        }
        this.players = new ArrayList<>();
        for(Map.Entry<User, PlayerIndicator> userAndColor : request.getPlayers().entrySet()) {
            this.players.add(new PlayerAndIndicator(userAndColor.getKey().getId(), userAndColor.getValue()));
        }
        this.parameters = request.getParameters();
    }
}
