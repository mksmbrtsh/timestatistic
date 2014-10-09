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
import com.google.android.gms.internal.md;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
	private String mSplitChar;
	private boolean mExportWithHeader;
	private String mDateTimeFormat;
	private String mFileName;
	private boolean mIncludeDateTime;
	final Handler mHandler = new Handler();
	private IntentFilter mIntentFilter;

	@Override
	public void onCreate() {
		super.onCreate();
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(ExportToCSVService.EXPORT);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				String[] selectionArgs;
				Cursor cursor;
				if (mExportOnlyNotes) {
					selectionArgs = new String[] { "",
							String.valueOf(mSelectStartItem),
							String.valueOf(mSelectEndItem) };
					cursor = getContentResolver().query(
							RecordsDbHelper.CONTENT_URI_ALLNOTES,
							null,
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
				SimpleDateFormat sdf;
				try {
					if (mDateTimeFormat.length() == 0) {
						sdf = new SimpleDateFormat("yyyyMMdd");
					} else {
						sdf = new SimpleDateFormat(mDateTimeFormat);
					}
				} catch (IllegalArgumentException e) {
					Toast.makeText(getApplicationContext(),
							getString(R.string.error_dateformat),
							Toast.LENGTH_LONG).show();
					e.printStackTrace();
					cursor.close();
					ExportToCSVService.this.stopSelf();
					Intent intent3 = new Intent();
					intent3.setAction(EXPORT);
					getApplicationContext().sendBroadcast(intent3);
					return;
				}

				outFile = new File(mPath, getCSVFileName(mFileName,
						getString(R.string.now), mIncludeDateTime,
						mSelectStartItem, mSelectEndItem));
				if (outFile.exists())
					outFile.delete();
				try {
					outFile.createNewFile();
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(),
							getString(R.string.error_cr_file),
							Toast.LENGTH_LONG).show();
					e.printStackTrace();
					cursor.close();
					ExportToCSVService.this.stopSelf();
					Intent intent3 = new Intent();
					intent3.setAction(EXPORT);
					getApplicationContext().sendBroadcast(intent3);
					return;
				}
				try {
					fstream = new FileWriter(outFile);
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(),
							getString(R.string.error_open_file),
							Toast.LENGTH_LONG).show();
					e.printStackTrace();
					cursor.close();
					ExportToCSVService.this.stopSelf();
					Intent intent3 = new Intent();
					intent3.setAction(EXPORT);
					getApplicationContext().sendBroadcast(intent3);
					return;
				}
				out = new BufferedWriter(fstream);
				if (mExportWithHeader) {
					sb = new StringBuilder();
					sb.append("name");// count name
					sb.append(mSplitChar);
					if (mExportOnlyNotes) {
						sb.append("notes");// notes
						sb.append(mSplitChar);
					} else if (mExportNotes) {
						sb.append("notes");// notes
						sb.append(mSplitChar);
					}
					sb.append("start"); // начала
					sb.append(mSplitChar); //
					sb.append("stop"); // конца
					sb.append("\n"); //
					try {
						out.write(sb.toString());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast.makeText(getApplicationContext(),
								getString(R.string.error_write_file),
								Toast.LENGTH_LONG).show();
						cursor.close();
						ExportToCSVService.this.stopSelf();
						Intent intent3 = new Intent();
						intent3.setAction(EXPORT);
						getApplicationContext().sendBroadcast(intent3);
						return;
					}
				}
				if (cursor.moveToFirst()) {
						if (ids.contains(cursor.getInt(0))) {
							sb = new StringBuilder();
							sb.append(replaceSpecSymbols(cursor.getString(3)));// count
																				// name
							sb.append(mSplitChar);
							if (mExportOnlyNotes) {
								sb.append(replaceSpecSymbols(cursor
										.getString(8)));// notes
								sb.append(mSplitChar);
							} else if (mExportNotes) {
								Cursor c = getContentResolver().query(
										RecordsDbHelper.CONTENT_URI_NOTES,
										new String[] { RecordsDbHelper.ID3,
												RecordsDbHelper.NOTE },
										RecordsDbHelper.ID3 + "=?",
										new String[] { String.valueOf(cursor
												.getInt(5)) }, null);
								if (c.getCount() == 1) {
									c.moveToFirst();
									sb.append(replaceSpecSymbols(c.getString(1)));// notes
									sb.append(mSplitChar);
								} else
									sb.append(mSplitChar); // notes
								c.close();
							}
							if (mDateTimeFormat.length() == 0)
								sb.append(Long.toString(cursor.getLong(2))); // начала
							else {
								sb.append(replaceSpecSymbols(sdf
										.format(new Date(cursor.getLong(2)))));
							}
							sb.append(mSplitChar); //
							long end = cursor.getLong(7);
							if (mDateTimeFormat.length() == 0)
								sb.append(end != 0 ? Long.toString(end)
										: getString(R.string.now)); // конца
							else {
								sb.append(end != 0 ? replaceSpecSymbols(sdf
										.format(new Date(end))) : "-"); // конца
							}
							sb.append("\n"); //
							try {
								out.write(sb.toString());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Message msg = new Message();
								msg.what = 0;
								msg.obj = getString(R.string.error_write_file);
								handler.sendMessage(msg);
							}
						}

						Intent intent = new Intent();
						intent.setAction(EXPORT);
						intent.putExtra("count", cursor.getCount());
						intent.putExtra("progress", 1);
						getApplicationContext().sendBroadcast(intent);
				}
				try {
					out.flush();
				} catch (IOException e) {
					e.printStackTrace();
					cursor.close();
					ExportToCSVService.this.stopSelf();
					Intent intent3 = new Intent();
					intent3.setAction(EXPORT);
					intent3.putExtra("msg",
							getString(R.string.error_close_file));
					getApplicationContext().sendBroadcast(intent3);
					Message msg = new Message();
					msg.what = 0;
					msg.obj = getString(R.string.error_close_file);
					handler.sendMessage(msg);
					return;
				}
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
					cursor.close();
					ExportToCSVService.this.stopSelf();
					Intent intent3 = new Intent();
					intent3.setAction(EXPORT);
					getApplicationContext().sendBroadcast(intent3);
					Message msg = new Message();
					msg.what =0;
					msg.obj = getString(R.string.error_close_file);
					handler.sendMessage(msg);
					return;
				}
				try {
					fstream.close();
				} catch (IOException e) {
					e.printStackTrace();
					cursor.close();
					ExportToCSVService.this.stopSelf();
					Intent intent3 = new Intent();
					intent3.setAction(EXPORT);						
					getApplicationContext().sendBroadcast(intent3);
					Message msg = new Message();
					msg.what = 0;
					msg.obj = getString(R.string.error_close_file);
					handler.sendMessage(msg);
					return;
				}
				cursor.close();
				ExportToCSVService.this.stopSelf();
				Intent intent3 = new Intent();
				intent3.setAction(EXPORT);
				Message msg = new Message();
				msg.what =0;
				msg.obj = getString(R.string.export_to_csv_complete);
				handler.sendMessage(msg);
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
		mSplitChar = intent.getStringExtra("split_char");
		mDateTimeFormat = intent.getStringExtra("datetime_format");
		mExportWithHeader = intent.getBooleanExtra("export_with_header", false);
		mIncludeDateTime = intent.getBooleanExtra("filename_include_datetime",
				true);
		mFileName = intent.getStringExtra("filename");
		if (mSplitChar.length() == 0)
			mSplitChar = ";";
		t.start();
		return super.onStartCommand(intent, flags, startId);
	};

	private String replaceSpecSymbols(String s) {
		return s.replaceAll("\"", "\"\"")
				.replaceAll(mSplitChar, "\"" + mSplitChar + "\"")
				.replaceAll("\n", "\"\n\"");
	}

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

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				String m = (String) msg.obj;
				if (m != null) {
					Toast.makeText(getApplicationContext(), m,
							Toast.LENGTH_LONG).show();
				}
			}

		}
	};

	public static String getCSVFileName(String fileName, String now,
			boolean includeDateTime, long selectStartItem, long selectEndItem) {
		if (!fileName.contains(".")) {
			if (includeDateTime) {
				SimpleDateFormat filesdf = new SimpleDateFormat("yyyyMMdd");
				fileName += filesdf.format(new Date(selectStartItem))
						+ "-"
						+ ((selectEndItem != -1) ? filesdf.format(new Date(
								selectEndItem)) : now) + ".csv";
			} else
				fileName += ".csv";
		} else {
			if (includeDateTime) {
				SimpleDateFormat filesdf = new SimpleDateFormat("yyyyMMdd");
				int index = fileName.lastIndexOf('.');
				String n = fileName.substring(0, index);
				String r = fileName.substring(index + 1, fileName.length() - 1);
				fileName = n
						+ filesdf.format(new Date(selectStartItem))
						+ "-"
						+ ((selectEndItem != -1) ? filesdf.format(new Date(
								selectEndItem)) : now) + "." + r;
			}
		}
		return fileName;
	}

}
