package serverfacade;

import chess.ChessGame;
import com.google.gson.Gson;
import requestresult.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class ServerFacade {
    private final int port;

    public ServerFacade(int port) {
        this.port = port;
    }

    public LoginResult login(String username, String password) throws IOException {
        String requestType = "POST";
        String route = "/session";
        HttpURLConnection connection = getHTTPConnection(requestType, route, null);

        LoginRequest request = new LoginRequest(username, password);
        try (OutputStream requestBody = connection.getOutputStream()) {
            var jsonBody = new Gson().toJson(request);
            requestBody.write(jsonBody.getBytes());
        }

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (InputStream responseBody = connection.getInputStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(responseBody);
                LoginResult response = new Gson().fromJson(inputStreamReader, LoginResult.class);
                System.out.printf("Successfully logged in user %s.%n", response.username());
                return response;
            }
        } else {
            try (InputStream responseBody = connection.getErrorStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(responseBody);
                System.out.println(new Gson().fromJson(inputStreamReader, Map.class).get("message"));
                return new LoginResult(username, null);
            }
        }
    }

    public RegisterResult register(String username, String password, String email) throws IOException {
        String requestType = "POST";
        String route = "/user";
        HttpURLConnection connection = getHTTPConnection(requestType, route, null);

        RegisterRequest request = new RegisterRequest(username, password, email);
        try (OutputStream requestBody = connection.getOutputStream()) {
            var jsonBody = new Gson().toJson(request);
            requestBody.write(jsonBody.getBytes());
        }

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (InputStream responseBody = connection.getInputStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(responseBody);
                RegisterResult response = new Gson().fromJson(inputStreamReader, RegisterResult.class);
                System.out.printf("Successfully logged in user %s.%n", response.username());
                return response;
            }
        } else {
            try (InputStream responseBody = connection.getErrorStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(responseBody);
                System.out.println(new Gson().fromJson(inputStreamReader, Map.class).get("message"));
                return new RegisterResult(username, null);
            }
        }
    }

    public LogoutResult logout(String authToken) throws IOException {
        String route = "/session";

        String urlString = String.format("http://localhost:%d%s", port, route);
        URL url = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setReadTimeout(5000);
        connection.setRequestMethod("DELETE");
        connection.setDoOutput(true);

        connection.addRequestProperty("authorization", authToken);
        connection.connect();

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            System.out.println("Successfully logged out.");
        } else {
            try (InputStream responseBody = connection.getErrorStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(responseBody);
                System.out.println(new Gson().fromJson(inputStreamReader, Map.class).get("message"));
            }
        }
        return new LogoutResult();
    }

    public CreateResult create(String authToken, String gameName) throws IOException {
        String requestType = "POST";
        String route = "/game";
        HttpURLConnection connection = getHTTPConnection(requestType, route, authToken);

        CreateRequest request = new CreateRequest(authToken, gameName);
        try (OutputStream requestBody = connection.getOutputStream()) {
            var jsonBody = new Gson().toJson(request);
            requestBody.write(jsonBody.getBytes());
        }

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (InputStream responseBody = connection.getInputStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(responseBody);
                CreateResult response = new Gson().fromJson(inputStreamReader, CreateResult.class);
                System.out.printf("Successfully created game %s.%n", gameName);
                return response;
            }
        } else {
            try (InputStream responseBody = connection.getErrorStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(responseBody);
                System.out.println(new Gson().fromJson(inputStreamReader, Map.class).get("message"));
                return new CreateResult(-1);
            }
        }
    }

    public JoinResult join(String authToken, ChessGame.TeamColor playerColor, int gameID) throws IOException {
        String requestType = "PUT";
        String route = "/game";
        HttpURLConnection connection = getHTTPConnection(requestType, route, authToken);

        JoinRequest request = new JoinRequest(authToken, playerColor, gameID);
        try (OutputStream requestBody = connection.getOutputStream()) {
            var jsonBody = new Gson().toJson(request);
            requestBody.write(jsonBody.getBytes());
        }

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (InputStream responseBody = connection.getInputStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(responseBody);
                JoinResult response = new Gson().fromJson(inputStreamReader, JoinResult.class);
                System.out.println("Successfully joined game.");
                return response;
            }
        } else {
            try (InputStream responseBody = connection.getErrorStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(responseBody);
                System.out.println(new Gson().fromJson(inputStreamReader, Map.class).get("message"));
                return new JoinResult();
            }
        }
    }

    public ClearResult clear() throws IOException {
        String requestType = "DELETE";
        String route = "/db";
        HttpURLConnection connection = getHTTPConnection(requestType, route, null);

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (InputStream responseBody = connection.getInputStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(responseBody);
                ClearResult response = new Gson().fromJson(inputStreamReader, ClearResult.class);
                System.out.println("Cleared all users, games, and auths.");
                return response;
            }
        } else {
            try (InputStream responseBody = connection.getErrorStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(responseBody);
                System.out.println(new Gson().fromJson(inputStreamReader, Map.class).get("message"));
                return new ClearResult();
            }
        }
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
}
