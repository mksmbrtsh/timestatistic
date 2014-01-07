package maximsblog.blogspot.com.timestatistic;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

	 final public static String ONE_TIME = "onetime";
	 final public static String NAME = "name";
     @Override
     public void onReceive(Context context, Intent intent) {
      PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
      PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
      //Acquire the lock
      wl.acquire();
      Bundle extras = intent.getExtras();
      String name = extras.getString("name");
      visibleNotif(context, name);
      //Release the lock
      wl.release();
     }
     
     public static void visibleNotif(Context context, String name) {
 		NotificationManager mNotificationManager = (NotificationManager) context
 				.getSystemService(Context.NOTIFICATION_SERVICE);
 			final Intent intent1 = new Intent(context, MainActivity.class);
 			final PendingIntent contentIntent = PendingIntent.getActivity(
 					context.getApplicationContext(), 0, intent1,
 					Intent.FLAG_ACTIVITY_CLEAR_TASK);
 			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
 					context).setSmallIcon(R.drawable.ic_notification).setLights(Color.RED, 500, 500);
 			Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if(alarmSound == null){
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                if(alarmSound == null){
                    alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                }
            }
            mBuilder.setSound(alarmSound);
            Format formatter = new SimpleDateFormat("HH:mm");
 			Notification n = mBuilder.setContentTitle(name!=null ? name : context.getString(R.string.notif)).setContentText(formatter.format(new Date())).build();
 			n.contentIntent = contentIntent;
 			n.flags = Notification.FLAG_AUTO_CANCEL;
 			mNotificationManager.notify(101 , n);
 			((Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
 	}
     
     
     
     
     
     public void SetAlarm(Context context, String name, long l)
 {
     AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
     Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
     intent.putExtra(ONE_TIME, Boolean.FALSE);
     intent.putExtra(NAME, name);
     PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
     am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), l  , pi); 
 }

 public void CancelAlarm(Context context)
 {
     Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
     PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
     AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
     alarmManager.cancel(sender);
 }
/* public void setOnetimeTimer(Context context){
         AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
     Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
     intent.putExtra(ONE_TIME, Boolean.TRUE);
     PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
     am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
 }*/

}
