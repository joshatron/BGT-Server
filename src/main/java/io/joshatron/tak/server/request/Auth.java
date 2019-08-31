package io.joshatron.tak.server.request;

import io.joshatron.tak.server.exceptions.ErrorCode;
import io.joshatron.tak.server.exceptions.GameServerException;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Base64;

@Data
@AllArgsConstructor
public class Auth {

    private String username;
    private String password;

    //Constructor for basic auth
    public Auth(String basicAuth) throws GameServerException {
        if(basicAuth == null) {
            throw new GameServerException(ErrorCode.EMPTY_AUTH);
        }
        if(!basicAuth.startsWith("Basic ")) {
            throw new GameServerException(ErrorCode.INVALID_FORMATTING);
        }
        //Decode from base 64
        String decoded = new String(Base64.getDecoder().decode(basicAuth.replace("Basic ", "")));
        //Makes sure there is only one :
        if(decoded.length() - decoded.replace(":", "").length() >= 1) {
            String[] auth = decoded.split(":");
            if(auth.length < 2) {
                throw new GameServerException(ErrorCode.INVALID_AUTH);
            }
            username = auth[0];
            password = decoded.substring(username.length() + 1);

            if(username.length() == 0 || password.length() == 0) {
                throw new GameServerException(ErrorCode.EMPTY_FIELD);
            }
        }
    }
}
