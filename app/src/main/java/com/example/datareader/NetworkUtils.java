package com.example.datareader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NetworkUtils {

    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 10000;

    private NetworkUtils() {
        // Utility class should not be instantiated.
    }

    public static String downloadText(String urlAddress) throws IOException {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlAddress);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("Accept", "application/xml, application/json");
            connection.connect();

            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Serverio atsakymo kodas: " + responseCode);
            }

            InputStream inputStream = connection.getInputStream();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8)
            );

            StringBuilder responseText = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                responseText.append(line);
            }

            reader.close();
            return responseText.toString();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}