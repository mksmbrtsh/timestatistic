package maximsblog.blogspot.com.timestatistic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import maximsblog.blogspot.com.timestatistic.MainActivity.MainFragments;
import maximsblog.blogspot.com.timestatistic.TimesCursorAdapter.ITimes;
import android.content.Intent;
import android.database.Cursor;
import android.opengl.Visibility;
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
import android.widget.AbsListView.RecyclerListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v4.widget.SimpleCursorAdapter;

public class DiaryFragment extends Fragment implements LoaderCallbacks<Cursor>,
		MainFragments, OnItemClickListener, OnItemLongClickListener,
		OnClickListener {
	
	private final int LOADER_ID = 4;
	
	public static DiaryFragment newInstance() {

		return new DiaryFragment();
	}

	private LoaderManager loadermanager;
	private ListView mList;
	private DiaryCursorAdapter mAdapter;
	private long mStartdate;
	private String mFilterNotes;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadermanager = getLoaderManager();
		String[] uiBindFrom = { RecordsDbHelper.NAME,
				RecordsDbHelper.STARTTIME, RecordsDbHelper.LENGHT,
				RecordsDbHelper.NOTE };
		int[] uiBindTo = { R.id.name, R.id.time, R.id.lenght_record, R.id.note_text };
		mStartdate = app.getStartDate(getActivity()).date;
		mAdapter = new DiaryCursorAdapter(this.getActivity(),
				R.layout.diary_row, null, uiBindFrom, uiBindTo, 0, mStartdate);
		loadermanager.initLoader(LOADER_ID, null, this);
		if (savedInstanceState != null) {
			mAdapter.setSelectedPosition(savedInstanceState
					.getInt("mChoiceUnionMode"));
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
		RelativeLayout layout = (RelativeLayout) inflater.inflate(
				R.layout.fragment_diary, container, false);
		mList = (ListView) layout.findViewById(R.id.listView1);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(this);
		mList.setEmptyView(layout.findViewById(R.id.empty));
		return layout;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader loader;
		String[] selectionArgs;
		if (mFilterNotes != null && mFilterNotes.length() > 0)
			selectionArgs = new String[] { mFilterNotes };
		else
			selectionArgs = new String[] { "" };

		loader = new CursorLoader(this.getActivity(),
				RecordsDbHelper.CONTENT_URI_ALLNOTES, null,
				RecordsDbHelper.STARTTIME + " IS NOT NULL AND "
						+ RecordsDbHelper.NOTE + " LIKE ?", selectionArgs,
				null);

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		if (mAdapter != null && cursor != null) {
			mAdapter.swapCursor(cursor); // swap the new cursor in.

			if (!mAdapter.getSelected().isEmpty()
					&& Collections.max(mAdapter.getSelected().keySet()) >= cursor
							.getCount()) {
				mAdapter.setSelectedPosition(TimesCursorAdapter.NORMAL_MODE);
				mAdapter.getSelected().clear();
			}
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
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
			Cursor cursor = mAdapter.getCursor();
			String name = cursor.getString(3);
			int idRecord = cursor.getInt(5);
			long start = cursor.getLong(2);
			long lenght = cursor.getLong(1);
			String note = cursor.getString(8);
			DiaryEditorDialogFragment mDiaryEditorDialogFragment = new DiaryEditorDialogFragment();
			mDiaryEditorDialogFragment
					.setCounterDialogListener((MainActivity) getActivity());
			mDiaryEditorDialogFragment.setValues(name, idRecord, start, lenght, note);
			mDiaryEditorDialogFragment.show(this.getActivity()
					.getSupportFragmentManager(), "mDiaryEditorDialogFragment");
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
			int position, long arg3) {
		mAdapter.getSelected().clear();
		mAdapter.setChoiceUnionMode(position);

		onTimeRecordChange();
		return true;
	}

	public void onTimeRecordChange() {
		mList.invalidateViews();
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
					note = times.getString(8);
				}
				lenght += times.getLong(1);
				idrecords.add(times.getInt(5));
				if (times.getLong(1) == 0) {
					nowCounter = true;
					iDtimer = times.getInt(5);
					note = times.getString(8);
				}

			}

			UnionRecordDialogFragment unionRecordDialog = new UnionRecordDialogFragment();
			unionRecordDialog.setDialogListener((MainActivity) getActivity());
			unionRecordDialog.setValues(mAdapter.getSelected(), start, lenght,
					nowCounter, iDtimer, idrecords, note);
			unionRecordDialog.show(this.getActivity()
					.getSupportFragmentManager(), "mUnionRecordDialog");
		} else {
			setNormalMode();
			onTimeRecordChange();
		}
	}

	public void setNormalMode() {
		mAdapter.setSelectedPosition(TimesCursorAdapter.NORMAL_MODE);
		mAdapter.getSelected().clear();
	}

	public void setFilter(String query) {
		mFilterNotes = query;
	}

}
