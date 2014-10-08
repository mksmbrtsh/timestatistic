package maximsblog.blogspot.com.timestatistic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

public class ExportToCSVService extends Service {

	public static final String EXPORT = "export_to_gcalendar_stop";
	public static boolean isRunning;

	private long mSelectStartItem;
	private long mSelectEndItem;
	private int[] mIDs;
	private boolean[] mChecked;
	private boolean mExportNotes;
	private boolean mExportOnlyNotes;
	private String mPath;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Thread t = new Thread(new Runnable() {
			int i = 0;

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
				File outFile;
				FileWriter fstream = null;
				BufferedWriter out = null;
				StringBuilder sb;
				SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
				outFile = new File(mPath, "timestat"
				+ sdf.format(new Date()) + ".csv");
				if(outFile.exists())
					outFile.delete();
				try {
					outFile.createNewFile();
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(), getString(R.string.error_cr_file), Toast.LENGTH_LONG).show();
					e.printStackTrace();
					cursor.close();
					ExportToCSVService.this.stopSelf();
					Intent intent3 = new Intent();
					intent3.setAction(EXPORT);
					getApplicationContext().sendBroadcast(intent3);
				}
				try {
					fstream = new FileWriter(outFile);
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(), getString(R.string.error_open_file), Toast.LENGTH_LONG).show();
					e.printStackTrace();
					cursor.close();
					ExportToCSVService.this.stopSelf();
					Intent intent3 = new Intent();
					intent3.setAction(EXPORT);
					getApplicationContext().sendBroadcast(intent3);
				}
				out = new BufferedWriter(fstream);
				if (cursor.moveToFirst()) {
					do {
						if (ids.contains(cursor.getInt(0))) {
							sb = new StringBuilder();
							sb.append(cursor.getString(3));// count name
							sb.append(";");
							if(mExportOnlyNotes) {
								sb.append(cursor.getString(8));// notes
								sb.append(";");
							} else if(mExportNotes) {
								Cursor c = getContentResolver().query(RecordsDbHelper.CONTENT_URI_NOTES, new String[] { RecordsDbHelper.ID3, RecordsDbHelper.NOTE },RecordsDbHelper.ID3+ "=?" , new String[] { String.valueOf(cursor.getInt(5)) }, null);
								if(c.getCount() == 1) {
									c.moveToFirst();
									sb.append(c.getString(1));// notes
									sb.append(";");
								} else
									sb.append(";"); // notes
								c.close();
							} 			
							sb.append(Long.toString(cursor.getLong(2))); // начала
							sb.append(";"); //
							long end = cursor.getLong(7);
							sb.append(end !=0 ? Long.toString(end) : "-"); // конца
							sb.append(";\n"); //									
							try {
								out.write(sb.toString());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Toast.makeText(getApplicationContext(), getString(R.string.error_write_file), Toast.LENGTH_LONG).show();
								break;
							}
						}
						
						Intent intent = new Intent();
						intent.setAction(EXPORT);
						intent.putExtra("count", cursor.getCount());
						intent.putExtra("progress", i1);
						getApplicationContext().sendBroadcast(intent);
						i1++;
					} while (cursor.moveToNext() && isRunning);
				}
				try {
					out.flush();
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), getString(R.string.error_close_file), Toast.LENGTH_LONG).show();
				}
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), getString(R.string.error_close_file), Toast.LENGTH_LONG).show();
				}
				try {
					fstream.close();
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), getString(R.string.error_close_file), Toast.LENGTH_LONG).show();
				}
				cursor.close();
				ExportToCSVService.this.stopSelf();
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
		mPath = intent.getStringExtra("export_path");
		mExportNotes = intent.getBooleanExtra("export_notes", false);
		mExportOnlyNotes = intent.getBooleanExtra("export_only_notes", false);
		t.start();
		Toast.makeText(ExportToCSVService.this, R.string.one_record_export, Toast.LENGTH_LONG).show();
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
