package io.joshatron.tak.server.utils;

import io.joshatron.tak.ai.player.AI;
import io.joshatron.tak.ai.player.AIFactory;
import io.joshatron.tak.engine.exception.TakEngineException;
import io.joshatron.tak.engine.game.GameResult;
import io.joshatron.tak.engine.game.GameState;
import io.joshatron.tak.engine.player.TakPlayer;
import io.joshatron.tak.engine.turn.Turn;
import io.joshatron.tak.server.database.GameDAO;
import io.joshatron.tak.server.exceptions.ErrorCode;
import io.joshatron.tak.server.exceptions.GameServerException;
import org.springframework.scheduling.annotation.Async;

public class AiUtils {

    private static final String[] names = new String[]{"AI"};

    public AiUtils() {
        throw new IllegalStateException("This is a utility class");
    }

    public static boolean isAi(String name) {
        if(name == null || name.isEmpty()) {
            return false;
        }

        for(String ai : names) {
            if(name.equalsIgnoreCase(ai)) {
                return true;
            }
        }

        return false;
    }

    @Async
    public static void playTurn(GameState state, String gameId, GameDAO gameDAO) throws GameServerException {
        try {
            TakPlayer player = AIFactory.createPlayer(AI.DEFENSIVE_MINIMAX, state.getBoardSize(), state.getCurrentPlayer(), state.getFirstPlayer());
            Turn turn = player.getTurn(state);
            state.executeTurn(turn);

            gameDAO.addTurn(gameId, turn.toString());
            GameResult result = state.checkForWinner();
            if(result.isFinished()) {
                gameDAO.finishGame(gameId, result.getWinner());
            }
        } catch(TakEngineException e) {
            throw new GameServerException(ErrorCode.GAME_ENGINE_ERROR);
        }
    }
}
