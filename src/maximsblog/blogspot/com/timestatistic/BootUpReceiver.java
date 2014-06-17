package maximsblog.blogspot.com.timestatistic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.PowerManager;
import android.preference.PreferenceManager;

public class BootUpReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
		// Acquire the lock
		wl.acquire();
		app.loadRunningCounterAlarm(context);
		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
				"visible_notif", false)) {
			Cursor c = context.getContentResolver().query(
					RecordsDbHelper.CONTENT_URI_TIMES,
					new String[] { RecordsDbHelper.ID, RecordsDbHelper.NAME,
							RecordsDbHelper.ISRUNNING },
					RecordsDbHelper.ISRUNNING + "='1'", null, null);
			c.moveToFirst();
			SettingsActivity.visibleNotif(context, c.getLong(3), c.getLong(2), c.getString(5), true);
			c.close();
		}
		// Release the lock
		wl.release();
	}

}
