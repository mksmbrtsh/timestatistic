package maximsblog.blogspot.com.timestatistic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import maximsblog.blogspot.com.timestatistic.MainActivity.MainFragments;
import maximsblog.blogspot.com.timestatistic.TimesCursorAdapter.ITimes;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.support.v4.widget.SimpleCursorAdapter;

public class TimeRecordsFragment extends Fragment implements
		LoaderCallbacks<Cursor>, MainFragments, OnItemClickListener,
		OnItemLongClickListener, ITimes, OnClickListener {
	public static TimeRecordsFragment newInstance() {

		return new TimeRecordsFragment();
	}

	private LoaderManager loadermanager;
	private ListView mList;
	private TimesCursorAdapter mAdapter;
	private View mUnionPanel;
	private int mChoiceUnionMode;
	private HashMap<Integer, Boolean> mSelected;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadermanager = getLoaderManager();
		String[] uiBindFrom = { RecordsDbHelper.NAME,
				RecordsDbHelper.STARTTIME, RecordsDbHelper.LENGHT };
		int[] uiBindTo = { R.id.name, R.id.start, R.id.lenght };

		mAdapter = new TimesCursorAdapter(this.getActivity(),
				R.layout.time_row, null, uiBindFrom, uiBindTo, 0, this);
		loadermanager.initLoader(1, null, this);
		if (savedInstanceState != null) {
			mChoiceUnionMode = savedInstanceState.getInt("mChoiceUnionMode");
			mSelected = (HashMap<Integer, Boolean>) savedInstanceState
					.getSerializable("mSelected");
		} else {
			mSelected = new HashMap<Integer, Boolean>();
			mChoiceUnionMode = TimesCursorAdapter.NORMAL_MODE;
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
		CursorLoader loader = new CursorLoader(this.getActivity(),
				RecordsDbHelper.CONTENT_URI_ALLTIMES, null,
				RecordsDbHelper.STARTTIME + " IS NOT NULL ", null, null);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		if (mAdapter != null && cursor != null) {
			mAdapter.swapCursor(cursor); // swap the new cursor in.
			
			if (!mSelected.isEmpty() && Collections.max(mSelected.keySet()) >= cursor.getCount()) {
				mChoiceUnionMode = TimesCursorAdapter.NORMAL_MODE;
				mSelected.clear();
			}
			mAdapter.setSelectedPosition(mChoiceUnionMode);
			mAdapter.setSelected(mSelected);
			if (mChoiceUnionMode != TimesCursorAdapter.NORMAL_MODE)
				mUnionPanel.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReload() {
		loadermanager.restartLoader(1, null, this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (mAdapter.getChoiceUnionMode() == TimesCursorAdapter.NORMAL_MODE) {
			Cursor cursor = mAdapter.getCursor();
			int idtimer = cursor.getInt(0);
			int idRecord = cursor.getInt(5);
			long start = cursor.getLong(2);
			long lenght = cursor.getLong(1);
			SplitRecordDialogFragment mSplitRecordDialog = new SplitRecordDialogFragment();
			mSplitRecordDialog
					.setCounterDialogListener((MainActivity) getActivity());
			mSplitRecordDialog.setValues(idtimer, idRecord, start, lenght);
			mSplitRecordDialog.show(this.getActivity()
					.getSupportFragmentManager(), "mSplitRecordDialog");
		} else {
			CheckBox check = (CheckBox)arg1.findViewById(R.id.check);
			if(check.getVisibility() == View.VISIBLE)
				check.toggle();
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
			int position, long arg3) {
		mSelected.clear();
		mChoiceUnionMode = position;
		mAdapter.setSelected(mSelected);
		((TimesCursorAdapter) mList.getAdapter())
				.setChoiceUnionMode(mChoiceUnionMode);

		mList.invalidateViews();
		slideVisibleBotton(mUnionPanel, View.VISIBLE);
		return true;
	}

	@Override
	public void onTimeRecordChange() {
		mList.invalidateViews();
		if (mAdapter.getChoiceUnionMode() == TimesCursorAdapter.NORMAL_MODE) {
			setNormalMode();
		}
	}

	public void slideVisibleBotton(View view, int visible) {
		if (visible == View.VISIBLE) {
			TranslateAnimation animate = new TranslateAnimation(0, 0,
					view.getHeight(), 0);
			animate.setDuration(500);
			animate.setFillAfter(true);
			view.startAnimation(animate);
			view.setVisibility(View.VISIBLE);

		} else {
			TranslateAnimation animate = new TranslateAnimation(0, 0, 0,
					view.getHeight());
			animate.setDuration(500);
			animate.setFillAfter(true);
			view.startAnimation(animate);
			view.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.ok) {
			Cursor times = getActivity().getContentResolver().query(
					RecordsDbHelper.CONTENT_URI_ALLTIMES, null, null, null,
					null);
			long start = Long.MAX_VALUE;
			long lenght = 0;
			long clenght = -1;
			boolean nowCounter = false;
			int iDtimer = -1;
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
				}
				lenght += times.getLong(1);
				idrecords.add(times.getInt(5));
				if (times.getLong(1) == 0) {
					nowCounter = true;
					iDtimer = times.getInt(5);
				}

			}

			UnionRecordDialogFragment unionRecordDialog = new UnionRecordDialogFragment();
			unionRecordDialog.setDialogListener((MainActivity) getActivity());
			unionRecordDialog.setValues(mAdapter.getSelected(), start, lenght,
					nowCounter, iDtimer, idrecords);
			unionRecordDialog.show(this.getActivity()
					.getSupportFragmentManager(), "mUnionRecordDialog");
		} else {
			setNormalMode();
		}
	}

	public void setNormalMode() {
		mAdapter.setSelectedPosition(mChoiceUnionMode = TimesCursorAdapter.NORMAL_MODE);
		mSelected.clear();
		mAdapter.setSelected(mSelected);
		slideVisibleBotton(mUnionPanel, View.GONE);
	}

}
