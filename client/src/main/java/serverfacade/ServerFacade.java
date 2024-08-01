package serverfacade;

import chess.ChessGame;
import com.google.gson.Gson;
import requestresult.ListResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ServerFacade {
    private final int port;

    public ServerFacade(int port) {
        this.port = port;
    }

    public Map<String, Object> login(String username, String password) throws IOException {
        String requestType = "POST";
        String route = "/session";
        HttpURLConnection connection = getHTTPConnection(requestType, route, null);

        Map<String, Object> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);

        return handleRequest(connection, request, HashMap.class);
    }

    public Map<String, Object> register(String username, String password, String email) throws IOException {
        String requestType = "POST";
        String route = "/user";
        HttpURLConnection connection = getHTTPConnection(requestType, route, null);

        Map<String, Object> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);
        request.put("email", email);
        return handleRequest(connection, request, HashMap.class);
    }

    public Map<String, Object> logout(String authToken) throws IOException {
        String requestType = "DELETE";
        String route = "/session";
        HttpURLConnection connection = getHTTPConnection(requestType, route, authToken);

        return handleRequest(connection, null, HashMap.class);
    }

    public ListResult list(String authToken) throws IOException {
        String requestType = "GET";
        String route = "/game";
        HttpURLConnection connection = getHTTPConnection(requestType, route, authToken);

        return handleRequest(connection, null, ListResult.class);
    }

    public Map<String, Object> create(String authToken, String gameName) throws IOException {
        String requestType = "POST";
        String route = "/game";
        HttpURLConnection connection = getHTTPConnection(requestType, route, authToken);

        Map<String, Object> request = new HashMap<>();
        request.put("gameName", gameName);
        return handleRequest(connection, request, HashMap.class);
    }

    public Map<String, Object> join(String authToken, ChessGame.TeamColor playerColor, int gameID) throws IOException {
        String requestType = "PUT";
        String route = "/game";
        HttpURLConnection connection = getHTTPConnection(requestType, route, authToken);

        Map<String, Object> request = new HashMap<>();
        request.put("playerColor", playerColor);
        request.put("gameID", gameID);
        return handleRequest(connection, request, HashMap.class);
    }

    public Map<String, Object> clear() throws IOException {
        String requestType = "DELETE";
        String route = "/db";
        HttpURLConnection connection = getHTTPConnection(requestType, route, null);

        System.out.println("Cleared all users, games, and auths.");
        return handleRequest(connection, null, HashMap.class);
    }

    private HttpURLConnection getHTTPConnection(String httpType, String route, String authToken) throws IOException {
        String urlString = String.format("http://localhost:%d%s", port, route);
        URL url = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setReadTimeout(5000);
        connection.setRequestMethod(httpType);
        connection.setDoOutput(true);

        if (authToken != null) {
            connection.addRequestProperty("authorization", authToken);
        }
        connection.connect();

        return connection;
    }

    private <T> T handleRequest(HttpURLConnection connection, Map<String, Object> request, Class<T> responseClass) throws IOException {
        if (request != null) {
            try (OutputStream requestBody = connection.getOutputStream()) {
                var jsonBody = new Gson().toJson(request);
                requestBody.write(jsonBody.getBytes());
            }
        }

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (InputStream responseBody = connection.getInputStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(responseBody);
                return new Gson().fromJson(inputStreamReader, responseClass);
            }
        } else {
            try (InputStream responseBody = connection.getErrorStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(responseBody);
                return new Gson().fromJson(inputStreamReader, responseClass);
            }
        }
    }
}
