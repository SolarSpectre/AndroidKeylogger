package com;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import java.util.concurrent.Executors;

public class Utils {
    private static final String TAG = "Utils";
    private static final String WEBHOOK_URL = "";
    public static void sendMessage(String logMessage) {

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONObject messageJSON = new JSONObject();
                messageJSON.put("content", "```\n" + logMessage + "\n```");

                URL url = new URL(WEBHOOK_URL);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(messageJSON.toString().getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    Log.d(TAG, "Message sent to Discord. Response code: " + responseCode);
                } else {
                    Log.e(TAG, "Failed to send message to Discord. Response code: " + responseCode);
                }
                connection.disconnect();
            } catch (JSONException e) {
                Log.e(TAG, "Failed to create JSON object: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Error sending message to Discord: " + e.getMessage());
            }
        });
    }
}


