package io.joshatron.bgt.server.validation;

import io.joshatron.bgt.server.database.GameDAO;
import io.joshatron.bgt.server.database.model.GameRequest;
import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.database.model.PlayerAndIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GameValidator {

    @Autowired
    private GameDAO gameDAO;

    public GameRequest verifyGameRequest(User user, String request) {
        UUID id = UUID.fromString(request);
        try {
            GameRequest gameRequest = gameDAO.getGameRequestInfo(id);
            for(PlayerAndIndicator player : gameRequest.getPlayers()) {
                if(player.getPlayer().equals(user.getId())) {
                    return gameRequest;
                }
            }
            throw new GameServerException(ErrorCode.REQUEST_NOT_FOUND);
        } catch(GameServerException e) {
            throw new GameServerException(ErrorCode.REQUEST_NOT_FOUND);
        }
    }
}
