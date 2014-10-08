package maximsblog.blogspot.com.timestatistic;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.internal.cn;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.CancellationSignal;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.support.v4.content.CursorLoader;
import android.text.format.Time;
import android.view.View;
import android.widget.Toast;

public class ExportToGoogleCalendarService extends Service {

	public static final String EXPORT = "export_to_gcalendar_stop";
	public static boolean isRunning;

	private long mSelectStartItem;
	private long mSelectEndItem;
	private int[] mIDs;
	private boolean[] mChecked;
	private int mCalendarID;
	private boolean mExportNotes;
	private boolean mExportOnlyNotes;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Thread t = new Thread(new Runnable() {
			int i = 0;

			@SuppressLint("NewApi")
			@Override
			public void run() {
				String[] selectionArgs;
				Cursor cursor;
				if(mExportOnlyNotes) {
					selectionArgs = new String[] {
							"",
							String.valueOf(mSelectStartItem),
							String.valueOf(mSelectEndItem) };
					cursor = getContentResolver().query(
							RecordsDbHelper.CONTENT_URI_ALLNOTES, null,
							RecordsDbHelper.STARTTIME + " IS NOT NULL AND "
						+ RecordsDbHelper.NOTE + " LIKE ?",
							selectionArgs, null);
				} else {
					selectionArgs = new String[] {
							String.valueOf(mSelectStartItem),
							String.valueOf(mSelectEndItem) };
					cursor = getContentResolver().query(
							RecordsDbHelper.CONTENT_URI_ALLTIMES, null,
							RecordsDbHelper.STARTTIME + " IS NOT NULL ",
							selectionArgs, null);
				}
				
				int i1 = 0;
				ArrayList<Integer> ids = new ArrayList<Integer>();
				for (int i = 0; i < mIDs.length; i++) {
					if (mChecked[i])
						ids.add(mIDs[i]);
				}

				if (cursor.moveToFirst()) {
						if (ids.contains(cursor.getInt(0))) {
							ContentValues event = new ContentValues();
							event.put("calendar_id", mCalendarID); // ID
																		// календаря
																		// мы
																		// получили
							// ранее
							event.put(CalendarContract.Events.TITLE,
									cursor.getString(3)); // Название события
							if(mExportOnlyNotes) {
								event.put(CalendarContract.Events.DESCRIPTION, cursor.getString(8));
							} else if(mExportNotes) {
								Cursor c = getContentResolver().query(RecordsDbHelper.CONTENT_URI_NOTES, new String[] { RecordsDbHelper.ID3, RecordsDbHelper.NOTE },RecordsDbHelper.ID3+ "=?" , new String[] { String.valueOf(cursor.getInt(5)) }, null);
								if(c.getCount() == 1) {
									c.moveToFirst();
									event.put(CalendarContract.Events.DESCRIPTION, c.getString(1)); // Описание
								} else
									event.put(CalendarContract.Events.DESCRIPTION, ""); // Описание
								c.close();
							} else
								event.put(CalendarContract.Events.DESCRIPTION, ""); // Описание
																				// события
							event.put(CalendarContract.Events.EVENT_TIMEZONE,
									Time.getCurrentTimezone());
							event.put(CalendarContract.Events.DTSTART,
									Long.toString(cursor.getLong(2))); // время
																		// начала
							event.put(CalendarContract.Events.DTEND,
									Long.toString(cursor.getLong(7))); // время
																		// окончания
							event.put(CalendarContract.Events.HAS_ALARM, 0);// напоминание
							event.put("eventStatus", 1);// точное присутствие на
														// событии
							Uri count = getContentResolver().insert(
									CalendarContract.Events.CONTENT_URI, event);
						}
						Intent intent = new Intent();
						intent.setAction(EXPORT);
						intent.putExtra("count", cursor.getCount());
						intent.putExtra("progress", i1);
						getApplicationContext().sendBroadcast(intent);
						i1++;
				}
				cursor.close();
				ExportToGoogleCalendarService.this.stopSelf();
				Intent intent3 = new Intent();
				intent3.setAction(EXPORT);
				getApplicationContext().sendBroadcast(intent3);

			}
		});

		isRunning = true;
		mSelectStartItem = intent.getLongExtra("start", mSelectStartItem);
		mSelectEndItem = intent.getLongExtra("stop", mSelectEndItem);
		mIDs = intent.getIntArrayExtra("ids");
		mChecked = intent.getBooleanArrayExtra("checked");
		mCalendarID = Integer.valueOf(intent.getStringExtra("calendar_id"));
		mExportNotes = intent.getBooleanExtra("export_notes", false);
		mExportOnlyNotes = intent.getBooleanExtra("export_only_notes", false);
		t.start();
		Toast.makeText(ExportToGoogleCalendarService.this, R.string.one_record_export, Toast.LENGTH_LONG).show();
		return super.onStartCommand(intent, flags, startId);
	};

	@Override
	public IBinder onBind(Intent intent) {
		return new Binder();
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		isRunning = false;
		super.onDestroy();
	}
	

}
