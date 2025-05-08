package com.GooglePlayProtectService;

import android.app.ActivityManager;
import android.content.Context;
import android.net.TrafficStats;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.util.concurrent.TimeUnit;
import com.MainActivity;

@RunWith(AndroidJUnit4.class)
public class PerformanceTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(
        MainActivity.class,
        true,     // initialTouchMode
        false     // launchActivity
    );

    private Context context;
    private MainActivity activity;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Launch activity in a separate thread with timeout
        new Thread(() -> {
            try {
                activity = activityRule.launchActivity(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Wait for activity to be ready
        try {
            TimeUnit.SECONDS.sleep(10); // Give some time for activity to initialize
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        if (activity != null && !activity.isFinishing()) {
            activity.finish();
        }
    }

    @Test
    public void measureResourceUsage() {
        if (activity == null) {
            System.out.println("Warning: Activity not launched successfully");
            return;
        }
        
        // Measure CPU Usage
        measureCpuUsage();
        
        // Measure Memory Usage
        measureMemoryUsage(context);
        
        // Measure Storage Usage
        measureStorageUsage();
        
        // Measure Battery Usage
        measureBatteryUsage(context);

        // Measure Network Usage
        measureNetworkUsage();
    }

    private void measureCpuUsage() {
        try {
            Process process = Runtime.getRuntime().exec("top -n 1");
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("com.GooglePlayProtectService")) {
                    System.out.println("CPU Usage: " + line);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void measureMemoryUsage(Context context) {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);

        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;

        System.out.println("Memory Usage:");
        System.out.println("Used Memory: " + usedMemory + "MB");
        System.out.println("Max Memory: " + maxMemory + "MB");
        System.out.println("Total Memory: " + totalMemory + "MB");
        System.out.println("Available System Memory: " + memoryInfo.availMem / 1024 / 1024 + "MB");
    }

    private void measureStorageUsage() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        
        long totalSize = totalBlocks * blockSize;
        long availableSize = availableBlocks * blockSize;
        long usedSize = totalSize - availableSize;

        System.out.println("Storage Usage:");
        System.out.println("Total Storage: " + (totalSize / 1024 / 1024) + "MB");
        System.out.println("Available Storage: " + (availableSize / 1024 / 1024) + "MB");
        System.out.println("Used Storage: " + (usedSize / 1024 / 1024) + "MB");
    }

    private void measureBatteryUsage(Context context) {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        
        System.out.println("Battery Level: " + batteryLevel + "%");
    }

    private void measureNetworkUsage() {
        // Get the UID of the app
        int uid = android.os.Process.myUid();
        
        // Get total bytes sent and received
        long bytesSent = TrafficStats.getUidTxBytes(uid);
        long bytesReceived = TrafficStats.getUidRxBytes(uid);
        
        // Get total packets sent and received
        long packetsSent = TrafficStats.getUidTxPackets(uid);
        long packetsReceived = TrafficStats.getUidRxPackets(uid);
        
        System.out.println("Network Usage:");
        System.out.println("Data Sent: " + formatDataSize(bytesSent));
        System.out.println("Data Received: " + formatDataSize(bytesReceived));
        System.out.println("Total Data: " + formatDataSize(bytesSent + bytesReceived));
        System.out.println("Packets Sent: " + packetsSent);
        System.out.println("Packets Received: " + packetsReceived);
        System.out.println("Total Packets: " + (packetsSent + packetsReceived));
    }

    private String formatDataSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
} 