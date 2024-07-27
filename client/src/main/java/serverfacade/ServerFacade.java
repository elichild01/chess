package serverfacade;

import com.google.gson.Gson;
import requestresult.LoginResult;

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
        HttpURLConnection connection = getHttpURLConnection();

        var body = Map.of("username", username, "password", password);
        try (OutputStream requestBody = connection.getOutputStream()) {
            var jsonBody = new Gson().toJson(body);
            requestBody.write(jsonBody.getBytes());
        }

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (InputStream responseBody = connection.getInputStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(responseBody);
                var response = new Gson().fromJson(inputStreamReader, Map.class);
                String responseUsername = (String)response.get("username");
                String authToken = (String)response.get("authToken");
                System.out.printf("Successfully logged in user %s.%n", responseUsername);
                return new LoginResult(responseUsername, authToken);
            }
        } else {
            try (InputStream responseBody = connection.getErrorStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(responseBody);
                System.out.println(new Gson().fromJson(inputStreamReader, Map.class).get("message"));
                return new LoginResult(username, null);
            }
        }
    }

    private HttpURLConnection getHttpURLConnection() throws IOException {
        String route = "/session";
        String urlString = String.format("http://localhost:%d%s", port, route);
        URL url = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setReadTimeout(5000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.connect();

        return connection;
    }
}
