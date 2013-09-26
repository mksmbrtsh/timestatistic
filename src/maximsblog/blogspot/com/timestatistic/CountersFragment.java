package maximsblog.blogspot.com.timestatistic;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import maximsblog.blogspot.com.timestatistic.AddCounterDialogFragment.AddCounterDialog;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

public final class CountersFragment extends Fragment implements
		LoaderCallbacks<Cursor>, OnItemClickListener, OnClickListener, AddCounterDialog {
	
	private Timer mTimer;
	private long mCurrentTimerId;
	private AddCounterDialogFragment mAddCounterDialogFragment;
	private CountersCursorAdapter mAdapter;
	private LoaderManager loadermanager;
	private ListView mList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadermanager = getLoaderManager();
		
		String[] uiBindFrom = {  RecordsDbHelper.LENGHT, RecordsDbHelper.NAME };
		int[] uiBindTo = {  R.id.current, R.id.name};
		mAddCounterDialogFragment = new AddCounterDialogFragment();
		mAddCounterDialogFragment.setCounterDialogListener(this);
		
		mAdapter = new CountersCursorAdapter(this.getActivity(),
				R.layout.count_row, null, uiBindFrom, uiBindTo, 0);
		
		loadermanager.initLoader(1, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.fragment_counters, container, false);
		mList = (ListView) layout.findViewById(R.id.listView1);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(this);
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
			/*Cursor currentTimer = getActivity().getContentResolver().query(
					RecordsDbHelper.CONTENT_URI_TIMES,
					new String[] { RecordsDbHelper.TIMERSID,
							RecordsDbHelper.LENGHT },
					RecordsDbHelper.LENGHT + " IS NULL", null, null);
			if (currentTimer.getCount() == 1) {
				// launch timer
				currentTimer.moveToFirst();
				mCurrentTimerId = currentTimer.getLong(0);
				mAdapter.setCurrentTimerId(mCurrentTimerId);
				currentTimer.close();
			}*/
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		if(position == 0)
			return;
		Cursor cursor = mAdapter.getCursor();
		boolean isRunning = cursor.getInt(5) == 1;
		int id;
		ContentValues cv;
		long now = new Date().getTime();
		if(isRunning) {
			long start = cursor.getLong(3);
			
			long lenght = now - start;
			id = cursor.getInt(0);
			cv = new ContentValues();
			cv.put(RecordsDbHelper.LENGHT, lenght);
			getActivity().getContentResolver().update(
					RecordsDbHelper.CONTENT_URI_TIMES, cv,
					RecordsDbHelper.ID2+ "=?",
					new String[] { String.valueOf(id) });
		}
		cv = new ContentValues();
		id = cursor.getInt(1);
		cv.put(RecordsDbHelper.TIMERSID, id);
		cv.put(RecordsDbHelper.STARTTIME, now);
		getActivity().getContentResolver().insert(
				RecordsDbHelper.CONTENT_URI_TIMES, cv);
		cv.clear();
		cv.put(RecordsDbHelper.ISRUNNING, 1);
		getActivity().getContentResolver().update(
				RecordsDbHelper.CONTENT_URI_TIMERS, cv, RecordsDbHelper.ID + " = ?", new String[] { String.valueOf(id) });
	}

	@Override
	public void onClick(View v) {
		mAddCounterDialogFragment.show(getFragmentManager(), "dlg1");
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

	@Override
	public void onFinishAddDialog(String inputText) {
		ContentValues cv = new ContentValues();
		cv.put(RecordsDbHelper.NAME, inputText);
		Uri row  = getActivity().getContentResolver().insert(
				RecordsDbHelper.CONTENT_URI_TIMERS, cv);
		int id = Integer.valueOf(row.getLastPathSegment());
		cv.clear();
		cv.put(RecordsDbHelper.TIMERSID, id);
		getActivity().getContentResolver().insert(
				RecordsDbHelper.CONTENT_URI_TIMES, cv);
		loadermanager.restartLoader(1, null, this);
	}
	
	public static CountersFragment newInstance() {
		CountersFragment fragment = new CountersFragment();
		return fragment;
	}
}