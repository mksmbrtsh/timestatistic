package maximsblog.blogspot.com.timestatistic;

import java.io.File;
import java.io.IOException;
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
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;

public class SettingsActivity extends SherlockPreferenceActivity implements
		OnPreferenceChangeListener, OnPreferenceClickListener {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		Preference p = findPreference("visible_notif");
		p.setOnPreferenceChangeListener(this);
		p = findPreference("export");
		p.setOnPreferenceClickListener(this);
		p = findPreference("import");
		p.setOnPreferenceClickListener(this);
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
					Intent.FLAG_ACTIVITY_CLEAR_TASK);
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					context).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle((new Date(start)).toString())
					.setContentText(name).setOngoing(false).setWhen(start)
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

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals("export")) {
			OpenHelper o = new OpenHelper(getApplicationContext());
			try {
				o.exportDatabase(getFilesDir(), getExternalFilesDir(null)
						.getAbsolutePath() + File.separator + "1.db");

			} catch (IOException e) {
				e.printStackTrace();
			}
			o.close();
		} else {
			OpenHelper o = new OpenHelper(getApplicationContext());
			try {
				o.importDatabase(getFilesDir(), getExternalFilesDir(null)
						.getAbsolutePath() + File.separator + "1.db");

			} catch (IOException e) {
				e.printStackTrace();
			}
			o.close();
		}
		getContentResolver().notifyChange(RecordsDbHelper.CONTENT_URI_TIMES, null);
		return false;
	}

}
