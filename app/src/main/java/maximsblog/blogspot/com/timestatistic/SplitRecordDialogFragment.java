package maximsblog.blogspot.com.timestatistic;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SplitRecordDialogFragment extends DialogFragment implements
		OnClickListener, IdateChange {
	private IRecordDialog mListener;

	private Spinner mCurrentCounter;
	private EditText mCurrentNoteEdit;
	// original values
	private int mOriginalPosition;
	private long mOriginalStart;
	private long mOriginalLenght;
	private int mIDtimer;
	private int mIDrecord;
	private int mRecoderPosition;

	// edit values
	private int mCurrentPosition;
	private long mCurrentStart;
	private long mCurrentLenght;
	private String mCurrentNote;

	private Calendar mCalendar = Calendar.getInstance();
	private SimpleCursorAdapter mCurrentCounterAdapter;


	private SplitView mSplitView;


	public void setCounterDialogListener(IRecordDialog listener) {
		mListener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mOriginalPosition = savedInstanceState.getInt("mOriginalPosition");
			mOriginalStart = savedInstanceState.getLong("mOriginalStart");
			mOriginalLenght = savedInstanceState.getLong("mOriginalLenght");
			mIDtimer = savedInstanceState.getInt("mIDtimer");
			mIDrecord = savedInstanceState.getInt("mIDrecord");
			mRecoderPosition = savedInstanceState.getInt("mIDrecord");
			mCurrentPosition = savedInstanceState.getInt("mCurrentPosition");
			mCurrentStart = savedInstanceState.getLong("mCurrentStart");
			mCurrentLenght = savedInstanceState.getLong("mCurrentLenght");
			mCurrentNote = savedInstanceState.getString("mCurrentNote");
		} else {
			mOriginalPosition = -1;
			mCurrentPosition = -1;
		}

	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// original values
		outState.putInt("mOriginalPosition", mOriginalPosition);
		outState.putLong("mOriginalStart", mOriginalStart);
		outState.putLong("mOriginalLenght", mOriginalLenght);
		outState.putInt("mIDtimer", mIDtimer);
		outState.putInt("mIDrecord", mIDrecord);
		outState.putInt("mRecoderPosition", mRecoderPosition);
		// edit values
		outState.putInt("mCurrentPosition",
				mCurrentCounter.getSelectedItemPosition());
		outState.putLong("mCurrentStart", mCurrentStart);
		outState.putLong("mCurrentLenght", mCurrentLenght);
		outState.putString("mCurrentNote", mCurrentNote = mCurrentNoteEdit.getText().toString());
		super.onSaveInstanceState(outState);
	};

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().setTitle(getString(R.string.edit));
		View v = inflater.inflate(R.layout.fragment_splitrecord_dialog, container, false);
		v.findViewById(R.id.ok).setOnClickListener(this);
		v.findViewById(R.id.cancel).setOnClickListener(this);
		v.findViewById(R.id.union).setOnClickListener(this);
		mCurrentCounter = (Spinner) v.findViewById(R.id.current_counter);
		mCurrentNoteEdit = (EditText)v.findViewById(R.id.current_note);
		mSplitView = (SplitView) v.findViewById(R.id.split_view);
		String[] from = { RecordsDbHelper.NAME };
		int[] to = { android.R.id.text1 };

		mCurrentCounterAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_spinner_item, null, from, to);
		mCurrentCounterAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCurrentCounter.setAdapter(mCurrentCounterAdapter);
		mSplitView.setIdateChange(this);
		mSplitView.setDateTimes(mIDrecord,mCurrentStart,mCurrentLenght);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		Cursor newtimers = getActivity().getContentResolver().query(
				RecordsDbHelper.CONTENT_URI_TIMES, null, null, null, RecordsDbHelper.SORTID);
		if (mOriginalPosition == -1)
			for (int i1 = 0, cnt1 = newtimers.getCount(); i1 < cnt1; i1++) {
				newtimers.moveToPosition(i1);
				if (newtimers.getInt(4) == mIDtimer) {
					mOriginalPosition = i1;
					mCurrentPosition = i1;
					break;
				}
			}
		((SimpleCursorAdapter) mCurrentCounter.getAdapter())
				.swapCursor(newtimers);
		mCurrentCounter.setSelection(mCurrentPosition);
		if(mCurrentNote!=null) {
			mCurrentNoteEdit.setText("");
			mCurrentNoteEdit.append(mCurrentNote);
		}
	};

	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.ok) {
			if (mOriginalPosition != mCurrentCounter.getSelectedItemPosition()
					|| mOriginalStart != mCurrentStart
					|| mOriginalLenght + mOriginalStart != mCurrentStart
							+ mCurrentLenght
							|| !mCurrentNote.equals(mCurrentNoteEdit.getText().toString())){
				editRecord();
				app.setStatusBar(getActivity().getApplicationContext());
				app.setRunningCounterAlarmSettings(getActivity().getApplicationContext());
			}
			mListener.onRefreshFragmentsValue();
			dismiss();
		} else if (id == R.id.union) {
			mListener.onRefreshFragmentsValue();
			dismiss();
			TimeRecordsFragment timeRecordsFragment = (TimeRecordsFragment)((MainActivity)getActivity()).findFragmentByPosition(1);
			timeRecordsFragment.setUnionMode(mRecoderPosition);
			timeRecordsFragment.onReload();
		} else if (id == R.id.cancel) {
			dismiss();
		}
	}

	private void editRecord() {
		ContentValues cv = new ContentValues();
		Cursor c = ((SimpleCursorAdapter) mCurrentCounter.getAdapter())
				.getCursor();
		c.moveToPosition(mCurrentCounter.getSelectedItemPosition());
		if (mOriginalLenght == 0) {
			cv.put(RecordsDbHelper.ISRUNNING, 1);
			getActivity().getContentResolver().update(
					RecordsDbHelper.CONTENT_URI_TIMERS, cv,
					RecordsDbHelper.ID + " = ?",
					new String[] { String.valueOf(c.getInt(4)) });
			cv.clear();
		}
		cv.put(RecordsDbHelper.TIMERSID, c.getInt(4));
		cv.put(RecordsDbHelper.STARTTIME, mCurrentStart);
		cv.put(RecordsDbHelper.LENGHT, mCurrentLenght);
		cv.put(RecordsDbHelper.ENDTIME, mCurrentStart + mCurrentLenght);
		getActivity().getContentResolver().update(
				RecordsDbHelper.CONTENT_URI_TIMES, cv,
				RecordsDbHelper.ID2 + "=?",
				new String[] { String.valueOf(mIDrecord) });
		cv.clear();
		editNote(mIDrecord, mCurrentNoteEdit.getText().toString().trim());
		Uri u;
		if (mOriginalLenght + mOriginalStart != mCurrentStart + mCurrentLenght
				&& mCurrentLenght != 0) {
			cv.put(RecordsDbHelper.TIMERSID, c.getInt(4));
			cv.put(RecordsDbHelper.STARTTIME, mCurrentStart + mCurrentLenght);
			if (mOriginalLenght != 0) {
				cv.put(RecordsDbHelper.LENGHT, mOriginalStart + mOriginalLenght
						- (mCurrentStart + mCurrentLenght));
				cv.put(RecordsDbHelper.ENDTIME, mCurrentStart + mCurrentLenght + (mOriginalStart + mOriginalLenght
						- (mCurrentStart + mCurrentLenght)));
			}
			else {
				cv.put(RecordsDbHelper.LENGHT, 0);
			}
			u = getActivity().getContentResolver().insert(
					RecordsDbHelper.CONTENT_URI_TIMES, cv);
			cv.clear();
			int id = Integer.valueOf(u.getLastPathSegment());
			editNote(mIDrecord, mCurrentNoteEdit.getText().toString().trim());
			
			if (mOriginalLenght == 0) {
				cv.put(RecordsDbHelper.ISRUNNING, 1);
				getActivity().getContentResolver().update(
						RecordsDbHelper.CONTENT_URI_TIMERS, cv,
						RecordsDbHelper.ID + " = ?",
						new String[] { String.valueOf(c.getInt(4)) });
				cv.clear();
			}
		}
	}

	private void editNote(int id, String note) {
		if(note.length() == 0){
			getActivity().getContentResolver().delete(RecordsDbHelper.CONTENT_URI_NOTES, RecordsDbHelper.ID3 + "=?", new String[]{String.valueOf(id)});
			return;
		}
		ContentValues cv = new ContentValues();
		cv.put(RecordsDbHelper.ID3, id);
		cv.put(RecordsDbHelper.NOTE, note);
		getActivity().getContentResolver().insert(
				RecordsDbHelper.CONTENT_URI_NOTES, cv);
	}

	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}

	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
	}

	public void setValues(int position, int idtimer, int idRecord, long start, long lenght, String note) {
		mRecoderPosition = position;
		mIDtimer = idtimer;
		mIDrecord = idRecord;
		mOriginalStart = start;
		mOriginalLenght = lenght;

		mCurrentStart = start;
		mCurrentLenght = lenght;
		mCurrentNote = note;
	}

	@Override
	public void timeChange(long newvalue) {
		mCurrentLenght = newvalue - mCurrentStart;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}
}