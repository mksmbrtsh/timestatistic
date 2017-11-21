package maximsblog.blogspot.com.timestatistic;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class UnionRecordDialogFragment extends DialogFragment implements
		OnClickListener {
	private IRecordDialog mListener;
	private Spinner mCurrentCounter;

	private HashMap<Integer, Boolean> mSelected;

	// values
	private int mIDtimer;
	private long mStart;
	private long mLenght;
	private boolean mNowCounter;
	private ArrayList<Integer> mIdrecords;
	private int mCurrentPosition;

	private Calendar mCalendar = Calendar.getInstance();
	private SimpleCursorAdapter mCurrentCounterAdapter;

	DateFormat mFormatterDateTime = DateFormat.getDateTimeInstance(
			DateFormat.SHORT, DateFormat.SHORT);

	private TextView mUnionDateTimeInterval;
	private EditText mCurrentNoteEdit;
	private String mCurrentNote;

	public void setDialogListener(MainActivity mainActivity) {
		mListener = mainActivity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mStart = savedInstanceState.getLong("mStart");
			mLenght = savedInstanceState.getLong("mLenght");
			mIDtimer = savedInstanceState.getInt("mIDtimer");
			mNowCounter = savedInstanceState.getBoolean("mNowCounter");
			mCurrentNote = savedInstanceState.getString("mCurrentNoteEdit");
			mIdrecords = (ArrayList<Integer>) savedInstanceState
					.getSerializable("mIdrecords");
			mCurrentPosition = savedInstanceState.getInt("mCurrentPosition");
		} else {
			mCurrentPosition = -1;
		}

	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// values
		outState.putLong("mStart", mStart);
		outState.putLong("mLenght", mLenght);
		outState.putInt("mIDtimer", mIDtimer);
		outState.putSerializable("mIdrecords", mIdrecords);
		outState.putBoolean("mNowCounter", mNowCounter);
		outState.putInt("mCurrentPosition", mCurrentPosition);
		outState.putString(mCurrentNote = "mCurrentNoteEdit", mCurrentNoteEdit.getText()
				.toString());
		super.onSaveInstanceState(outState);
	};

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().setTitle(getString(R.string.edit));
		View v = inflater.inflate(R.layout.fragment_unionrecord_dialog, null);
		v.findViewById(R.id.ok).setOnClickListener(this);
		v.findViewById(R.id.cancel).setOnClickListener(this);

		mCurrentCounter = (Spinner) v.findViewById(R.id.current_counter);

		String[] from = { RecordsDbHelper.NAME };
		int[] to = { android.R.id.text1 };

		mCurrentCounterAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_spinner_item, null, from, to, 0);
		mCurrentCounterAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCurrentCounter.setAdapter(mCurrentCounterAdapter);
		mUnionDateTimeInterval = (TextView) v.findViewById(R.id.note_text);
		mCurrentNoteEdit = (EditText) v.findViewById(R.id.current_note);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		Cursor timers = getActivity().getContentResolver().query(
				RecordsDbHelper.CONTENT_URI_TIMES, null, null, null, RecordsDbHelper.SORTID);
		((SimpleCursorAdapter) mCurrentCounter.getAdapter()).swapCursor(timers);
		if (mCurrentPosition == -1)
			for (int i1 = 0, cnt1 = timers.getCount(); i1 < cnt1; i1++) {
				timers.moveToPosition(i1);
				if (timers.getInt(4) == mIDtimer) {
					mCurrentPosition = i1;
					break;
				}
			}
		mCurrentCounter.setSelection(mCurrentPosition);
		if(mCurrentNote!=null) {
			mCurrentNoteEdit.setText("");
			mCurrentNoteEdit.append(mCurrentNote);
		}
		setCurrentText();
	};

	private void setCurrentText() {
		mCalendar.setTimeInMillis(mStart);
		String startString = mFormatterDateTime.format(mCalendar.getTime());
		String stopString;
		mUnionDateTimeInterval.setText(startString);
		if (!mNowCounter) {
			mCalendar.setTimeInMillis(mStart + mLenght);
			stopString = mFormatterDateTime.format(mCalendar.getTime());

		} else {
			stopString = getString(R.string.now);
		}
		mUnionDateTimeInterval.setText(startString + " - " + stopString);
	}

	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.ok) {
			editRecord();
			mListener.onRefreshFragmentsValue();
			dismiss();
		} else if (id == R.id.cancel) {
			mListener.onRefreshFragmentsValue();
			dismiss();
		}
	}

	private void editRecord() {

		Cursor c = ((SimpleCursorAdapter) mCurrentCounter.getAdapter())
				.getCursor();
		c.moveToPosition(mCurrentCounter.getSelectedItemPosition());
		ContentValues cv = new ContentValues();
		if (mNowCounter) {
			cv.put(RecordsDbHelper.ISRUNNING, 1);
			getActivity().getContentResolver().update(
					RecordsDbHelper.CONTENT_URI_TIMERS, cv,
					RecordsDbHelper.ID + " = ?",
					new String[] { String.valueOf(c.getInt(4)) });
			cv.clear();
		} else {
			cv.put(RecordsDbHelper.LENGHT, mLenght);
			cv.put(RecordsDbHelper.ENDTIME, mStart + mLenght);
		}
		cv.put(RecordsDbHelper.TIMERSID, c.getInt(4));
		cv.put(RecordsDbHelper.STARTTIME, mStart);
		cv.put(RecordsDbHelper.ID2, c.getInt(0));
		getActivity().getContentResolver().insert(
				RecordsDbHelper.CONTENT_URI_TIMES, cv);

		editNote(c.getInt(0), mCurrentNoteEdit.getText().toString().trim());

		for (Integer iterable_element : mIdrecords) {
			if (c.getInt(0) != iterable_element) {
				getActivity().getContentResolver().delete(
						RecordsDbHelper.CONTENT_URI_TIMES,
						RecordsDbHelper.ID2 + "=?",
						new String[] { String.valueOf(iterable_element) });

				getActivity().getContentResolver().delete(
						RecordsDbHelper.CONTENT_URI_NOTES,
						RecordsDbHelper.ID3 + "=?",
						new String[] { String.valueOf(iterable_element) });
			}
		}
		if (mNowCounter) {
			app.loadRunningCounterAlarm(getActivity().getApplicationContext());
			app.setStatusBar(getActivity().getApplicationContext());
		}
	}

	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}

	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
	}

	public void setValues(HashMap<Integer, Boolean> selected, long start,
			long lenght, boolean nowCounter, int iDtimer,
			ArrayList<Integer> idrecords, String note) {
		mSelected = selected;
		mStart = start;
		mLenght = lenght;
		mNowCounter = nowCounter;
		mIDtimer = iDtimer;
		mIdrecords = idrecords;
		mCurrentNote = note;
	}

	private void editNote(int id, String note) {
		if (note.length() == 0) {
			getActivity().getContentResolver().delete(
					RecordsDbHelper.CONTENT_URI_NOTES,
					RecordsDbHelper.ID3 + "=?",
					new String[] { String.valueOf(id) });
			return;
		}
		ContentValues cv = new ContentValues();
		cv.put(RecordsDbHelper.ID3, id);
		cv.put(RecordsDbHelper.NOTE, note);
		getActivity().getContentResolver().insert(
				RecordsDbHelper.CONTENT_URI_NOTES, cv);
	}
}
