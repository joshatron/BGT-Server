package io.joshatron.bgt.server.response;

import io.joshatron.bgt.server.database.model.GameRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class RequestInfo {
    private UUID id;
    private UUID requester;
    private List<OpponentWithColor> players;
    private Map<String,String> parameters;

    public RequestInfo(GameRequest request) {
        this.id = request.getId();
        if(request.getRequester() != null) {
            this.requester = request.getRequester().getId();
        }
        this.players = request.getPlayers().stream()
                .map(p -> new OpponentWithColor(p.getPlayer(), p.getIndicator()))
                .collect(Collectors.toList());
        this.parameters = request.getParameters();
    }
}
