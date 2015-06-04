package maximsblog.blogspot.com.timestatistic;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract.Colors;
import android.view.View;
import android.widget.RemoteViews;

public class CountWidgetProvider extends AppWidgetProvider {

	public void onUpdate(final Context context,
			AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			final int appWidgetId = appWidgetIds[i];
			updateWidget(context, appWidgetId);
		}
	}

	public static void updateWidget(final Context context, final int appWidgetId) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				AppWidgetManager appWidgetManager = AppWidgetManager
						.getInstance(context);
					Cursor c = context.getContentResolver().query(
							RecordsDbHelper.CONTENT_URI_TIMES,
							new String[] { RecordsDbHelper.ID, RecordsDbHelper.NAME,
									RecordsDbHelper.ISRUNNING, RecordsDbHelper.STARTTIME,
									RecordsDbHelper.TIMERSID },
							RecordsDbHelper.ISRUNNING + "=?",
							new String[] { String.valueOf(1) }, null);
					if (c.moveToFirst()) {
						int timeId = c.getInt(0);
						String name = c.getString(5);
						boolean isRunning = c.getInt(6) == 1;
						int color = c.getInt(7);
						long start = c.getLong(3);

						RemoteViews views = getRemoteViews(appWidgetId,
								context, name, color);
						appWidgetManager.updateAppWidget(appWidgetId, views);
					}
					c.close();
			}
		});
		t.start();
	}

	private static RemoteViews getRemoteViews(int appWidgetId, Context context,
			String name, int color) {
		
		Intent intent;
		intent = new Intent(context, MainActivity.class);
		Bundle extras = new Bundle();
		extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		intent.putExtras(extras);

		PendingIntent pendingIntent = PendingIntent.getActivity(context,
				appWidgetId, intent, 0);
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.c_appwidget_layout);
		views.setOnClickPendingIntent(R.id.background, pendingIntent);

		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		int backgroundResource = prefs.getInt("dc_background_"
				+ appWidgetId, android.R.color.transparent);
		int backgroundCounterAlpha = prefs.getInt("dc_transparent_"
				+ appWidgetId, android.R.color.transparent);
		int intColor = ~0xFFFFFF | (0xFFFFFF & ~color);
		color = ( backgroundCounterAlpha << 24 ) | ( color & 0x00ffffff );
		views.setInt(R.id.background_counter, "setBackgroundColor",
				color);
		views.setInt(R.id.background, "setBackgroundResource",
				backgroundResource);
		
		views.setTextViewText(R.id.value_text, name);
		views.setTextColor(R.id.value_text, intColor);
		return views;
	}


	@Override
	public void onDeleted(final Context context, int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			final int appWidgetId = appWidgetIds[i];
			Editor editor = PreferenceManager.getDefaultSharedPreferences(
					context).edit();
			editor.remove("dc_user_" + appWidgetId);
			editor.remove("dc_background_" + appWidgetId);
			editor.remove("dc_textcolor_" + appWidgetId);
			editor.remove("dc_titlevisible_" + appWidgetId);
			editor.commit();
		}
	}

	private void handleTouchWiz(Context context, Intent intent) {
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);

		int appWidgetId = intent.getIntExtra("widgetId", 0);
		int widgetSpanX = intent.getIntExtra("widgetspanx", 0);
		int widgetSpanY = intent.getIntExtra("widgetspany", 0);

		if (appWidgetId > 0 && widgetSpanX > 0 && widgetSpanY > 0) {
			Bundle newOptions = new Bundle();
			// We have to convert these numbers for future use
			newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
					widgetSpanY * 74);
			newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
					widgetSpanX * 74);

			// onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
			// newOptions);
		}
	}
}
