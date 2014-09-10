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
		alarm.SetAlarm(context, c.getString(5), c.getLong(8));
		c.close();
	}

	public static void delAlarm(Context context) {
		app.alarm.CancelAlarm(context);
	}

	public static FilterDateOption getStartDatePeriod(Context context) {
		long checkedItem = PreferenceManager.getDefaultSharedPreferences(
				context).getLong(SettingsActivity.STARTTIMEFILTERPERIOD, 5);
		return getStart(context, checkedItem);
	}
	
	public static FilterDateOption getStartDate(Context context) {
		long checkedItem = PreferenceManager.getDefaultSharedPreferences(
				context).getLong(SettingsActivity.STARTTIMEFILTER, 5);
		return getStart(context, checkedItem);
	}
	
	private static FilterDateOption getStart(Context context, long checkedItem) {
		Calendar calendar = Calendar.getInstance();
		long result;
		String resultName;
		String[] startDateNames = context.getResources().getStringArray(
				R.array.StartFilters);
		if (checkedItem < 6) {
			resultName = startDateNames[(int) checkedItem];
			switch ((int)checkedItem) {

			case SettingsActivity.STARTTIMEFILTERS.TODAY:
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				result = calendar.getTimeInMillis();
				break;
			case SettingsActivity.STARTTIMEFILTERS.YESTERDAY:
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.add(Calendar.DATE, -1);
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
			case SettingsActivity.STARTTIMEFILTERS.ALLTIME:
				result = 1;
				break;
			default:
				result = 1;
			}
		} else {
			result = checkedItem;
			resultName = startDateNames[6];
		}
		FilterDateOption startDateOption = new FilterDateOption();
		startDateOption.date = result;
		startDateOption.dateName = resultName;
		return startDateOption;
	}

	public static FilterDateOption getEndDatePeriod(Context context) {
		long checkedItem = PreferenceManager.getDefaultSharedPreferences(
				context).getLong(SettingsActivity.ENDTIMEFILTERPERIOD, 5);
		return getEnd(context, checkedItem);
	}
	private static FilterDateOption getEnd(Context context, long checkedItem) {
		Calendar calendar = Calendar.getInstance();
		long result;
		String resultName;
		String[] startDateNames = context.getResources().getStringArray(
				R.array.EndFilters);
		if (checkedItem < 6) {
			resultName = startDateNames[(int) checkedItem];
			switch ((int)checkedItem) {

			case SettingsActivity.STARTTIMEFILTERS.TODAY:
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.add(Calendar.DATE, 1);
				result = calendar.getTimeInMillis();
				break;
			case SettingsActivity.STARTTIMEFILTERS.YESTERDAY:
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
				calendar.add(Calendar.DATE, 7);
				result = calendar.getTimeInMillis();
				break;
			case SettingsActivity.STARTTIMEFILTERS.MOUNTH:
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.DAY_OF_MONTH, 1);
				calendar.add(Calendar.MONTH, 1);
				result = calendar.getTimeInMillis();
				break;
			case SettingsActivity.STARTTIMEFILTERS.YEAR:
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.DAY_OF_MONTH, 1);
				calendar.set(Calendar.MONTH, 0);
				calendar.add(Calendar.YEAR, 1);
				result = calendar.getTimeInMillis();
				break;
			case SettingsActivity.STARTTIMEFILTERS.ALLTIME:
				result = -1;
				break;
			default:
				result = -1;
			}
		} else {
			result = checkedItem;
			resultName = startDateNames[6];
		}
		FilterDateOption startDateOption = new FilterDateOption();
		startDateOption.date = result;
		startDateOption.dateName = resultName;
		return startDateOption;
	}

	public static FilterDateOption getEndDate(Context context) {
		long checkedItem = PreferenceManager.getDefaultSharedPreferences(
				context).getLong(SettingsActivity.ENDTIMEFILTER, 5);
		return getEnd(context, checkedItem);
	}
}
