package maximsblog.blogspot.com.timestatistic;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import maximsblog.blogspot.com.timestatistic.CountersPeriodSetupDialogFragment.IPeriodSetupDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class ExportToCSVActivity extends SherlockFragmentActivity implements
		IRecordDialog, IPeriodSetupDialog, OnClickListener,
		OnCheckedChangeListener {

	private static final String IDS = "ids";
	private static final String EXPORT_PATH = "export_path";
	private static final String EXPORT_WITH_NOTES = "export_notes";
	private static final String EXPORT_ONLY_NOTES = "export_only_notes";
	private static final int SELECT_PATH = 1;
	private static final String SPLIT_CHAR = "csv_split_char";
	private static final String EXPORT_WITH_HEADER = "export_csv_with_header";
	private static final String DATETIME_FORMAT = "csv_datetime_format";
	private static final String FILENAME = "csv_file_name";
	private static final String INCLUDE_DATETIME_IN_FILENAME = "csv_include_datetime_in_filename";
	private long mSelectStartItem;
	private long mSelectEndItem;
	private int[] mIDs;
	private boolean[] mChecked;
	private String mExportPath;

	private IntentFilter mIntentFilter;
	private Button mExportStart;
	private Button mExportPathSelect;
	private Button mFilterSelect;
	private CheckBox mExportWithDiary;
	private CheckBox mExportOnlyDiary;
	private EditText mSplitChar;
	private String mOldSplitChar;
	private CheckBox mExportWithHeader;
	private EditText mDateTimeFormat;
	private String mOldDateTimeFormat;
	private EditText mFileName;
	private String mOldFileName;
	private CheckBox mFileNameIncludeDateTime;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_export_to_csv);
		TextView t = (TextView) findViewById(R.id.datetimeformat);
		t.setText(getResources().getText(R.string.datetime_format));
		Linkify.addLinks(t, Linkify.ALL);
		t.setMovementMethod(LinkMovementMethod.getInstance());
		mExportPathSelect = (Button) findViewById(R.id.select_folder_btn);
		mExportPathSelect.setOnClickListener(this);
		findViewById(R.id.select_conters_btn).setOnClickListener(this);
		mFilterSelect = (Button) findViewById(R.id.set_filter_btn);
		mFilterSelect.setOnClickListener(this);
		mExportStart = (Button) findViewById(R.id.export_btn);
		mExportStart.setOnClickListener(this);
		mExportOnlyDiary = (CheckBox) findViewById(R.id.export_only_diary);
		mExportOnlyDiary.setOnCheckedChangeListener(this);
		mExportWithDiary = (CheckBox) findViewById(R.id.export_notes);
		mExportWithDiary.setOnCheckedChangeListener(this);
		mExportWithHeader = (CheckBox) findViewById(R.id.header);
		mExportWithHeader.setOnCheckedChangeListener(this);
		mFileNameIncludeDateTime = (CheckBox) findViewById(R.id.include_datetime_in_file_name);
		mFileNameIncludeDateTime.setOnCheckedChangeListener(this);
		mSplitChar = (EditText) findViewById(R.id.split_char);
		mDateTimeFormat = (EditText) findViewById(R.id.datetime_format);
		mFileName = (EditText) findViewById(R.id.filename);
		if (ExportToCSVService.isRunning) {
			mExportStart.setText(getString(R.string.stop));
		} else
			mExportStart.setText(getString(R.string.Export));

		if (savedInstanceState == null) {
			FilterDateOption startDateOption = app.getStartDateExport(this);
			FilterDateOption endDateOption = app.getEndDateExport(this);
			mSelectStartItem = startDateOption.date;
			mSelectEndItem = endDateOption.date;
			setFilterText(startDateOption, endDateOption);
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			String s = prefs.getString(IDS, null);
			mExportPath = prefs.getString(EXPORT_PATH, null);
			mDateTimeFormat.setText(mOldDateTimeFormat = prefs.getString(
					DATETIME_FORMAT, ""));
			mSplitChar
					.setText(mOldSplitChar = prefs.getString(SPLIT_CHAR, ";"));
			mFileName.setText(mOldFileName = prefs
					.getString(FILENAME, "ts.csv"));
			mExportWithDiary.setChecked(prefs.getBoolean(EXPORT_WITH_NOTES,
					false));
			mExportOnlyDiary.setChecked(prefs.getBoolean(EXPORT_ONLY_NOTES,
					false));
			mExportWithHeader.setChecked(prefs.getBoolean(EXPORT_WITH_HEADER,
					false));
			mFileNameIncludeDateTime.setChecked(prefs.getBoolean(
					INCLUDE_DATETIME_IN_FILENAME, true));
			if (s != null) {
				String[] ids = s.split(";");
				mIDs = new int[ids.length];
				mChecked = new boolean[ids.length];
				for (int i = 0; i < ids.length; i++) {
					String[] split = ids[i].split("\\.");
					mIDs[i] = Integer.valueOf(split[0]);
					mChecked[i] = split[1].equals("1");
				}
			} else {
				Cursor newtimers = getContentResolver().query(
						RecordsDbHelper.CONTENT_URI_TIMES, null, null, null,
						RecordsDbHelper.SORTID);
				mIDs = new int[newtimers.getCount()];
				mChecked = new boolean[newtimers.getCount()];
				for (int i1 = 0, cnt1 = newtimers.getCount(); i1 < cnt1; i1++) {
					newtimers.moveToPosition(i1);
					mIDs[i1] = (newtimers.getInt(4));
					mChecked[i1] = true;
				}
				newtimers.close();
			}
		} else {
			FragmentManager fm = getSupportFragmentManager();
			FilterDateSetDialogFragment startDateSetDialogFragment;
			mSelectStartItem = savedInstanceState.getLong("mSelectStartItem");
			mSelectEndItem = savedInstanceState.getLong("mSelectEndItem");
			setFilterText(mSelectStartItem, mSelectEndItem);
			mIDs = savedInstanceState.getIntArray("ids");
			mChecked = savedInstanceState.getBooleanArray("checked");
			mExportPath = savedInstanceState.getString(EXPORT_PATH);
			mExportWithDiary.setChecked(savedInstanceState
					.getBoolean(EXPORT_WITH_NOTES));
			mExportOnlyDiary.setChecked(savedInstanceState
					.getBoolean(EXPORT_ONLY_NOTES));
			mExportWithHeader.setChecked(savedInstanceState
					.getBoolean(EXPORT_WITH_HEADER));
			mOldSplitChar = savedInstanceState.getString(SPLIT_CHAR);
			mOldDateTimeFormat = savedInstanceState.getString(DATETIME_FORMAT);
			mFileNameIncludeDateTime.setChecked(savedInstanceState
					.getBoolean(INCLUDE_DATETIME_IN_FILENAME));
			startDateSetDialogFragment = (FilterDateSetDialogFragment) fm
					.findFragmentByTag("mStartDateSetDialogFragment");
			if (startDateSetDialogFragment != null)
				startDateSetDialogFragment.setDialogListener(this);
			CountersPeriodSetupDialogFragment countersPeriodSetupDialogFragment = (CountersPeriodSetupDialogFragment) fm
					.findFragmentByTag("countersPeriodSetupDialogFragment");
			if (countersPeriodSetupDialogFragment != null)
				countersPeriodSetupDialogFragment.setPeriodSetupDialog(this);
		}
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(ExportToCSVService.EXPORT);
		if (mExportPath == null) {
			mExportStart.setEnabled(false);
			mExportPathSelect.setText(getString(R.string.location));
		} else {
			mExportPathSelect.setText(getString(R.string.select_path) + ":\n"
					+ mExportPath);
		}
		mExportWithDiary.setEnabled(!mExportOnlyDiary.isChecked());
	}

	private void setFilterText(FilterDateOption startDateOption,
			FilterDateOption endDateOption) {
		String s1, s2;
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(
				"dd/MM/yy HH:mm");
		if (!startDateOption.dateName.equals(getResources().getStringArray(
				R.array.StartFilters)[6]))
			s1 = startDateOption.dateName;
		else
			s1 = mSimpleDateFormat.format(new Date(startDateOption.date));
		if (!endDateOption.dateName.equals(getResources().getStringArray(
				R.array.EndFilters)[6]))
			s2 = endDateOption.dateName;
		else
			s2 = mSimpleDateFormat.format(new Date(endDateOption.date));
		mFilterSelect.setText(getString(R.string.filterdateset) + ":\n" + s1
				+ " - " + s2);
	}

	private void setFilterText(long startDateOption, long endDateOption) {
		String s1, s2;
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(
				"dd/MM/yy HH:mm");

		s1 = mSimpleDateFormat.format(new Date(startDateOption));

		s2 = mSimpleDateFormat.format(new Date(endDateOption));
		mFilterSelect.setText(getString(R.string.filterdateset) + ":\n" + s1
				+ " - " + s2);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mIntentReceiver, mIntentFilter);
	};

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mIntentReceiver);
	};

	@Override
	protected void onDestroy() {
		String newSplitChar = mSplitChar.getText().toString();
		String newDatetimeFormat = mDateTimeFormat.getText().toString();
		String newFilename = mFileName.getText().toString();
		if (!newSplitChar.equals(mOldSplitChar)) {
			mOldSplitChar = newSplitChar;
			Editor editor = PreferenceManager.getDefaultSharedPreferences(this)
					.edit();
			editor.putString(SPLIT_CHAR, mOldSplitChar);
			editor.commit();
		}
		if (!newDatetimeFormat.equals(mOldDateTimeFormat)) {
			mOldDateTimeFormat = newDatetimeFormat;
			Editor editor = PreferenceManager.getDefaultSharedPreferences(this)
					.edit();
			editor.putString(DATETIME_FORMAT, mOldDateTimeFormat);
			editor.commit();
		}
		if (!newFilename.equals(mOldFileName)) {
			mOldFileName = newFilename;
			Editor editor = PreferenceManager.getDefaultSharedPreferences(this)
					.edit();
			editor.putString(FILENAME, mOldFileName);
			editor.commit();
		}
		super.onDestroy();
	};

	@Override
	public void onRefreshFragmentsValue() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDiaryFragmentsRefresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFilterDateSet(long start, long stop) {
		String setting = SettingsActivity.STARTTIMEFILTEREXPORTCSV;
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this)
				.edit();
		editor.putLong(setting, start);
		setting = SettingsActivity.ENDTIMEFILTEREXPORTCSV;
		editor.putLong(setting, stop);
		editor.commit();
		FilterDateOption startDateOption = app.getStartDateExport(this);
		mSelectStartItem = startDateOption.date;
		FilterDateOption endDateOption = app.getEndDateExport(this);
		mSelectEndItem = endDateOption.date;
		setFilterText(startDateOption, endDateOption);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("mSelectStartItem", mSelectStartItem);
		outState.putLong("mSelectEndItem", mSelectEndItem);
		outState.putString(EXPORT_PATH, mExportPath);
		outState.putBooleanArray("checked", mChecked);
		outState.putIntArray("ids", mIDs);
		outState.putBoolean(EXPORT_WITH_NOTES, mExportWithDiary.isChecked());
		outState.putBoolean(EXPORT_ONLY_NOTES, mExportOnlyDiary.isChecked());
		outState.putBoolean(EXPORT_WITH_HEADER, mExportWithHeader.isChecked());
		outState.putString(SPLIT_CHAR, mOldSplitChar);
		outState.putString(DATETIME_FORMAT, mOldDateTimeFormat);
		outState.putString(FILENAME, mOldFileName);
		outState.putBoolean(INCLUDE_DATETIME_IN_FILENAME,
				mFileNameIncludeDateTime.isChecked());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void setupCounters(int[] ids, boolean[] checked) {
		mIDs = ids;
		mChecked = checked;
		Editor edit = PreferenceManager.getDefaultSharedPreferences(this)
				.edit();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ids.length; i++) {
			sb.append(mIDs[i]);
			sb.append('.');
			if (mChecked[i])
				sb.append('1');
			else
				sb.append('0');
			sb.append(';');
		}
		sb.deleteCharAt(sb.length() - 1);
		edit.putString(IDS, sb.toString());
		edit.commit();
	}

	@Override
	public void onClick(View v) {
		FilterDateSetDialogFragment startDateSetDialogFragment;
		Bundle args = new Bundle();
		switch (v.getId()) {
		case R.id.select_folder_btn: {
			Intent intent = new Intent(this, FileDialog.class);
			intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
			intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
			startActivityForResult(intent, SELECT_PATH);
		}
			break;
		case R.id.select_conters_btn:
			CountersPeriodSetupDialogFragment countersPeriodSetupDialogFragment = new CountersPeriodSetupDialogFragment();
			countersPeriodSetupDialogFragment.setPeriodSetupDialog(this);
			args.putBooleanArray("checked", mChecked);
			args.putIntArray("ids", mIDs);
			countersPeriodSetupDialogFragment.setArguments(args);
			countersPeriodSetupDialogFragment.show(
					this.getSupportFragmentManager(),
					"countersPeriodSetupDialogFragment");
			break;
		case R.id.set_filter_btn:
			startDateSetDialogFragment = new FilterDateSetDialogFragment();
			long start = PreferenceManager.getDefaultSharedPreferences(this)
					.getLong(SettingsActivity.STARTTIMEFILTEREXPORTCSV, 5);
			long end = PreferenceManager.getDefaultSharedPreferences(this)
					.getLong(SettingsActivity.ENDTIMEFILTEREXPORTCSV, 5);
			args.putLong("start", start);
			args.putLong("stop", end);
			startDateSetDialogFragment.setArguments(args);
			startDateSetDialogFragment.setDialogListener(this);
			startDateSetDialogFragment.show(this.getSupportFragmentManager(),
					"mStartDateSetDialogFragment");
			break;
		case R.id.export_btn:
			Intent intent = new Intent(this, ExportToCSVService.class);
			if (!ExportToCSVService.isRunning) {
				intent.putExtra("start", mSelectStartItem);
				intent.putExtra("stop", mSelectEndItem);
				intent.putExtra("checked", mChecked);
				intent.putExtra("ids", mIDs);
				intent.putExtra("export_path", mExportPath);
				intent.putExtra("export_notes", mExportWithDiary.isChecked());
				intent.putExtra("export_only_notes",
						mExportOnlyDiary.isChecked());
				intent.putExtra("export_with_header",
						mExportWithHeader.isChecked());

				intent.putExtra("split_char", mSplitChar.getText().toString());
				intent.putExtra("datetime_format", mDateTimeFormat.getText()
						.toString());
				intent.putExtra("filename", mFileName.getText().toString());
				intent.putExtra("filename_include_datetime",
						mFileNameIncludeDateTime.isChecked());
				getApplicationContext().startService(intent);
				mExportStart.setText("остановить");
			} else {
				getApplicationContext().stopService(intent);
				mExportStart.setText(getString(R.string.Export));
			}
			break;
		default:
			break;
		}

	}

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ExportToCSVService.EXPORT)) {
				if (ExportToCSVService.isRunning) {
					mExportStart.setText(getString(R.string.stop)
							+ intent.getIntExtra("progress", 0) + "/"
							+ intent.getIntExtra("count", 0));

				} else {
					mExportStart.setText(getString(R.string.Export));
					Intent newintent = new Intent();
					newintent.setAction(Intent.ACTION_VIEW);
					File f = new File(mExportPath,
							ExportToCSVService.getCSVFileName(mFileName
									.getText().toString(),
									getString(R.string.now),
									mFileNameIncludeDateTime.isChecked(),
									mSelectStartItem, mSelectEndItem));
					if (f.exists()) {
						MimeTypeMap myMime = MimeTypeMap.getSingleton();
						String mimeType = myMime
								.getMimeTypeFromExtension(fileExt(f.toString())
										.substring(1));
						newintent.setDataAndType(Uri.fromFile(f), mimeType);
						newintent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
						try {
							startActivity(newintent);
						} catch (android.content.ActivityNotFoundException e) {
							int i=0;
							i++;
							
						}
					}
				}
			}
		}
	};

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.export_only_diary) {
			Editor edit = PreferenceManager.getDefaultSharedPreferences(this)
					.edit();
			edit.putBoolean(EXPORT_ONLY_NOTES, isChecked);
			edit.commit();
			mExportWithDiary.setEnabled(!mExportOnlyDiary.isChecked());
		} else if (buttonView.getId() == R.id.header) {
			Editor edit = PreferenceManager.getDefaultSharedPreferences(this)
					.edit();
			edit.putBoolean(EXPORT_WITH_HEADER, isChecked);
			edit.commit();
		} else if (buttonView.getId() == R.id.include_datetime_in_file_name) {
			Editor edit = PreferenceManager.getDefaultSharedPreferences(this)
					.edit();
			edit.putBoolean(INCLUDE_DATETIME_IN_FILENAME, isChecked);
			edit.commit();
		} else {
			Editor edit = PreferenceManager.getDefaultSharedPreferences(this)
					.edit();
			edit.putBoolean(EXPORT_WITH_NOTES, isChecked);
			edit.commit();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null && requestCode == SELECT_PATH
				&& resultCode == RESULT_OK) {
			mExportPath = data.getStringExtra(FileDialog.RESULT_PATH);
			Editor edit = PreferenceManager.getDefaultSharedPreferences(this)
					.edit();
			edit.putString(EXPORT_PATH, mExportPath);
			edit.commit();
			mExportStart.setEnabled(true);
			mExportPathSelect.setText(getString(R.string.select_path) + ":\n"
					+ mExportPath);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private String fileExt(String url) {
		if (url.indexOf("?") > -1) {
			url = url.substring(0, url.indexOf("?"));
		}
		if (url.lastIndexOf(".") == -1) {
			return null;
		} else {
			String ext = url.substring(url.lastIndexOf("."));
			if (ext.indexOf("%") > -1) {
				ext = ext.substring(0, ext.indexOf("%"));
			}
			if (ext.indexOf("/") > -1) {
				ext = ext.substring(0, ext.indexOf("/"));
			}
			return ext.toLowerCase();

		}
	}
}
