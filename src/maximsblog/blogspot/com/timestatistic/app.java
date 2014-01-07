package maximsblog.blogspot.com.timestatistic;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.preference.PreferenceManager;

public class app extends Application {
	private static AlarmManagerBroadcastReceiver alarm = new AlarmManagerBroadcastReceiver();

	
	
	public static void loadRunningCounterAlarm(Context context) {
		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
				"alarm", false)) {
			setRunningCounterAlarmSettings(context);
		}
	}

	public static void setRunningCounterAlarmSettings(Context context) {
		Cursor c = context.getContentResolver().query(
				RecordsDbHelper.CONTENT_URI_TIMES,
				new String[] { RecordsDbHelper.ID, RecordsDbHelper.NAME,
						RecordsDbHelper.ISRUNNING },
				RecordsDbHelper.ISRUNNING + "='1'", null, null);
		c.moveToFirst();
		alarm.SetAlarm(context,c.getString(5), c.getLong(8));
		c.close();
	}
	
	public static void delAlarm(Context context) {
		app.alarm.CancelAlarm(context);
	}
}
