package io.joshatron.bgt.server.logic.utils;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;

public class HttpUtils {

    public static String baseUrl = "https://localhost:8080";

    public static HttpClient createHttpClient() {
        try {
            SSLContextBuilder builder = SSLContexts.custom();
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                    return true;
                }
            });
            SSLContext sslContext = builder.build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslContext, new X509HostnameVerifier() {
                @Override
                public void verify(String host, SSLSocket ssl)
                        throws IOException {
                }

                @Override
                public void verify(String host, X509Certificate cert)
                        throws SSLException {
                }

                @Override
                public void verify(String host, String[] cns,
                                   String[] subjectAlts) throws SSLException {
                }

                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });

            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                    .<ConnectionSocketFactory>create().register("https", sslsf)
                    .build();

            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
                    socketFactoryRegistry);

            return HttpClients.custom().setConnectionManager(cm).build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getBasicAuthString(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    public static Response createUser(String username, String password, HttpClient client) throws IOException {
        String payload = "{";
        if(username != null) {
            payload += "\"username\": \"" + username + "\"";
        }
        if(password != null) {
            if(username != null) {
                payload += ",";
            }
            payload += "\"password\": \"" + password + "\"";
        }
        payload += "}";

        HttpPost request = new HttpPost(baseUrl + "/account/register");
        StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response changeUsername(String username, String password, String newPassword, HttpClient client) throws IOException {
        String payload = "{";
        if (newPassword != null) {
            payload += "\"text\": \"" + newPassword + "\"";
        }
        payload += "}";

        HttpPost request = new HttpPost(baseUrl + "/account/change-name");
        StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
        if (username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }
        request.setEntity(entity);

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response changePassword(String username, String password, String newPassword, HttpClient client) throws IOException {
        String payload = "{";
        if(newPassword != null) {
            payload += "\"text\": \"" + newPassword + "\"";
        }
        payload += "}";

        HttpPost request = new HttpPost(baseUrl + "/account/change-pass");
        StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }
        request.setEntity(entity);

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }


    public static Response authenticate(String username, String password, HttpClient client) throws IOException {
        HttpGet request = new HttpGet(baseUrl + "/account/authenticate");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response searchUser(String username, String userId, HttpClient client) throws IOException {
        StringBuilder params = new StringBuilder("?");
        if(username != null) {
            params.append("user=");
            params.append(username);
            if(userId != null) {
                params.append("&");
            }
        }
        if(userId != null) {
            params.append("id=");
            params.append(userId);
        }
        HttpGet request = new HttpGet(baseUrl + "/account/user" + params.toString());

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response requestFriend(String username, String password, String other, HttpClient client) throws IOException {
        HttpPost request = new HttpPost(baseUrl + "/social/request/create/" + other);
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response cancelFriendRequest(String username, String password, String other, HttpClient client) throws IOException {
        HttpDelete request = new HttpDelete(baseUrl + "/social/request/cancel/" + other);
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response respondToRequest(String username, String password, String other, String answer, HttpClient client) throws IOException {
        String payload = "{\"text\": \"" + answer + "\"}";

        HttpPost request = new HttpPost(baseUrl + "/social/request/respond/" + other);
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }
        StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response checkIncomingFriendRequests(String username, String password, HttpClient client) throws IOException {
        HttpGet request = new HttpGet(baseUrl + "/social/request/incoming");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response checkOutgoingFriendRequests(String username, String password, HttpClient client) throws IOException {
        HttpGet request = new HttpGet(baseUrl + "/social/request/outgoing");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response unfriendUser(String username, String password, String other, HttpClient client) throws IOException {
        HttpDelete request = new HttpDelete(baseUrl + "/social/user/" + other + "/unfriend");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response blockUser(String username, String password, String other, HttpClient client) throws IOException {
        HttpPost request = new HttpPost(baseUrl + "/social/user/" + other + "/block");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response unblockUser(String username, String password, String other, HttpClient client) throws IOException {
        HttpDelete request = new HttpDelete(baseUrl + "/social/user/" + other + "/unblock");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response checkIfBlocked(String username, String password, String other, HttpClient client) throws IOException {
        HttpGet request = new HttpGet(baseUrl + "/social/user/" + other + "/blocked");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response getFriends(String username, String password, HttpClient client) throws IOException {
        HttpGet request = new HttpGet(baseUrl + "/social/user/friends");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response getBlocked(String username, String password, HttpClient client) throws IOException {
        HttpGet request = new HttpGet(baseUrl + "/social/user/blocking");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response sendMessage(String username, String password, String recipient, String message, HttpClient client) throws IOException {
        String payload = "{\"text\": \"" + message + "\"}";

        HttpPost request = new HttpPost(baseUrl + "/social/message/send/" + recipient);
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }
        StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response searchMessages(String username, String password, String senders, Date start, Date end, String read, String from, HttpClient client) throws IOException {
        boolean first = true;
        StringBuilder params = new StringBuilder("?");

        if(senders != null) {
            params.append("others=");
            params.append(senders);
            first = false;
        }
        if(start != null) {
            if(!first) {
                params.append("&");
            }
            params.append("start=");
            params.append(start.getTime());
            first = false;
        }
        if(end != null) {
            if(!first) {
                params.append("&");
            }
            params.append("end=");
            params.append(end.getTime());
            first = false;
        }
        if(read != null) {
            if(!first) {
                params.append("&");
            }
            params.append("read=");
            params.append(read);
            first = false;
        }
        if(from != null) {
            if(!first) {
                params.append("&");
            }
            params.append("from=");
            params.append(from);
            first = false;
        }

        HttpGet request;
        if(!first) {
            request = new HttpGet(baseUrl + "/social/message/search" + params);
        }
        else {
            request = new HttpGet(baseUrl + "/social/message/search");
        }
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response getSocialNotifications(String username, String password, HttpClient client) throws IOException {
        HttpGet request = new HttpGet(baseUrl + "/social/notifications");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response requestGame(String username, String password, String other, Integer size, String requesterColor, String first, HttpClient client) throws IOException {
        boolean firstParam = true;
        String payload = "{";
        if(size != null) {
            payload += "\"size\": " + size.toString();
            firstParam = false;
        }
        if(requesterColor != null) {
            if(!firstParam) {
                payload += ",";
            }
            payload += "\"requesterColor\": \"" + requesterColor + "\"";
            firstParam = false;
        }
        if(first != null) {
            if(!firstParam) {
                payload += ",";
            }
            payload += "\"first\": \"" + first + "\"";
        }
        payload += "}";

        HttpPost request = new HttpPost(baseUrl + "/games/request/create/" + other);
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }
        StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response deleteGameRequest(String username, String password, String other, HttpClient client) throws IOException {
        HttpDelete request = new HttpDelete(baseUrl + "/games/request/cancel/" + other);
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response respondToGameRequest(String username, String password, String other, String answer, HttpClient client) throws IOException {
        String payload = "{\"text\": \"" + answer + "\"}";

        HttpPost request = new HttpPost(baseUrl + "/games/request/respond/" + other);
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }
        StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response checkIncomingGameRequests(String username, String password, HttpClient client) throws IOException {
        HttpGet request = new HttpGet(baseUrl + "/games/request/incoming");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response checkOutgoingGameRequests(String username, String password, HttpClient client) throws IOException {
        HttpGet request = new HttpGet(baseUrl + "/games/request/outgoing");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response requestRandomGame(String username, String password, int size, HttpClient client) throws IOException {
        HttpPost request = new HttpPost(baseUrl + "/games/request/random/create/" + size);
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response deleteRandomGameRequest(String username, String password, HttpClient client) throws IOException {
        HttpDelete request = new HttpDelete(baseUrl + "/games/request/random/cancel");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response getRandomRequestSize(String username, String password, HttpClient client) throws IOException {
        HttpGet request = new HttpGet(baseUrl + "/games/request/random/outgoing");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response searchGames(String username, String password, String opponents, Date start, Date end, String complete, String pending, String sizes, String winner, String color, HttpClient client) throws IOException {
        boolean first = true;
        StringBuilder params = new StringBuilder("?");

        if(opponents != null) {
            params.append("opponents=");
            params.append(opponents);
            first = false;
        }
        if(start != null) {
            if(!first) {
                params.append("&");
            }
            params.append("start=");
            params.append(start.getTime());
            first = false;
        }
        if(end != null) {
            if(!first) {
                params.append("&");
            }
            params.append("end=");
            params.append(end.getTime());
            first = false;
        }
        if(complete != null) {
            if(!first) {
                params.append("&");
            }
            params.append("complete=");
            params.append(complete);
            first = false;
        }
        if(pending != null) {
            if(!first) {
                params.append("&");
            }
            params.append("pending=");
            params.append(pending);
            first = false;
        }
        if(sizes != null) {
            if(!first) {
                params.append("&");
            }
            params.append("sizes=");
            params.append(sizes);
            first = false;
        }
        if(winner != null) {
            if(!first) {
                params.append("&");
            }
            params.append("winner=");
            params.append(winner);
            first = false;
        }
        if(color != null) {
            if(!first) {
                params.append("&");
            }
            params.append("color=");
            params.append(color);
            first = false;
        }

        HttpGet request;
        if(!first) {
            request = new HttpGet(baseUrl + "/games/search" + params);
        }
        else {
            request = new HttpGet(baseUrl + "/games/search");
        }
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response getGame(String username, String gameId, String password, boolean full, HttpClient client) throws IOException {
        HttpGet request;
        if(full) {
            request = new HttpGet(baseUrl + "/games/game/" + gameId + "?full=true");
        }
        else {
            request = new HttpGet(baseUrl + "/games/game/" + gameId);
        }
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response getPossibleMoves(String username, String gameId, String password, HttpClient client) throws IOException {
        HttpGet request = new HttpGet(baseUrl + "/games/game/" + gameId + "/possible");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response playTurn(String username, String gameId, String turn, String password, HttpClient client) throws IOException {
        String payload = "{\"text\": \"" + turn + "\"}";

        HttpPost request = new HttpPost(baseUrl + "/games/game/" + gameId + "/play");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }
        StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response getGameNotifications(String username, String password, HttpClient client) throws IOException {
        HttpGet request = new HttpGet(baseUrl + "/games/notifications");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response sendGameMessage(String username, String gameId, String message, String password, HttpClient client) throws IOException {
        String payload = "{\"text\": \"" + message + "\"}";

        HttpPost request = new HttpPost(baseUrl + "/games/game/" + gameId + "/send-message");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }
        StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response initializeAdminAccount(HttpClient client) throws IOException {
        HttpPost request = new HttpPost(baseUrl + "/admin/initialize");

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response changeAdminPassword(String username, String password, String newPass, HttpClient client) throws IOException {
        String payload = "{\"text\": \"" + newPass + "\"}";

        HttpPost request = new HttpPost(baseUrl + "/admin/change-pass");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }
        StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response resetUserPassword(String username, String password, String user, HttpClient client) throws IOException {
        HttpPost request = new HttpPost(baseUrl + "/admin/user/" + user + "/reset");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        return response;
    }

    public static Response banUser(String username, String password, String user, HttpClient client) throws IOException {
        HttpPost request = new HttpPost(baseUrl + "/admin/user/" + user + "/ban");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response unbanUser(String username, String password, String user, HttpClient client) throws IOException {
        HttpPost request = new HttpPost(baseUrl + "/admin/user/" + user + "/unban");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }

    public static Response unlockUser(String username, String password, String user, HttpClient client) throws IOException {
        HttpPost request = new HttpPost(baseUrl + "/admin/user/" + user + "/unlock");
        if(username != null && password != null) {
            request.setHeader("Authorization", getBasicAuthString(username, password));
        }

        Response response = new Response(client.execute(request));

        request.releaseConnection();

        return response;
    }
}
