package maximsblog.blogspot.com.timestatistic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.preference.PreferenceManager;

public class BootUpReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		if(intent.getAction()!=null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
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
						app.vibration = PreferenceManager.getDefaultSharedPreferences(context)
								.getBoolean("vibration", false);
						app.loadRunningCounterAlarm(context);
						app.setStatusBar(context);
						// Release the lock
						if (wl != null)
							wl.release();
					}
				}


			});
			t.start();
		}
	}
}
