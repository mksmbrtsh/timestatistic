package maximsblog.blogspot.com.timestatistic;

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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_export_to_csv);
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
		if (ExportToCSVService.isRunning) {
			mExportStart.setText("остановить");
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
			mExportWithDiary.setChecked(prefs.getBoolean(EXPORT_WITH_NOTES,
					false));
			mExportOnlyDiary.setChecked(prefs.getBoolean(EXPORT_ONLY_NOTES,
					false));
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
			mExportPathSelect.setText(getString(R.string.select_path)
					+ ":\n" + mExportPath);
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
	public void onRefreshFragmentsValue() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDiaryFragmentsRefresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFilterDateSet(long start, long stop) {
		String setting = SettingsActivity.STARTTIMEFILTEREXPORT;
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this)
				.edit();
		editor.putLong(setting, start);
		setting = SettingsActivity.ENDTIMEFILTEREXPORT;
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
					.getLong(SettingsActivity.STARTTIMEFILTEREXPORT, 5);
			long end = PreferenceManager.getDefaultSharedPreferences(this)
					.getLong(SettingsActivity.ENDTIMEFILTEREXPORT, 5);
			args.putLong("start", start);
			args.putLong("stop", end);
			startDateSetDialogFragment.setArguments(args);
			startDateSetDialogFragment.setDialogListener(this);
			startDateSetDialogFragment.show(this.getSupportFragmentManager(),
					"mStartDateSetDialogFragment");
			break;
		case R.id.export_btn:
			Intent intent = new Intent(this,
					ExportToCSVService.class);
			if (!ExportToCSVService.isRunning) {
				intent.putExtra("start", mSelectStartItem);
				intent.putExtra("stop", mSelectEndItem);
				intent.putExtra("checked", mChecked);
				intent.putExtra("ids", mIDs);
				intent.putExtra("export_path", mExportPath);
				intent.putExtra("export_notes", mExportWithDiary.isChecked());
				intent.putExtra("export_only_notes",
						mExportOnlyDiary.isChecked());
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
					mExportStart.setText("остановить"
							+ intent.getIntExtra("progress", 0) + "/"
							+ intent.getIntExtra("count", 0));

				} else
					mExportStart
							.setText(getString(R.string.export_to_gcalendar));
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
		} else {
			Editor edit = PreferenceManager.getDefaultSharedPreferences(this)
					.edit();
			edit.putBoolean(EXPORT_WITH_NOTES, isChecked);
			edit.commit();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null && requestCode == SELECT_PATH && resultCode == RESULT_OK) {
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
}
