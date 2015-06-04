package maximsblog.blogspot.com.timestatistic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.Toast;

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
		alarm.SetAlarm(context, c.getString(5), c.getLong(8), true);
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

	public static FilterDateOption getStart(Context context, long checkedItem) {
		Calendar calendar = Calendar.getInstance();
		long result;
		String resultName;
		String[] startDateNames = context.getResources().getStringArray(
				R.array.StartFilters);
		if (checkedItem < 6) {
			resultName = startDateNames[(int) checkedItem];
			switch ((int) checkedItem) {

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
				try {
					result = context.getPackageManager().getPackageInfo(
							context.getPackageName(), 0).firstInstallTime;
				} catch (NameNotFoundException e) {
					e.printStackTrace();
					result = 1;
				}
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

	public static FilterDateOption getEnd(Context context, long checkedItem) {
		Calendar calendar = Calendar.getInstance();
		long result;
		String resultName;
		String[] startDateNames = context.getResources().getStringArray(
				R.array.EndFilters);
		if (checkedItem < 6) {
			resultName = startDateNames[(int) checkedItem];
			switch ((int) checkedItem) {

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

	public static FilterDateOption getStartDateExport(
			ExportToGoogleCalendarActivity context) {
		long checkedItem = PreferenceManager.getDefaultSharedPreferences(
				context).getLong(SettingsActivity.STARTTIMEFILTEREXPORT, 5);
		return getStart(context, checkedItem);
	}

	public static FilterDateOption getEndDateExport(
			ExportToGoogleCalendarActivity context) {
		long checkedItem = PreferenceManager.getDefaultSharedPreferences(
				context).getLong(SettingsActivity.ENDTIMEFILTEREXPORT, 5);
		return getEnd(context, checkedItem);
	}

	public static FilterDateOption getStartDateExport(
			ExportToCSVActivity context) {
		long checkedItem = PreferenceManager.getDefaultSharedPreferences(
				context).getLong(SettingsActivity.STARTTIMEFILTEREXPORTCSV, 5);
		return getStart(context, checkedItem);
	}

	public static FilterDateOption getEndDateExport(ExportToCSVActivity context) {
		long checkedItem = PreferenceManager.getDefaultSharedPreferences(
				context).getLong(SettingsActivity.ENDTIMEFILTEREXPORTCSV, 5);
		return getEnd(context, checkedItem);
	}

	public static void BitmapShare(Context context, Bitmap b) {
			Toast.makeText(context,
					context.getString(R.string.share_diagram),
					Toast.LENGTH_LONG).show();
	}
	
	public static void setStatusBar(Context context) {
		boolean visible = PreferenceManager.getDefaultSharedPreferences(
				context.getApplicationContext()).getBoolean("visible_notif",
				false);
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(100);
		if (visible) {
			FilterDateOption startDateOption = app.getStartDate(context);
			long mStartdate = startDateOption.date;
			long mEnddate = -1;
			String[] selectionArgs = new String[] { String.valueOf(mStartdate),
					String.valueOf(mEnddate), String.valueOf(1) };

			Cursor c = context.getContentResolver().query(
					RecordsDbHelper.CONTENT_URI_TIMES,
					new String[] { RecordsDbHelper.ID, RecordsDbHelper.NAME,
							RecordsDbHelper.ISRUNNING },
					RecordsDbHelper.ISRUNNING + "=?", selectionArgs, null);
			if (c.moveToFirst()) {

				long start = c.getLong(3);
				long lenght = c.getLong(2);
				String name = c.getString(5);

				final Intent intent1 = new Intent(context, MainActivity.class);
				final PendingIntent contentIntent = PendingIntent.getActivity(
						context.getApplicationContext(), 0, intent1,
						Intent.FLAG_ACTIVITY_CLEAR_TASK);
				Builder mBuilder;
				if (start < mStartdate)
					start = mStartdate;
				long now = new Date().getTime();

				if (now > mEnddate && mEnddate != -1) {

				} else {
					lenght = now - start + lenght;
				}

				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(start);
				Calendar calendarNow = Calendar.getInstance();
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendarNow.set(Calendar.MILLISECOND, 0);
				calendarNow.set(Calendar.SECOND, 0);
				calendarNow.set(Calendar.MINUTE, 0);
				calendarNow.set(Calendar.HOUR_OF_DAY, 0);
				String contentText;
				String subText;

				if (calendar.getTimeInMillis() == calendarNow.getTimeInMillis()) {
					DateFormat simpleDateFormat = SimpleDateFormat
							.getTimeInstance();
					contentText = context.getString(R.string.sum_since) + ": "
							+ simpleDateFormat.format(mStartdate);
					subText = context.getString(R.string.switchtime) + ": "
							+ simpleDateFormat.format(start);
				} else {
					DateFormat simpleDateFormat = SimpleDateFormat
							.getDateTimeInstance();
					contentText = context.getString(R.string.sum_since) + ": "
							+ simpleDateFormat.format(mStartdate);
					subText = context.getString(R.string.switchtime) + ": "
							+ simpleDateFormat.format(start);
				}
				boolean alarm = PreferenceManager.getDefaultSharedPreferences(
						context).getBoolean("alarm", false);

				mBuilder = new NotificationCompat.Builder(context)

						.setSmallIcon(R.drawable.ic_status_bar_not)
						.setContentTitle(
								context.getString(R.string.now) + ": " + name)
						.setOngoing(false).setSubText(contentText)
						.setContentText(subText)
						.setWhen(new Date().getTime() - lenght)
						.setAutoCancel(false).setUsesChronometer(true);
				if (alarm)
					mBuilder.setLargeIcon(BitmapFactory.decodeResource(
							context.getResources(), R.drawable.ic_status_bar_not));
				Notification n = mBuilder.build();
				n.contentIntent = contentIntent;
				n.when = new Date().getTime() - lenght;

				n.flags = Notification.FLAG_ONGOING_EVENT;
				mNotificationManager.notify(100, n);
			}
			c.close();
		}
	}
	
	public static void updateDayCountAppWidget(Context context) {
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
		ComponentName widgetComponent = new ComponentName(context, CountWidgetProvider.class);
		int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);
		
		Intent update = new Intent(context, CountWidgetProvider.class);
		update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
		update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		context.getApplicationContext().sendBroadcast(update);
	}
}
