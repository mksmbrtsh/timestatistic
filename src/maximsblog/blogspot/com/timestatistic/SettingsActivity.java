package maximsblog.blogspot.com.timestatistic;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.Toast;

public class SettingsActivity extends SherlockPreferenceActivity implements
		OnPreferenceChangeListener {

	
	
	public static final String STARTTIMEFILTER = "startdatetimefilter";
	public static final String ENDTIMEFILTER = "enddatetimefilter";

	public static final String STARTTIMEFILTERPERIOD = "startdatetimefilterperiod";

	public static final String ENDTIMEFILTERPERIOD = "enddatetimefilterperiod";

	public static final String ENDTIMEFILTEREXPORT = "enddatetimefilterexport";
	public static final String STARTTIMEFILTEREXPORT = "startdatetimefilterexport";
	
	public static final String ENDTIMEFILTEREXPORTCSV = "enddatetimefilterexportcsv";
	public static final String STARTTIMEFILTEREXPORTCSV = "startdatetimefilterexportcsv";
	
	public static final class STARTTIMEFILTERS
	{
		public static final int TODAY = 0;
		public static final int YESTERDAY = 1;
		public static final int WEEK = 2;
		public static final int MOUNTH = 3;
		public static final int YEAR = 4;
		public static final int ALLTIME = 5;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		Preference p = findPreference("visible_notif");
		p.setOnPreferenceChangeListener(this);
		p = findPreference("alarm");
		p.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals("alarm")) {
			if ((Boolean) newValue) {
				app.setRunningCounterAlarmSettings(getApplicationContext());
			} else {
				app.delAlarm(getApplicationContext());
			}
		} else {
			Cursor c = getContentResolver().query(
					RecordsDbHelper.CONTENT_URI_TIMES,
					new String[] { RecordsDbHelper.ID, RecordsDbHelper.NAME,
							RecordsDbHelper.ISRUNNING },
					RecordsDbHelper.ISRUNNING + "='1'", null, null);
			c.moveToFirst();

			if ((Boolean) newValue) {
				visibleNotif(this, c.getLong(3), c.getLong(2), c.getString(5),
						true);
			} else {
				visibleNotif(this, c.getLong(3), c.getLong(2), c.getString(5),
						false);
			}
			c.close();
		}
		return true;
	}

	public static void visibleNotif(Context context, long start, long lenght,
			String name, boolean visible) {
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(100);
		if (visible) {
			final Intent intent1 = new Intent(context, MainActivity.class);
			final PendingIntent contentIntent = PendingIntent.getActivity(
					context.getApplicationContext(), 0, intent1,
					Intent.FLAG_ACTIVITY_CLEAR_TASK);
			Builder mBuilder;
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				mBuilder = new NotificationCompat.Builder(
						context).setSmallIcon(R.drawable.ic_notification)
						.setContentTitle(name).setOngoing(false)
						.setWhen(new Date().getTime() - lenght)
						.setAutoCancel(false).setUsesChronometer(true);
				else mBuilder = new NotificationCompat.Builder(
					context).setSmallIcon(R.drawable.ic_status_bar_not)
					.setContentTitle(name).setOngoing(false)
					.setWhen(new Date().getTime() - lenght)
					.setAutoCancel(false).setUsesChronometer(true);

			Notification n = mBuilder.build();
			n.contentIntent = contentIntent;
			n.when = new Date().getTime() - lenght;

			n.flags = Notification.FLAG_ONGOING_EVENT;
			mNotificationManager.notify(100, n);
		}
	}

	public static void visibleNotif(Context context, long start, long lenght,
			String string) {
		boolean visible = PreferenceManager.getDefaultSharedPreferences(
				context.getApplicationContext()).getBoolean("visible_notif",
				false);
		visibleNotif(context, start, lenght, string, visible);
	}
}
