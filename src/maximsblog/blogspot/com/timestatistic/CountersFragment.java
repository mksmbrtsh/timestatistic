package maximsblog.blogspot.com.timestatistic;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import maximsblog.blogspot.com.timestatistic.MainActivity.MainFragments;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

public final class CountersFragment extends Fragment implements
		LoaderCallbacks<Cursor>, OnItemClickListener, MainFragments,
		OnItemLongClickListener {

	private Timer mTimer;
	private CountersCursorAdapter mAdapter;
	private LoaderManager loadermanager;
	private GridView mList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadermanager = getLoaderManager();
		String[] uiBindFrom = { RecordsDbHelper.LENGHT, RecordsDbHelper.NAME };
		int[] uiBindTo = { R.id.current, R.id.name };

		mAdapter = new CountersCursorAdapter(this.getActivity(),
				R.layout.count_row, null, uiBindFrom, uiBindTo, 0);
		loadermanager.initLoader(1, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.fragment_counters, container, false);
		mList = (GridView) layout.findViewById(R.id.listView1);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(this);
		mList.setOnItemLongClickListener(this);

		return layout;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader loader = new CursorLoader(this.getActivity(),
				RecordsDbHelper.CONTENT_URI_TIMES, null, null, null, null);
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
		long lenght = now - start;
		cv = new ContentValues();
		cv.put(RecordsDbHelper.LENGHT, lenght);
		getActivity().getContentResolver().update(
				RecordsDbHelper.CONTENT_URI_TIMES, cv,
				RecordsDbHelper.ID2 + "=?",
				new String[] { String.valueOf(timeId) });
		cv.clear();
		Cursor cursor = mAdapter.getCursor();
		boolean isRunning = cursor.getInt(6) == 1;
		int counterId;
		if (isRunning) {
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
		loadermanager.restartLoader(1, null, this);
		SettingsActivity.visibleNotif(getActivity(), cursor.getLong(3),
				cursor.getLong(2), cursor.getString(5));
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
		loadermanager.restartLoader(1, null, this);
	}

	
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		Cursor cursor = mAdapter.getCursor();
		int id = cursor.getInt(1);
		String name = cursor.getString(5);
		boolean isRunning = cursor.getInt(6) == 1;
		int color = cursor.getInt(7);
		CounterEditorDialogFragment addCounterDialogFragment = new CounterEditorDialogFragment();
		addCounterDialogFragment
				.setCounterDialogListener((MainActivity) getActivity());
		addCounterDialogFragment.setIdCounter(id);
		addCounterDialogFragment.setName(name);
		addCounterDialogFragment.setColor(color);
		addCounterDialogFragment.setIsRunning(isRunning);
		addCounterDialogFragment.show(this.getActivity()
				.getSupportFragmentManager(), "mAddCounterDialogFragment");
		return true;
	}
}