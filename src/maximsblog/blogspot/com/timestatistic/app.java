package maximsblog.blogspot.com.timestatistic;

import java.util.Calendar;

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
	
	public static long getStartDate(Context context) {
		int checkedItem = PreferenceManager.getDefaultSharedPreferences(
				context).getInt(SettingsActivity.STARTTIMEFILTER, 0);
		Calendar calendar = Calendar.getInstance();
		long result;
		switch (checkedItem) {
		case SettingsActivity.STARTTIMEFILTERS.ALLTIME:
			result = 0;
			break;
		case SettingsActivity.STARTTIMEFILTERS.TODAY:
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			result = calendar.getTimeInMillis();
			break;
		case SettingsActivity.STARTTIMEFILTERS.WEEK:
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
			result = calendar.getTimeInMillis();
			break;
		case SettingsActivity.STARTTIMEFILTERS.MOUNTH:
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			result = calendar.getTimeInMillis();
			break;
		case SettingsActivity.STARTTIMEFILTERS.YEAR:
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.set(Calendar.MONTH, 0);
			result = calendar.getTimeInMillis();
			break;
		default:
			result = 0;
		}
		return result;
	}
}
