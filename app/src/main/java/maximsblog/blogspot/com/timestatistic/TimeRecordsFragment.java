package maximsblog.blogspot.com.timestatistic;

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import maximsblog.blogspot.com.timestatistic.MainActivity.MainFragments;

public class TimeRecordsFragment extends Fragment implements
		LoaderCallbacks<Cursor>, MainFragments, OnItemClickListener,
		OnItemLongClickListener, OnClickListener {
	
	private final int LOADER_ID = 2;
	
	public static TimeRecordsFragment newInstance() {

		return new TimeRecordsFragment();
	}

	private LoaderManager loadermanager;
	private ListView mList;
	private TimesCursorAdapter mAdapter;
	private View mUnionPanel;
	private long mStartdate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadermanager = getLoaderManager();
		String[] uiBindFrom = { RecordsDbHelper.NAME,
				RecordsDbHelper.STARTTIME, RecordsDbHelper.LENGHT };
		int[] uiBindTo = { R.id.name, R.id.time, R.id.lenght_record };
		mStartdate = app.getStartDate(getActivity()).date;
		mAdapter = new TimesCursorAdapter(this.getActivity(),
				R.layout.time_row, null, uiBindFrom, uiBindTo, 0, mStartdate);
		loadermanager.initLoader(LOADER_ID, null, this);
		if (savedInstanceState != null) {
			mAdapter.setSelectedPosition(savedInstanceState.getInt("mChoiceUnionMode"));
			mAdapter.setSelected((HashMap<Integer, Boolean>) savedInstanceState
					.getSerializable("mSelected"));
		} else {
			mAdapter.setSelected(new HashMap<Integer, Boolean>());
			mAdapter.setSelectedPosition(TimesCursorAdapter.NORMAL_MODE);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("mChoiceUnionMode", mAdapter.getChoiceUnionMode());
		outState.putSerializable("mSelected", mAdapter.getSelected());
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.fragment_time_records, container, false);
		mList = (ListView) layout.findViewById(R.id.listView1);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(this);
		mList.setOnItemLongClickListener(this);
		mList.setOnScrollListener(mAdapter);
		mList.setEmptyView(layout.findViewById(R.id.empty_records));
		mUnionPanel = layout.findViewById(R.id.union_panel);
		mUnionPanel.setVisibility(View.GONE);
		Button mUnionButton = (Button) mUnionPanel.findViewById(R.id.ok);
		Button mCancelUnionButton = (Button) mUnionPanel
				.findViewById(R.id.cancel);
		mUnionButton.setOnClickListener(this);
		mCancelUnionButton.setOnClickListener(this);
		return layout;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] selectionArgs = new String[] { String.valueOf(app.getStartDate(getActivity()).date), String.valueOf(app.getEndDate(getActivity()).date)};
		CursorLoader loader = new CursorLoader(this.getActivity(),
				RecordsDbHelper.CONTENT_URI_ALLTIMES, null,
				RecordsDbHelper.STARTTIME + " IS NOT NULL ", selectionArgs, null);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		if (mAdapter != null && cursor != null) {
			mAdapter.swapCursor(cursor); // swap the new cursor in.
			
			if (!mAdapter.getSelected().isEmpty()
					&& Collections.max(mAdapter.getSelected().keySet()) >= cursor.getCount()) {
				mAdapter.setSelectedPosition(TimesCursorAdapter.NORMAL_MODE);
				mAdapter.getSelected().clear();
			}
			if (mAdapter.getChoiceUnionMode() != TimesCursorAdapter.NORMAL_MODE)
				mUnionPanel.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReload() {
		mStartdate = app.getStartDate(getActivity()).date;
		mAdapter.setStartDate(mStartdate);
		loadermanager.restartLoader(LOADER_ID, null, this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		if (mAdapter.getChoiceUnionMode() == TimesCursorAdapter.NORMAL_MODE) {
			Cursor cursor = mAdapter.getCursor();
			int idtimer = cursor.getInt(0);
			int idRecord = cursor.getInt(5);
			long start = cursor.getLong(2);
			long lenght = cursor.getLong(1);
			cursor = getActivity().getContentResolver().query(RecordsDbHelper.CONTENT_URI_NOTES,
					new String[] {RecordsDbHelper.ID3, RecordsDbHelper.NOTE}, RecordsDbHelper.ID3 + "=?",new String[] { String.valueOf(idRecord) }, null);
			String note;
			if(cursor.getCount() ==1){
				cursor.moveToFirst();
				note = cursor.getString(1);
			} else
				note = null;
			SplitRecordDialogFragment mSplitRecordDialog = new SplitRecordDialogFragment();
			mSplitRecordDialog
					.setCounterDialogListener((MainActivity) getActivity());
			mSplitRecordDialog.setValues(idtimer, idRecord, start, lenght, note == null ? "": note);
			mSplitRecordDialog.show(this.getActivity()
					.getFragmentManager(), "mSplitRecordDialog");
		} else {
			CheckBox check = (CheckBox) arg1.findViewById(R.id.check);
			if (check.getVisibility() == View.VISIBLE) {
				if (!check.isChecked()) {
					check.setChecked(true);
					mAdapter.getSelected().put(position, true);
					if(position + 1  < mList.getCount() && mAdapter.getSelected().get(position + 1) == null )
						mAdapter.getSelected().put(position + 1, false);
					if(position - 1 >= 0 && mAdapter.getSelected().get(position - 1) == null)
						mAdapter.getSelected().put(position - 1, false);
					onTimeRecordChange();
				} else {
					check.setChecked(false);
					if(position == mAdapter.getChoiceUnionMode()) {
						mAdapter.setChoiceUnionMode(TimesCursorAdapter.NORMAL_MODE);
						mAdapter.getSelected().clear();
						mUnionPanel.setVisibility(View.GONE);
						onTimeRecordChange();
						return;
					}
					mAdapter.getSelected().put(position, false);
					if(position > mAdapter.getChoiceUnionMode()){
						HashMap<Integer, Boolean> newSelected = new HashMap<Integer, Boolean>();
						for(Entry<Integer, Boolean> iterable_element : mAdapter.getSelected().entrySet()) {
							if(iterable_element.getKey() <= position)
								newSelected.put(iterable_element.getKey(), iterable_element.getValue());
						}
						mAdapter.setSelected(newSelected);
						onTimeRecordChange();
					}
					if(position < mAdapter.getChoiceUnionMode()){
						HashMap<Integer, Boolean> newSelected = new HashMap<Integer, Boolean>();
						for(Entry<Integer, Boolean> iterable_element : mAdapter.getSelected().entrySet()) {
							if(iterable_element.getKey() >= position)
								newSelected.put(iterable_element.getKey(), iterable_element.getValue());
						}
						mAdapter.setSelected(newSelected);
						onTimeRecordChange();
					}
				}
			} else {
				if(check.getAnimation() != null && check.getAnimation().hasEnded())
					check.clearAnimation();
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
			int position, long arg3) {
		mAdapter.getSelected().clear();
		mAdapter.setChoiceUnionMode(position);
		
		mUnionPanel.setVisibility(View.VISIBLE);
		onTimeRecordChange();
		return true;
	}

	public void onTimeRecordChange() {
		mList.invalidateViews();
	}

	

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.ok) {
			Cursor times = mAdapter.getCursor();
			long start = Long.MAX_VALUE;
			//long lenght = 0;
			long stop = Long.MIN_VALUE;
			long clenght = -1;
			boolean nowCounter = false;
			int iDtimer = -1;
			String note = null;
			ArrayList<Integer> idrecords = new ArrayList<Integer>();
			HashMap<Integer, Boolean> selected = mAdapter.getSelected();
			for (Entry<Integer, Boolean> iterable_element : selected.entrySet()) {
				if (!iterable_element.getValue())
					continue;
				times.moveToPosition(iterable_element.getKey());
				if (times.getLong(2) < start)
					start = times.getLong(2);
				if (clenght < times.getLong(1)) {
					clenght = times.getLong(1);
					iDtimer = times.getInt(0);
					Cursor cursor = getActivity().getContentResolver().query(RecordsDbHelper.CONTENT_URI_NOTES,
							new String[] {RecordsDbHelper.ID3, RecordsDbHelper.NOTE}, RecordsDbHelper.ID3 + "=?",new String[] { String.valueOf(times.getInt(5)) }, null);
					
					if(cursor.getCount() ==1){
						cursor.moveToFirst();
						note = cursor.getString(1);
					} else
						note = null;
					cursor.close();
				}
				if(stop <  times.getLong(7))
					stop = times.getLong(7);
				//lenght += times.getLong(1);
				idrecords.add(times.getInt(5));
				if (times.getLong(1) == 0) {
					nowCounter = true;
					iDtimer = times.getInt(0);
					Cursor cursor = getActivity().getContentResolver().query(RecordsDbHelper.CONTENT_URI_NOTES,
							new String[] {RecordsDbHelper.ID3, RecordsDbHelper.NOTE}, RecordsDbHelper.ID3 + "=?",new String[] { String.valueOf(times.getInt(5)) }, null);
					if(cursor.getCount() ==1){
						cursor.moveToFirst();
						note = cursor.getString(1);
					} else
						note = null;
					cursor.close();
				}

			}
			long lenght = stop - start > 0 ? stop - start : new Date().getTime() - start;
			UnionRecordDialogFragment unionRecordDialog = new UnionRecordDialogFragment();
			unionRecordDialog.setDialogListener((MainActivity) getActivity());
			unionRecordDialog.setValues(mAdapter.getSelected(), start, lenght,
					nowCounter, iDtimer, idrecords, note);
			unionRecordDialog.show(this.getActivity()
					.getFragmentManager(), "mUnionRecordDialog");
		} else {
			setNormalMode();
			onTimeRecordChange();
		}
	}

	public void setNormalMode() {
		mAdapter.setSelectedPosition(TimesCursorAdapter.NORMAL_MODE);
		mAdapter.getSelected().clear();
		mUnionPanel.setVisibility(View.GONE);
	}

}
