package maximsblog.blogspot.com.timestatistic;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Vibrator;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

	final public static String ONE_TIME = "onetime";
	final public static String NAME = "name";

	@Override
	public void onReceive(final Context context, Intent intent) {

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				PowerManager pm = (PowerManager) context
						.getSystemService(Context.POWER_SERVICE);
				if(pm!= null) {
					PowerManager.WakeLock wl = pm.newWakeLock(
							PowerManager.PARTIAL_WAKE_LOCK, "timestatistic");
					// Acquire the lock
					if (wl != null)
						wl.acquire(10000);
					visible_notification(context);
					// Release the lock
					if (wl != null)
						wl.release();
				}
			}

		});
		t.start();
	}

	private void visible_notification(Context context) {
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		try {
			mNotificationManager.cancel(101);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		final Intent notificationIntent = new Intent(context, MainActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		notificationIntent.setAction(Long.toString(System.currentTimeMillis()));
		final PendingIntent contentIntent = PendingIntent.getActivity(
				context.getApplicationContext(), 0, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Notification.Builder mBuilder;
		mBuilder = new Notification.Builder(context).setSmallIcon(
				R.drawable.ic_notification).setLights(Color.RED, 500, 500);
		Uri alarmSound = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		if (alarmSound == null) {
			alarmSound = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			if (alarmSound == null) {
				alarmSound = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_ALARM);
			}
		}
		mBuilder.setSound(alarmSound);
		Format formatter = new SimpleDateFormat("HH:mm", context.getResources().getConfiguration().locale);
		FilterDateOption startDateOption = app.getStartDate(context);
		long mStartdate = startDateOption.date;
		long mEnddate = -1;
		String[] selectionArgs = new String[] { String.valueOf(mStartdate),
				String.valueOf(mEnddate), String.valueOf(1) };

		Cursor cursor = context.getContentResolver().query(
				RecordsDbHelper.CONTENT_URI_TIMES, null,
				RecordsDbHelper.ISRUNNING + "=?", selectionArgs, null);
		String name = null;
		long lenght = 0;
		long start = 0;
		long rememberInterval = 0;
		long now = new Date().getTime();
		if(cursor != null) {
			if (cursor.moveToFirst()) {
				name = cursor.getString(5);
				rememberInterval = cursor.getLong(8);
				start = cursor.getLong(3);
				if (start < mStartdate)
					start = mStartdate;
				if (now > mEnddate && mEnddate != -1) {
					lenght = cursor.getLong(2);
				} else {
					lenght = now - start + cursor.getLong(2);
				}
			}
			cursor.close();
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
			DateFormat simpleDateFormat = SimpleDateFormat.getTimeInstance();
			contentText = context.getString(R.string.since) + ": "
					+ simpleDateFormat.format(start);
			subText = context.getString(R.string.alerttime) + ": "
					+ simpleDateFormat.format(new Date());
		} else {
			DateFormat simpleDateFormat = SimpleDateFormat
					.getDateTimeInstance();
			contentText = context.getString(R.string.since) + ": "
					+ simpleDateFormat.format(start);
			subText = context.getString(R.string.alerttime) + ": "
					+ simpleDateFormat.format(new Date());
		}
		Notification n = mBuilder
				.setContentTitle(
						name + " (" + context.getString(R.string.alarm_notif)
								+ ")").setContentText(contentText)
				.setWhen((new Date().getTime() - lenght))
				.setUsesChronometer(true).setSubText(subText)
				.setNumber((int) Math.ceil((now - start) / rememberInterval))
				.setSmallIcon(R.drawable.ic_notification)
				.setLargeIcon(
						BitmapFactory.decodeResource(context.getResources(),
								R.drawable.ic_notification)).build();
		n.contentIntent = contentIntent;
		n.flags = Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(101, n);
		if (app.vibration)
			try {
				((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE))
						.vibrate(100);
			} catch (NullPointerException e){
				e.printStackTrace();
			}
	}

	public String getTime(Context context, double time) {
		int day;
		int hours;
		int minutes;
		int seconds;
		day = (int) (time / (24 * 60 * 60 * 1000));
		hours = (int) (time / (60 * 60 * 1000)) - day * 24;
		minutes = (int) (time / (60 * 1000)) - day * 24 * 60 - 60 * hours;
		String s;
		if (day > 0) {
			s = String.format(context.getResources().getConfiguration().locale,"%s\n%02d:%02d",
					getTimeString(context, "day", day), hours, minutes);
		} else
			s = String.format(context.getResources().getConfiguration().locale,"%02d:%02d", hours, minutes);
		return s;
	}

	private String getTimeString(Context context, String res, int l) {
		StringBuilder s = new StringBuilder();
		s.append(l);
		s.append(' ');
		if (l == 1 || (l % 10 == 1 && l != 11)) {
			s.append(context.getString(context.getResources().getIdentifier(
					res + "1", "string", context.getPackageName())));
		} else if ((l % 10 == 2 || l % 10 == 3 || l % 10 == 4) && l != 12
				&& l != 13 && l != 14) {
			s.append(context.getString(context.getResources().getIdentifier(
					res + "234", "string", context.getPackageName())));
		} else
			s.append(context.getString(context.getResources().getIdentifier(
					res + "s", "string", context.getPackageName())));
		return s.toString();
	}

	public void SetAlarm(Context context, String name, long l, boolean vibro) {
		CancelAlarm(context);

		Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
		intent.putExtra(ONE_TIME, Boolean.FALSE);
		intent.putExtra(NAME, name);
		intent.putExtra(NAME, vibro);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		if(alarmManager!=null)
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis() + l, l, pi);
	}

	public void CancelAlarm(Context context) {
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		try {
			mNotificationManager.cancel(101);
		} catch (NullPointerException e){
			e.printStackTrace();
		}
		Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
		PendingIntent sender = PendingIntent
				.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		if(alarmManager != null)
			alarmManager.cancel(sender);
	}
}
