package io.joshatron.bgt.server.utils;

import io.joshatron.bgt.server.database.model.GameRequest;
import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.request.*;
import io.joshatron.bgt.server.response.*;
import io.joshatron.bgt.server.validation.*;
import io.joshatron.tak.engine.exception.TakEngineException;
import io.joshatron.tak.engine.game.GameResult;
import io.joshatron.tak.engine.game.GameState;
import io.joshatron.tak.engine.game.Player;
import io.joshatron.tak.engine.turn.Turn;
import io.joshatron.tak.engine.turn.TurnUtils;
import io.joshatron.bgt.server.database.AccountDAO;
import io.joshatron.bgt.server.database.GameDAO;
import io.joshatron.bgt.server.database.SocialDAO;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class GameUtils {

    @Autowired
    private GameDAO gameDAO;
    @Autowired
    private SocialDAO socialDAO;
    @Autowired
    private AccountDAO accountDAO;
    @Autowired
    private AccountValidator accountValidator;
    @Autowired
    private SocialValidator socialValidator;
    @Autowired
    private GameValidator gameValidator;
    @Autowired
    private CustomBGTValidator bgtValidator;

    @Value("${game.forfeit-days:0}")
    private Integer daysToForfeit;

    public void requestGame(String authString, RequestGame gameRequest) {
        User user = accountValidator.verifyCredentials(authString);
        DTOValidator.validateGameRequest(gameRequest);
        bgtValidator.validateGameRequest(gameRequest);
        List<User> others = accountValidator.verifyUserIds(Arrays.asList(gameRequest.getOpponents()));
        others.forEach(o -> socialValidator.validateFriends(user, o.getId()));

        gameDAO.createGameRequest(user, others, gameRequest);
    }

    public RequestInfo getRequest(String authString, String requestId) {
        User user = accountValidator.verifyCredentials(authString);
        GameRequest request = gameValidator.verifyGameRequest(user, requestId);

        return new RequestInfo(request);
    }

    public void deleteRequest(String authString, String requestId) {
        User user = accountValidator.verifyCredentials(authString);
        GameRequest request = gameValidator.verifyGameRequest(user, requestId);

        gameDAO.deleteGameRequest(request.getId());
    }

    public void respondToGame(String authString, String requestId, GameRequestAnswer answer) {
        User user = accountValidator.verifyCredentials(authString);
        GameRequest request = gameValidator.verifyGameRequest(user, requestId);
        DTOValidator.validateGameRequestAnswer(answer);
        DTOValidator.validateUserNotRespondedYet(user, request);

        if(answer.getAnswer() == Answer.ACCEPT) {
            //gameDAO.startGame(id, user.getId().toString(), info.getSize(), info.getRequesterColor(), info.getFirst());
        }
        //gameDAO.deleteGameRequest(id, user.getId().toString());
    }

    public RequestInfo[] checkIncomingRequests(String authString) {
        User user = accountValidator.verifyCredentials(authString);

        List<GameRequest> requests = gameDAO.getIncomingGameRequests(user.getId());

        return requests.stream()
                .map(RequestInfo::new)
                .toArray(RequestInfo[]::new);
    }

    public RequestInfo[] checkOutgoingRequests(String authString) {
        User user = accountValidator.verifyCredentials(authString);

        List<GameRequest> requests = gameDAO.getOutgoingGameRequests(user);

        return requests.stream()
                .map(RequestInfo::new)
                .toArray(RequestInfo[]::new);
    }

    public void requestRandomGame(Auth auth, RandomGameRequest request) {
        DTOValidator.validateAuth(auth);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        if(gameDAO.randomGameRequestExists(user.getId().toString())) {
            throw new GameServerException(ErrorCode.GAME_REQUEST_EXISTS);
        }

        gameDAO.createRandomGameRequest(user.getId());
        resolveRandomGameRequests();
    }

    private void resolveRandomGameRequests() {
        RandomRequestInfo[] requests = gameDAO.getRandomGameRequests();

        for(int i = 0; i < requests.length; i++) {
            if(requests[i] != null) {
                for(int j = i + 1; j < requests.length; j++) {
//                    if(requests[j] != null && requests[i].getSize() == requests[j].getSize() &&
//                       !gameDAO.playingGame(requests[i].getRequester(), requests[j].getRequester()) &&
//                       !socialDAO.isBlocked(UUID.fromString(requests[i].getRequester()), UUID.fromString(requests[j].getRequester())) &&
//                       !socialDAO.isBlocked(UUID.fromString(requests[j].getRequester()), UUID.fromString(requests[i].getRequester()))) {
//                        gameDAO.startGame(requests[i].getRequester(), requests[j].getRequester(), requests[i].getSize(), Player.WHITE, Player.WHITE);
//                        gameDAO.deleteRandomGameRequest(requests[i].getRequester());
//                        gameDAO.deleteRandomGameRequest(requests[j].getRequester());
//                        requests[j] = null;
//                        break;
//                    }
                }
            }
        }
    }

    public void deleteRandomRequest(Auth auth) {
        DTOValidator.validateAuth(auth);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        if(!gameDAO.randomGameRequestExists(user.getId().toString())) {
            throw new GameServerException(ErrorCode.REQUEST_NOT_FOUND);
        }

        //gameDAO.deleteRandomGameRequest(user.getId().toString());
    }

    public boolean randomRequestExists(Auth auth) {
        DTOValidator.validateAuth(auth);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        if(!gameDAO.randomGameRequestExists(user.getId().toString())) {
            throw new GameServerException(ErrorCode.REQUEST_NOT_FOUND);
        }

        return false;
    }

    public GameInfo getGameInfo(Auth auth, String gameId) {
        DTOValidator.validateAuth(auth);
        DTOValidator.validateId(gameId);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        checkForForfeits(user.getId().toString());
        if(!gameDAO.gameExists(gameId)) {
            throw new GameServerException(ErrorCode.GAME_NOT_FOUND);
        }
        if(!gameDAO.userAuthorizedForGame(user.getId().toString(), gameId)) {
            throw new GameServerException(ErrorCode.GAME_NOT_FOUND);
        }

        GameInfo info = gameDAO.getGameInfo(gameId);
        info.setMessages(socialDAO.listMessages(UUID.fromString(gameId), null, null, null, null, null, RecipientType.GAME).parallelStream().map(MessageInfo::new).toArray(MessageInfo[]::new));
        for(MessageInfo message : info.getMessages()) {
            if(!message.getSender().equalsIgnoreCase(user.getId().toString())) {
                socialDAO.markMessageRead(UUID.fromString(message.getId()));
                message.setOpened(true);
            }
        }

        if(false) {
            return info;
        }
        else {
            try {
                GameState state = new GameState(info.getFirst(), info.getSize());
                for(String turn : info.getTurns()) {
                    state.executeTurn(TurnUtils.turnFromString(turn));
                }

                info.setFullState(state);

                return info;
            } catch(TakEngineException e) {
                throw new GameServerException(ErrorCode.GAME_ENGINE_ERROR);
            }
        }
    }

    public GameInfo[] findGames(Auth auth, String opponents, Long startTime, Long endTime, String complete, String pending) {
        DTOValidator.validateAuth(auth);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        checkForForfeits(user.getId().toString());
        Date now = new Date();
        Date start = null;
        if(startTime != null) {
            start = new Date(startTime.longValue());
            if(now.before(start)) {
                throw new GameServerException(ErrorCode.INVALID_DATE);
            }
        }
        Date end = null;
        if(endTime != null) {
            end = new Date(endTime.longValue());
            if(now.before(end)) {
                throw new GameServerException(ErrorCode.INVALID_DATE);
            }
        }
        String[] users = null;
        if(opponents != null && opponents.length() > 0) {
            users = opponents.split(",");
            for (String u : users) {
                if(!AiUtils.isAi(u)) {
                    UUID uuid = DTOValidator.validateId(u);
                    if (!accountDAO.userExists(uuid)) {
                        throw new GameServerException(ErrorCode.USER_NOT_FOUND);
                    }
                }
            }
        }
        if(start != null && end != null && start.after(end)) {
            throw new GameServerException(ErrorCode.INVALID_DATE);
        }
        Complete cpt = DTOValidator.validateComplete(complete);
        Pending pnd = DTOValidator.validatePending(pending);

        return gameDAO.listGames(user.getId().toString(), users, start, end, cpt, pnd);
    }

    public void sendGameMessage(Auth auth, String gameId, Message message) {
        DTOValidator.validateAuth(auth);
        DTOValidator.validateId(gameId);
        //Validator.validateText(message);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        checkForForfeits(user.getId().toString());
        if(!gameDAO.gameExists(gameId)) {
            throw new GameServerException(ErrorCode.GAME_NOT_FOUND);
        }
        if(!gameDAO.userAuthorizedForGame(user.getId().toString(), gameId)) {
            throw new GameServerException(ErrorCode.GAME_NOT_FOUND);
        }
        if(gameDAO.getGameInfo(gameId).isDone()) {
            throw new GameServerException(ErrorCode.GAME_IS_COMPLETE);
        }

        socialDAO.sendMessage(user.getId(), UUID.fromString(gameId), message.getMessage(), RecipientType.GAME);
    }

    public String[] getPossibleTurns(Auth auth, String gameId) {
        DTOValidator.validateAuth(auth);
        DTOValidator.validateId(gameId);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        checkForForfeits(user.getId().toString());
        if(!gameDAO.gameExists(gameId)) {
            throw new GameServerException(ErrorCode.GAME_NOT_FOUND);
        }
        if(!gameDAO.userAuthorizedForGame(user.getId().toString(), gameId)) {
            throw new GameServerException(ErrorCode.GAME_NOT_FOUND);
        }
        if(gameDAO.getGameInfo(gameId).isDone()) {
            return new String[0];
        }
        if(!gameDAO.isYourTurn(user.getId().toString(), gameId)) {
            throw new GameServerException(ErrorCode.NOT_YOUR_TURN);
        }

        GameState state = getStateFromId(gameId);

        List<Turn> possible = state.getPossibleTurns();
        String[] toReturn = new String[possible.size()];
        for(int i = 0; i < toReturn.length; i++) {
            toReturn[i] = possible.get(i).toString();
        }

        return toReturn;
    }

    public void playTurn(Auth auth, String gameId, Move move) {
        DTOValidator.validateAuth(auth);
        DTOValidator.validateId(gameId);
        //Validator.validateText(move);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        checkForForfeits(user.getId().toString());
        if(!gameDAO.gameExists(gameId)) {
            throw new GameServerException(ErrorCode.GAME_NOT_FOUND);
        }
        if(!gameDAO.userAuthorizedForGame(user.getId().toString(), gameId)) {
            throw new GameServerException(ErrorCode.GAME_NOT_FOUND);
        }
        if(!gameDAO.isYourTurn(user.getId().toString(), gameId)) {
            throw new GameServerException(ErrorCode.NOT_YOUR_TURN);
        }

        Turn proposed;
        try {
            proposed = TurnUtils.turnFromString(move.getMove());
        } catch(TakEngineException e) {
            throw new GameServerException(ErrorCode.INVALID_FORMATTING);
        }
        if(proposed == null) {
            throw new GameServerException(ErrorCode.INVALID_FORMATTING);
        }

        GameState state = getStateFromId(gameId);
        try {
            state.executeTurn(proposed);
        }
        catch(TakEngineException e) {
            throw new GameServerException(ErrorCode.ILLEGAL_MOVE);
        }

        //gameDAO.updateState(gameId, turn.getText());

        GameInfo info = gameDAO.getGameInfo(gameId);
        GameResult result = state.checkForWinner();
        if(result.isFinished()) {
            //gameDAO.finishGame(gameId, result.getWinner());
            if(result.getWinner() == Player.WHITE) {
                updateRatings(info.getWhite(), info.getBlack());
            }
            else if(result.getWinner() == Player.BLACK) {
                updateRatings(info.getBlack(), info.getWhite());
            }
        }
        else if(AiUtils.isAi(info.getBlack()) || AiUtils.isAi(info.getWhite())) {
            AiUtils.playTurn(state, info.getGameId(), gameDAO);
        }
    }

    private void updateRatings(String winner, String loser) {
        int k = 20;

        UUID winnerId = DTOValidator.validateId(winner);
        UUID loserId = DTOValidator.validateId(loser);

        User w = accountDAO.getUserFromId(winnerId);
        User l = accountDAO.getUserFromId(loserId);

        //Implement elo diff
        double winnerExpected = 1. / (1 + Math.pow(10, (l.getRating() - w.getRating()) / 400.));
        double loserExpected = 1. / (1 + Math.pow(10, (w.getRating() - l.getRating()) / 400.));

        accountDAO.updateRating(winnerId, (int)Math.round(w.getRating() + k * (1 - winnerExpected)));
        accountDAO.updateRating(loserId, (int)Math.round(l.getRating() + k * (0 - loserExpected)));
    }

    public GameNotifications getNotifications(Auth auth) {
        DTOValidator.validateAuth(auth);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        checkForForfeits(user.getId().toString());

        return gameDAO.getGameNotifications(user.getId().toString());
    }

    private GameState getStateFromId(String gameId) {
        try {
            GameInfo gameInfo = gameDAO.getGameInfo(gameId);

            Player player = gameInfo.getFirst();
            GameState state = new GameState(player, gameInfo.getSize());
            for(String turn : gameInfo.getTurns()) {
                Turn toPlay = TurnUtils.turnFromString(turn);
                state.executeTurn(toPlay);
            }

            return state;
        }
        catch(TakEngineException e) {
            throw new GameServerException(ErrorCode.GAME_ENGINE_ERROR);
        }
    }

    private void checkForForfeits(String userId) {
        if(daysToForfeit > 0) {
            GameInfo[] openGames = gameDAO.listGames(userId, null, null, null, Complete.INCOMPLETE, null);

            for(GameInfo game : openGames) {
                //check if it has been enough days
                if(Instant.now().toEpochMilli() - game.getLast() > daysToForfeit * (1000 * 60 * 60 * 24)) {
                    //gameDAO.finishGame(game.getGameId(), game.getCurrent().opposite());
                }
            }
        }
    }
}
