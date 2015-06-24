package maximsblog.blogspot.com.timestatistic;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SwitchCounterReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		int counterId = intent.getIntExtra("selected_count", -1);
		// running counter
		ContentValues cv = new ContentValues();
		long now = new Date().getTime();
		Cursor c = context.getContentResolver().query(
				RecordsDbHelper.CONTENT_URI_TIMES,
				new String[] { RecordsDbHelper.ID, RecordsDbHelper.NAME,
						RecordsDbHelper.ISRUNNING, RecordsDbHelper.STARTTIME,
						RecordsDbHelper.TIMERSID },
				RecordsDbHelper.ISRUNNING + "=?",
				new String[] { String.valueOf(1) }, null);
		c.moveToFirst();
		int timeId = c.getInt(0);
		long start = c.getLong(3);
		c.close();
		// set value to running counter
		long lenght = now - start;
		cv = new ContentValues();
		cv.put(RecordsDbHelper.LENGHT, lenght);
		cv.put(RecordsDbHelper.ENDTIME, start + lenght);
		context.getContentResolver().update(RecordsDbHelper.CONTENT_URI_TIMES,
				cv, RecordsDbHelper.ID2 + "=?",
				new String[] { String.valueOf(timeId) });
		cv.clear();

		Cursor cursor = context.getContentResolver().query(
				RecordsDbHelper.CONTENT_URI_TIMES,
				new String[] { RecordsDbHelper.ID, RecordsDbHelper.NAME,
						RecordsDbHelper.ISRUNNING, RecordsDbHelper.STARTTIME,
						RecordsDbHelper.TIMERSID },
				RecordsDbHelper.TIMERSID + "=?",
				new String[] { String.valueOf(counterId) }, null);
		if (cursor.moveToFirst()) {

			boolean isRunning = cursor.getInt(6) == 1;

			if (isRunning) {
				// if click to running counter, then switch to idle-counter
				counterId = 1;
			} else {
				counterId = cursor.getInt(4);
			}

			cv.put(RecordsDbHelper.TIMERSID, counterId);
			cv.put(RecordsDbHelper.STARTTIME, now);
			context.getContentResolver().insert(
					RecordsDbHelper.CONTENT_URI_TIMES, cv);
			cv.clear();
			cv.put(RecordsDbHelper.ISRUNNING, 1);
			context.getContentResolver().update(
					RecordsDbHelper.CONTENT_URI_TIMERS, cv,
					RecordsDbHelper.ID + " = ?",
					new String[] { String.valueOf(counterId) });
			app.loadRunningCounterAlarm(context.getApplicationContext());
			app.setStatusBar(context.getApplicationContext());
			((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE))
					.vibrate(100);
			app.updateDayCountAppWidget(context);
		} else
			Toast.makeText(context,
					context.getText(R.string.counter_widget_err),
					Toast.LENGTH_LONG).show();
		cursor.close();
		Editor edit = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
		edit.putBoolean("reload", true);
		edit.commit();
	}

}
