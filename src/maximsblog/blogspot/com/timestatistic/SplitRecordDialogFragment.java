package maximsblog.blogspot.com.timestatistic;


import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class SplitRecordDialogFragment extends DialogFragment implements
		OnClickListener {
	private ISplitRecordDialog mListener;
	private Button mDelButton;
	private boolean mIsRunning;
	private int mIDtimer;
	private Spinner mCurrentCounter;
	private Spinner mNewCounter;
	private int mPosition;
	private long mStart;
	private long mLenght;
	private EditText mCurrentStart;
	private EditText mCurrentStop;
	private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat();
	private Calendar mCalendar = Calendar.getInstance();
	private SimpleCursorAdapter mCurrentCounterAdapter;
	private SimpleCursorAdapter mNewCounterAdapter;

	public interface ISplitRecordDialog {
		void onFinishDialog();
	}

	public void setCounterDialogListener(ISplitRecordDialog listener) {
		mListener = listener;
	}


	public void setIsRunning(boolean isRunning) {
		mIsRunning = isRunning;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater
				.inflate(R.layout.fragment_splitrecord_dialog, null);
		v.findViewById(R.id.ok).setOnClickListener(this);
		v.findViewById(R.id.cancel).setOnClickListener(this);
		mCurrentCounter = (Spinner) v.findViewById(R.id.current_counter);
		mNewCounter = (Spinner) v.findViewById(R.id.new_record_counter);
		
		String[] from = { RecordsDbHelper.NAME };
		int[] to = {  android.R.id.text1 };
		
		mCurrentCounterAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, null, from, to);
		mCurrentCounter.setAdapter(mCurrentCounterAdapter);
		
		mNewCounterAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, null, from, to);
		mNewCounter.setAdapter(mNewCounterAdapter);
		getDialog().getWindow().setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		mCurrentStart = (EditText) v.findViewById(R.id.current_start);
		mCurrentStop = (EditText) v.findViewById(R.id.current_stop);
		
		
		return v;
	}

	@Override
	public void onResume() {
		Cursor timers = getActivity().getContentResolver().query(RecordsDbHelper.CONTENT_URI_TIMES, new String[] {RecordsDbHelper.ID,
				RecordsDbHelper.LENGHT,
				RecordsDbHelper.STARTTIME,
				RecordsDbHelper.NAME,
				RecordsDbHelper.COLOR }, null, null, null);
		
		Cursor newtimers = getActivity().getContentResolver().query(RecordsDbHelper.CONTENT_URI_TIMES, new String[] {RecordsDbHelper.ID,
				RecordsDbHelper.LENGHT,
				RecordsDbHelper.STARTTIME,
				RecordsDbHelper.NAME,
				RecordsDbHelper.COLOR }, null, null, null);
		for(int i1=0,cnt1 = newtimers.getCount();i1<cnt1;i1++)
		{
			newtimers.moveToPosition(i1);
			if(newtimers.getInt(1) == mIDtimer)
			{
				mPosition = i1;
				break;
			}
		}
		
		
		
		((SimpleCursorAdapter)mCurrentCounter.getAdapter()).swapCursor(timers);
		((SimpleCursorAdapter)mNewCounter.getAdapter()).swapCursor(newtimers);
		
		
		mCurrentCounter.setSelection(mPosition);
		mCalendar.setTimeInMillis(mStart);
		mCurrentStart.setText(mSimpleDateFormat.format(mCalendar.getTime()));
		mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(mStart + mLenght);
		mCurrentStop.setText(mSimpleDateFormat.format(mCalendar.getTime()));
		super.onResume();
	};

	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.ok) {
			mListener.onFinishDialog();
		} else if (id == R.id.del) {
			mListener.onFinishDialog();
		}
		dismiss();
	}

	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}

	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
	}


	public void setValues(int id, long start, long lenght) {
		mIDtimer = id;
		mStart = start;
		mLenght = lenght;
		
		
	}

	
}