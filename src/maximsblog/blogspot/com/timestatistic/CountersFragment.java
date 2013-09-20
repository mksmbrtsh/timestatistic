package maximsblog.blogspot.com.timestatistic;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import maximsblog.blogspot.com.timestatistic.AddCounterDialogFragment.AddCounterDialog;

import android.content.ContentValues;
import android.database.Cursor;
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
		
		String[] uiBindFrom = { RecordsDbHelper.NAME };
		int[] uiBindTo = { R.id.name };
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
				RecordsDbHelper.CONTENT_URI_TIMERS, null, null, null, null);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (mAdapter != null && cursor != null) {
			mAdapter.swapCursor(cursor); // swap the new cursor in.
			Cursor currentTimer = getActivity().getContentResolver().query(
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
			}
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

		long current_id = mAdapter.getCurrentTimerId();
		Cursor timeCursor = getActivity().getContentResolver().query(
				RecordsDbHelper.CONTENT_URI_TIMES,
				new String[] { RecordsDbHelper.ID, RecordsDbHelper.STARTTIME,
						RecordsDbHelper.LENGHT },
				RecordsDbHelper.TIMERSID + "=? AND " + RecordsDbHelper.LENGHT
						+ " IS NULL",
				new String[] { String.valueOf(current_id) }, null);
		ContentValues cv;
		int id;
		long now = new Date().getTime();
		boolean timerIsWorking = timeCursor.getCount() == 1;
		if(timerIsWorking) {
			timeCursor.moveToFirst();
			long start = timeCursor.getLong(1);
			long lenght = now - start;
			id = timeCursor.getInt(0);
			cv = new ContentValues();
			cv.put(RecordsDbHelper.LENGHT, lenght);
			getActivity().getContentResolver().update(
					RecordsDbHelper.CONTENT_URI_TIMES, cv,
					RecordsDbHelper.ID + "=?",
					new String[] { String.valueOf(id) });
		}
		timeCursor.close();
		cv = new ContentValues();
		Cursor c = mAdapter.getCursor();
		c.moveToPosition(position);
		id = c.getInt(0);
		if (id == current_id && timerIsWorking) {
			id = 1;
			mAdapter.setCurrentTimerId(1);
		}
		cv.put(RecordsDbHelper.TIMERSID, id);
		cv.put(RecordsDbHelper.STARTTIME, now);
		getActivity().getContentResolver().insert(
				RecordsDbHelper.CONTENT_URI_TIMES, cv);

		mAdapter.setCurrentTimerId(id);
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
		getActivity().getContentResolver().insert(
				RecordsDbHelper.CONTENT_URI_TIMERS, cv);
		mAdapter.notifyDataSetChanged();
	}
	
	public static CountersFragment newInstance() {
		CountersFragment fragment = new CountersFragment();
		return fragment;
	}
}