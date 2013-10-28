package maximsblog.blogspot.com.timestatistic;

import java.util.Date;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;

public class SettingsActivity extends SherlockPreferenceActivity implements
		OnPreferenceChangeListener {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		Preference p = findPreference("visible_notif");
		p.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Cursor c = getContentResolver().query(
				RecordsDbHelper.CONTENT_URI_TIMES,
				new String[] { RecordsDbHelper.ID, RecordsDbHelper.NAME,
						RecordsDbHelper.ISRUNNING },
				RecordsDbHelper.ISRUNNING + "='1'", null, null);
		c.moveToFirst();

		if ((Boolean) newValue) {
			visibleNotif(this, c.getLong(3), c.getString(5), true);
		} else {
			visibleNotif(this, c.getLong(3), c.getString(5), false);
		}
		c.close();
		return true;
	}

	public static void visibleNotif(Context context, long start, String name,
			boolean visible) {
		if (visible) {
			final Intent intent1 = new Intent(context, MainActivity.class);
			final PendingIntent contentIntent = PendingIntent.getActivity(
					context.getApplicationContext(), 0, intent1,
					Intent.FLAG_ACTIVITY_NO_HISTORY);
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					context).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle((new Date(start)).toLocaleString())
					.setContentText(name).setOngoing(false)
					.setAutoCancel(false).setUsesChronometer(true);
			NotificationManager mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification n = mBuilder.build();
			n.contentIntent = contentIntent;
			n.flags = Notification.FLAG_ONGOING_EVENT;
			mNotificationManager.notify(100, n);
		} else {
			NotificationManager mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(100);
		}
	}

	public static void visibleNotif(Context context, long start, String string) {
		boolean visible = PreferenceManager.getDefaultSharedPreferences(
				context.getApplicationContext()).getBoolean("visible_notif",
				false);
		visibleNotif(context, start, string, visible);
	}

}
