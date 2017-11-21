package maximsblog.blogspot.com.timestatistic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class BootUpReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				PowerManager pm = (PowerManager) context
						.getSystemService(Context.POWER_SERVICE);
				PowerManager.WakeLock wl = pm.newWakeLock(
						PowerManager.PARTIAL_WAKE_LOCK, "timestatistic");
				// Acquire the lock
				wl.acquire();
				app.loadRunningCounterAlarm(context);
				app.setStatusBar(context);
				// Release the lock
				wl.release();
			}

			
		});
		t.start();
	}
}
