package maximsblog.blogspot.com.timestatistic;

import maximsblog.blogspot.com.timestatistic.MainActivity.MainFragments;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.support.v4.widget.SimpleCursorAdapter;

public class TimeRecordsFragment extends Fragment implements
		LoaderCallbacks<Cursor>, MainFragments, OnItemLongClickListener {
	public static TimeRecordsFragment newInstance() {

		return new TimeRecordsFragment();
	}

	private LoaderManager loadermanager;
	private ListView mList;
	private TimesCursorAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadermanager = getLoaderManager();
		String[] uiBindFrom = { RecordsDbHelper.NAME, RecordsDbHelper.STARTTIME, RecordsDbHelper.LENGHT };
		int[] uiBindTo = { R.id.name, R.id.start, R.id.lenght };

		mAdapter = new TimesCursorAdapter(this.getActivity(),
				R.layout.time_row, null, uiBindFrom, uiBindTo, 0);
		loadermanager.initLoader(1, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.fragment_time_records, container, false);
		mList = (ListView) layout.findViewById(R.id.listView1);
		mList.setAdapter(mAdapter);
		mList.setOnItemLongClickListener(this);
		return layout;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader loader = new CursorLoader(this.getActivity(),
				RecordsDbHelper.CONTENT_URI_ALLTIMES, null, RecordsDbHelper.STARTTIME + " IS NOT NULL " , null, null);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		if (mAdapter != null && cursor != null) {
			mAdapter.swapCursor(cursor); // swap the new cursor in.
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
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		/*Cursor cursor = mAdapter.getCursor();
		int id = cursor.getInt(0);
		long start = cursor.getLong(2);
		long lenght = cursor.getLong(1);
		SplitRecordDialogFragment mSplitRecordDialog = ((MainActivity)getActivity()).mSplitRecordDialog;
		mSplitRecordDialog.setValues(id, start, lenght);
		((MainActivity)getActivity()).mSplitRecordDialog.show(this.getActivity().getSupportFragmentManager(),
		"dlg1");*/
		return false;
	}

}
