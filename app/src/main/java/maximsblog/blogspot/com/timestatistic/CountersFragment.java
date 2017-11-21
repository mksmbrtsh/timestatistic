package maximsblog.blogspot.com.timestatistic;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import maximsblog.blogspot.com.timestatistic.MainActivity.MainFragments;

public final class CountersFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener, MainFragments,
		OnItemLongClickListener {

	private final int LOADER_ID = 1;
	private Timer mTimer;
	private CountersCursorAdapter mAdapter;
	private LoaderManager loadermanager;
	private GridView mList;
	private long mStartdate;
	private long mEnddate;
	private View mCurrentPanel;
	private TextView mCurrent;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadermanager = getLoaderManager();
		String[] uiBindFrom = { RecordsDbHelper.LENGHT, RecordsDbHelper.NAME };
		int[] uiBindTo = { R.id.current, R.id.name };
		setActivityTitle();
		mAdapter = new CountersCursorAdapter(this.getActivity(),
				R.layout.count_row, null, uiBindFrom, uiBindTo, 0, mStartdate, mEnddate);
		loadermanager.initLoader(LOADER_ID, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		RelativeLayout layout = (RelativeLayout) inflater.inflate(
				R.layout.fragment_counters, container, false);
		mList = (GridView) layout.findViewById(R.id.listView1);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(this);
		mList.setOnItemLongClickListener(this);

		mCurrentPanel = layout.findViewById(R.id.current_panel);
		mCurrent = (TextView)layout.findViewById(R.id.current);
		
		return layout;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		
		String[] selectionArgs = new String[] { String.valueOf(mStartdate), String.valueOf(mEnddate)};
		CursorLoader loader = new CursorLoader(this.getActivity(),
				RecordsDbHelper.CONTENT_URI_TIMES, null, null, selectionArgs, null);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (mAdapter != null && cursor != null) {
			mAdapter.swapCursor(cursor); // swap the new cursor in.
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// running counter
		ContentValues cv = new ContentValues();
		long now = new Date().getTime();
		Cursor c = getActivity().getContentResolver().query(
				RecordsDbHelper.CONTENT_URI_TIMES,
				new String[] { RecordsDbHelper.ID, RecordsDbHelper.NAME,
						RecordsDbHelper.ISRUNNING, RecordsDbHelper.STARTTIME,
						RecordsDbHelper.TIMERSID },
				RecordsDbHelper.ISRUNNING + "=?",
				new String[] { String.valueOf(1) }, null);
		c.moveToFirst();
		int timeId = c.getInt(0);
		long start = c.getLong(3);
		c.close();
		// set value to running counter
		long lenght = now - start;
		cv = new ContentValues();
		cv.put(RecordsDbHelper.LENGHT, lenght);
		cv.put(RecordsDbHelper.ENDTIME, start + lenght);
		getActivity().getContentResolver().update(
				RecordsDbHelper.CONTENT_URI_TIMES, cv,
				RecordsDbHelper.ID2 + "=?",
				new String[] { String.valueOf(timeId) });
		cv.clear();
		Cursor cursor = mAdapter.getCursor();
		boolean isRunning = cursor.getInt(6) == 1;
		int counterId;
		if (isRunning) {
			// if click to running counter, then switch to idle-counter
			counterId = 1;
		} else {
			counterId = cursor.getInt(4);
		}
		cv.put(RecordsDbHelper.TIMERSID, counterId);
		cv.put(RecordsDbHelper.STARTTIME, now);
		getActivity().getContentResolver().insert(
				RecordsDbHelper.CONTENT_URI_TIMES, cv);
		cv.clear();
		cv.put(RecordsDbHelper.ISRUNNING, 1);
		getActivity().getContentResolver().update(
				RecordsDbHelper.CONTENT_URI_TIMERS, cv,
				RecordsDbHelper.ID + " = ?",
				new String[] { String.valueOf(counterId) });
		app.loadRunningCounterAlarm(getActivity().getApplicationContext());
		app.setStatusBar(getActivity().getApplicationContext());
		loadermanager.restartLoader(LOADER_ID, null, this);
		TimeRecordsFragment timeRecordsFragment = (TimeRecordsFragment) ((MainActivity) getActivity())
				.findFragmentByPosition(1);
		timeRecordsFragment.setNormalMode();
		((Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE))
		.vibrate(100);
		app.updateDayCountAppWidget(getActivity());
	}

	public final void timerAlert() {

		mTimer = new Timer();
		TimerTask tt = new TimerTask() {

			@Override
			public void run() {
				Runnable update = new Runnable() {

					@Override
					public void run() {

						int y = mList.getScrollY();
						mList.invalidateViews();
						mList.scrollBy(0, y);
						if(mEnddate == -1)
							mCurrentPanel.setVisibility(View.GONE);
						else {
							mCurrentPanel.setVisibility(View.VISIBLE);
							long time = mEnddate - new Date().getTime();
							if(time < 0)
								time = 0;
							mAdapter.setTime(mCurrent, time);
						}
					}
				};
				getActivity().runOnUiThread(update);
			}

		};
		mTimer.scheduleAtFixedRate(tt, 1000, 1000);

	}

	@Override
	public void onResume() {
		timerAlert();
		super.onResume();
	};

	@Override
	public void onPause() {
		mTimer.cancel();
		mTimer.purge();
		super.onPause();
	}

	public static CountersFragment newInstance() {
		CountersFragment fragment = new CountersFragment();
		return fragment;
	}

	@Override
	public void onReload() {
		setActivityTitle();
		mAdapter.setDate(mStartdate, mEnddate);		
		loadermanager.restartLoader(LOADER_ID, null, this);
	}

	private void setActivityTitle() {
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(
				"dd/MM/yy HH:mm");
		FilterDateOption startDateOption = app.getStartDate(getActivity());
		mStartdate = startDateOption.date;
		if(!startDateOption.dateName.equals(getResources().getStringArray(R.array.StartFilters)[6]))
			((Activity)getActivity()).getActionBar().setTitle(startDateOption.dateName);
		else {
			((Activity)getActivity()).getActionBar().setTitle(startDateOption.dateName + " " + mSimpleDateFormat.format(new Date(mStartdate)));
		}
		startDateOption = app.getEndDate(getActivity());
		mEnddate = startDateOption.date;
		if(!startDateOption.dateName.equals(getResources().getStringArray(R.array.EndFilters)[6]))
			((Activity)getActivity()).getActionBar().setSubtitle(startDateOption.dateName);
		else {
			((Activity)getActivity()).getActionBar().setSubtitle(startDateOption.dateName + " " + mSimpleDateFormat.format(new Date(mEnddate)));
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		Cursor cursor = mAdapter.getCursor();
		int id = cursor.getInt(1);
		String name = cursor.getString(5);
		boolean isRunning = cursor.getInt(6) == 1;
		int color = cursor.getInt(7);
		long interval = cursor.getLong(8);
		int sort = cursor.getInt(9);
		CounterEditorDialogFragment counterEditorDialogFragment = new CounterEditorDialogFragment();
		counterEditorDialogFragment
				.setCounterDialogListener((MainActivity) getActivity());
		counterEditorDialogFragment.setIdCounter(id);
		counterEditorDialogFragment.setName(name);
		counterEditorDialogFragment.setColor(color);
		counterEditorDialogFragment.setInterval(interval);
		counterEditorDialogFragment.setIsRunning(isRunning);
		counterEditorDialogFragment.setSortId(sort);
		counterEditorDialogFragment.show(this.getActivity().getFragmentManager(),"mCounterEditorDialogFragment");
		return true;
	}
}