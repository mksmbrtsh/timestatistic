package maximsblog.blogspot.com.timestatistic;

import android.annotation.SuppressLint;
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
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract.Colors;
import android.util.TypedValue;
import android.view.View;
import android.webkit.WebSettings.TextSize;
import android.widget.RemoteViews;
import android.widget.TextView;

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
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(context);
				int counterID = prefs.getInt(
						"dc_selected_count_" + appWidgetId, -1);

				AppWidgetManager appWidgetManager = AppWidgetManager
						.getInstance(context);
				Cursor c;
				if (counterID == -1) {
					c = context.getContentResolver().query(
							RecordsDbHelper.CONTENT_URI_TIMES,
							new String[] { RecordsDbHelper.ID,
									RecordsDbHelper.NAME,
									RecordsDbHelper.ISRUNNING,
									RecordsDbHelper.STARTTIME,
									RecordsDbHelper.TIMERSID },
							RecordsDbHelper.ISRUNNING + "=?",
							new String[] { String.valueOf(1) }, null);

				} else {
					c = context.getContentResolver().query(
							RecordsDbHelper.CONTENT_URI_TIMES,
							new String[] { RecordsDbHelper.ID,
									RecordsDbHelper.NAME,
									RecordsDbHelper.ISRUNNING,
									RecordsDbHelper.STARTTIME,
									RecordsDbHelper.TIMERSID },
							RecordsDbHelper.ID + "=?",
							new String[] { String.valueOf(counterID) }, null);

				}
				if (c.moveToFirst()) {
					boolean isRunning = c.getInt(6) == 1;
					String name = c.getString(5);
					
					int color = c.getInt(7);
					RemoteViews views = getRemoteViews(appWidgetId, context,
							name, color, counterID, prefs, isRunning);
					appWidgetManager.updateAppWidget(appWidgetId, views);
				}
				c.close();
			}
		});
		t.start();
	}

	@SuppressLint("NewApi")
	private static RemoteViews getRemoteViews(int appWidgetId, Context context,
			String name, int color, int counterID, SharedPreferences prefs, boolean isRunning) {

		Intent intent;

		PendingIntent pendingIntent;
		if (counterID != -1) {
			intent = new Intent(context, SwitchCounterReceiver.class);
			Bundle extras = new Bundle();
			extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			extras.putInt("selected_count", counterID);
			intent.putExtras(extras);
			pendingIntent = PendingIntent.getBroadcast(context, appWidgetId,
					intent, 0);
		} else {
			intent = new Intent(context, MainActivity.class);
			Bundle extras = new Bundle();
			extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			intent.putExtras(extras);
			pendingIntent = PendingIntent.getActivity(context, appWidgetId,
					intent, 0);
		}
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.c_appwidget_layout);
		views.setOnClickPendingIntent(R.id.background, pendingIntent);

		int backgroundResource = prefs.getInt("dc_background_" + appWidgetId,
				android.R.color.transparent);
		int backgroundCounterAlpha = prefs.getInt("dc_transparent_"
				+ appWidgetId, android.R.color.transparent);
		int intColor = ~0xFFFFFF | (0xFFFFFF & ~color);
		color = (backgroundCounterAlpha << 24) | (color & 0x00ffffff);
		views.setInt(R.id.background_counter, "setBackgroundColor", color);
		views.setInt(R.id.background, "setBackgroundResource",
				backgroundResource);
		int fontSize = prefs.getInt("dc_fontsize_" + appWidgetId, 20);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			views.setTextViewTextSize(R.id.status_text,
					TypedValue.COMPLEX_UNIT_SP, fontSize);
		} else {
			views.setFloat(R.id.value_text, "setTextSize", fontSize);
		}
		views.setTextViewText(R.id.value_text, name);
		String [] ss = name.split("\n");
		if (counterID != -1) {
			views.setViewVisibility(R.id.status_text, View.VISIBLE);
			if (!isRunning) {
				views.setTextViewText(R.id.status_text,context.getString(R.string.off));
			} else
				views.setTextViewText(R.id.status_text,context.getString(R.string.on));
		} else {
			views.setViewVisibility(R.id.status_text, View.GONE);
		}
		views.setTextColor(R.id.value_text, intColor);
		views.setTextColor(R.id.status_text, intColor);
		
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
