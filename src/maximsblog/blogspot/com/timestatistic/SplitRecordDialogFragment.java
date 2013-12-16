package maximsblog.blogspot.com.timestatistic;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.DataFormatException;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.Spinner;

public class SplitRecordDialogFragment extends DialogFragment implements
		OnClickListener, IdateChange {
	private ISplitRecordDialog mListener;
	private Button mDelButton;
	private boolean mIsRunning;
	private int mIDtimer;
	private Spinner mCurrentCounter;
	private Spinner mNewCounter;
	
	// original values
	private int mOriginalPosition;
	private long mOriginalStart;
	private long mOriginalLenght;
	
	// edit values
	private int mCurrentPosition;
	private long mCurrentStart;
	private long mCurrentLenght;
	private long mNewStart;
	private long mNewLenght;
	
	
	private Calendar mCalendar = Calendar.getInstance();
	private SimpleCursorAdapter mCurrentCounterAdapter;
	private SimpleCursorAdapter mNewCounterAdapter;
	private TextView mPeriod;
	
	private Button mCurrentStopDate;
	private Button mCurrentStopTime;
	private Button mCurrentStartTime;
	private Button mCurrentStartDate;

	private Button mNewStopDate;
	private Button mNewStopTime;
	private Button mNewStartTime;
	private Button mNewStartDate;
	
	DateFormat mFormatterDate = DateFormat.getDateInstance();
	DateFormat mFormatterTime = DateFormat.getTimeInstance(DateFormat.SHORT);
	

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
		getDialog().setTitle(getString(R.string.edit));
		View v = inflater.inflate(R.layout.fragment_splitrecord_dialog, null);
		v.findViewById(R.id.ok).setOnClickListener(this);
		v.findViewById(R.id.cancel).setOnClickListener(this);
		mCurrentCounter = (Spinner) v.findViewById(R.id.current_counter);
		mNewCounter = (Spinner) v.findViewById(R.id.new_record_counter);

		String[] from = { RecordsDbHelper.NAME };
		int[] to = { android.R.id.text1 };

		mCurrentCounterAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_spinner_item, null, from, to);
		mCurrentCounter.setAdapter(mCurrentCounterAdapter);

		mNewCounterAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_spinner_item, null, from, to);
		mNewCounter.setAdapter(mNewCounterAdapter);
		getDialog().getWindow().setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_VISIBLE);

		mCurrentStartTime = (Button) v.findViewById(R.id.current_starttime);
		mCurrentStartDate = (Button) v.findViewById(R.id.current_startdate);
		mCurrentStopDate = (Button) v.findViewById(R.id.current_stopdate);
		mCurrentStopTime = (Button) v.findViewById(R.id.current_stoptime);
		
		mNewStopDate = (Button) v.findViewById(R.id.new_stopdate);
		mNewStopTime = (Button) v.findViewById(R.id.new_stoptime);
		mNewStartTime = (Button) v.findViewById(R.id.new_starttime);
		mNewStartDate = (Button) v.findViewById(R.id.new_startdate);
		
		mPeriod = (TextView) v.findViewById(R.id.period);

		mCurrentStartTime.setOnClickListener(this);
		mCurrentStartDate.setOnClickListener(this);
		mCurrentStopTime.setOnClickListener(this);
		mCurrentStopDate.setOnClickListener(this);
		
		mNewStopDate.setOnClickListener(this);
		mNewStopTime.setOnClickListener(this);
		mNewStartTime.setOnClickListener(this);
		mNewStartDate.setOnClickListener(this);
		
		return v;
	}

	
	@Override
	public void onResume() {
		Cursor timers = getActivity().getContentResolver().query(
				RecordsDbHelper.CONTENT_URI_TIMES,
				new String[] { RecordsDbHelper.ID, RecordsDbHelper.LENGHT,
						RecordsDbHelper.STARTTIME, RecordsDbHelper.NAME,
						RecordsDbHelper.COLOR }, null, null, null);

		Cursor newtimers = getActivity().getContentResolver().query(
				RecordsDbHelper.CONTENT_URI_TIMES,
				new String[] { RecordsDbHelper.ID, RecordsDbHelper.LENGHT,
						RecordsDbHelper.STARTTIME, RecordsDbHelper.NAME,
						RecordsDbHelper.COLOR }, null, null, null);
		for (int i1 = 0, cnt1 = newtimers.getCount(); i1 < cnt1; i1++) {
			newtimers.moveToPosition(i1);
			if (newtimers.getInt(1) == mIDtimer) {
				mOriginalPosition = i1;
				break;
			}
		}

		((SimpleCursorAdapter) mCurrentCounter.getAdapter()).swapCursor(timers);
		((SimpleCursorAdapter) mNewCounter.getAdapter()).swapCursor(newtimers);

		mCurrentCounter.setSelection(mOriginalPosition);
		mCalendar.setTimeInMillis(mCurrentStart);
		
		String startString = mFormatterDate.format(mCalendar.getTime());
		mCurrentStartDate.setText(startString);
		startString = mFormatterTime.format(mCalendar.getTime());
		mCurrentStartTime.setText(startString);
		mCalendar.setTimeInMillis(mCurrentStart + mCurrentLenght);
		String stopString = mFormatterDate.format(mCalendar.getTime());
		mCurrentStopDate.setText(stopString);
		stopString = mFormatterTime.format(mCalendar.getTime());
		mCurrentStopTime.setText(stopString);
		
		mPeriod.setText(startString + " - " + stopString);

		mCalendar.setTimeInMillis(mNewStart);
		startString = mFormatterTime.format(mCalendar.getTime());
		mNewStartTime.setText(startString);
		startString = mFormatterDate.format(mCalendar.getTime());
		mNewStartDate.setText(startString);
		mCalendar.setTimeInMillis(mNewStart + mNewLenght);
		stopString = mFormatterTime.format(mCalendar.getTime());
		mNewStopTime.setText(stopString);
		stopString = mFormatterDate.format(mCalendar.getTime());
		mNewStopDate.setText(stopString);
		
		
		super.onResume();
	};
	
	private void showPickerFragment(int id, long time, boolean istime)
	{
		if(istime) {
			TimePickerFragment newFragment = new TimePickerFragment();
			Bundle b = new Bundle();
			b.putLong("time", time);
			b.putInt("id", id);
			newFragment.setArguments(b);
			newFragment.setDateChange(this);
			newFragment.show(getActivity().getSupportFragmentManager(),
					"timePicker");
		} else {
			DatePickerFragment newFragment = new DatePickerFragment();
			Bundle b = new Bundle();
			b.putLong("time", time);
			b.putInt("id", id);
			newFragment.setArguments(b);
			newFragment.setDateChange(this);
			newFragment.show(getActivity().getSupportFragmentManager(),
					"datePicker");
		}
	}
	
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.ok) {
			mListener.onFinishDialog();
			dismiss();
		} else if (id == R.id.del) {
			mListener.onFinishDialog();
			dismiss();
		} else if (id == R.id.current_starttime) {
			showPickerFragment(id, mCurrentStart, true);
		} else if (id == R.id.current_startdate) {
			showPickerFragment(id, mCurrentStart, false);
		} else if (id == R.id.current_stoptime) {
			showPickerFragment(id, mCurrentStart + mCurrentLenght, true);
		} else if (id == R.id.current_stopdate) {
			showPickerFragment(id, mCurrentStart + mCurrentLenght, false);
		} else if (id == R.id.new_starttime) {
			showPickerFragment(id, mCurrentStart, true);
		} else if (id == R.id.new_startdate) {
			showPickerFragment(id, mCurrentStart, false);
		} else if (id == R.id.new_stoptime) {
			showPickerFragment(id, mCurrentStart + mCurrentLenght, true);
		} else if (id == R.id.new_stopdate) {
			showPickerFragment(id, mCurrentStart + mCurrentLenght, false);
		}

	}

	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}

	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
	}

	public void setValues(int id, long start, long lenght) {
		mIDtimer = id;
		mOriginalStart = start;
		mOriginalLenght = lenght;
		
		mCurrentStart = start;
		mCurrentLenght = lenght;
		
		mNewStart = start + lenght;
		mNewLenght = 0;
	}

	
	public static class TimePickerFragment extends DialogFragment implements
			TimePickerDialog.OnTimeSetListener {
		private Calendar c;
		private IdateChange mIdateChange;
		private int id;
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			c = Calendar.getInstance();
			c.setTimeInMillis(getArguments().getLong("time"));
			id = getArguments().getInt("id");
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
			return new TimePickerDialog(getActivity(), this, hour, minute, true);
		}
		public void setDateChange(IdateChange idc)
		{
			mIdateChange = idc;		
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			c.set(Calendar.HOUR_OF_DAY, hourOfDay);
			c.set(Calendar.MINUTE, minute);
			mIdateChange.timeChange(id, c.getTimeInMillis());	
		}
	}

	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {
		private Calendar c;
		private IdateChange mIdateChange;
		private int id;
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			c = Calendar.getInstance();
			c.setTimeInMillis(getArguments().getLong("time"));
			id = getArguments().getInt("id");
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}
		public void setDateChange(IdateChange idc)
		{
			mIdateChange = idc;		
		}
		public void onDateSet(DatePicker view, int year, int month, int day) {
			c.set(Calendar.YEAR, year);
			c.set(Calendar.MONTH, month);
			c.set(Calendar.DAY_OF_MONTH, day);
			mIdateChange.timeChange(id, c.getTimeInMillis());	
		}
	}

	@Override
	public void timeChange(int id, long newvalue) {
		if (id == R.id.current_startdate)
		{
			mCurrentStart = newvalue;
			mCalendar.setTimeInMillis(mCurrentStart);
			mCurrentStartDate.setText(mFormatterDate.format(mCalendar.getTime()));
		} else if (id == R.id.current_starttime)
		{
			mCurrentStart = newvalue;
			mCalendar.setTimeInMillis(mCurrentStart);
			mCurrentStartTime.setText(mFormatterTime.format(mCalendar.getTime()));
		} else if (id == R.id.current_stopdate)
		{
			mCurrentLenght = newvalue - mCurrentStart;
			mCalendar.setTimeInMillis(mCurrentStart + mCurrentLenght);
			mCurrentStopDate.setText(mFormatterDate.format(mCalendar.getTime()));
		} else if (id == R.id.current_stoptime)
		{
			mCurrentLenght = newvalue - mCurrentStart;
			mCalendar.setTimeInMillis(mCurrentStart + mCurrentLenght);
			mCurrentStopTime.setText(mFormatterTime.format(mCalendar.getTime()));
		} else if (id == R.id.new_startdate)// new
		{
			mNewStart = newvalue;
			mCalendar.setTimeInMillis(mNewStart);
			mNewStartDate.setText(mFormatterDate.format(mCalendar.getTime()));
		} else if (id == R.id.new_starttime)
		{
			mNewStart = newvalue;
			mCalendar.setTimeInMillis(mNewStart);
			mNewStartTime.setText(mFormatterTime.format(mCalendar.getTime()));
		} else if (id == R.id.new_stopdate)
		{
			mNewLenght = newvalue - mNewStart;
			mCalendar.setTimeInMillis(mNewStart + mNewLenght);
			mNewStopDate.setText(mFormatterDate.format(mCalendar.getTime()));
		} else if (id == R.id.new_stoptime)
		{
			mNewLenght = newvalue - mNewStart;
			mCalendar.setTimeInMillis(mNewStart + mNewLenght);
			mNewStopTime.setText(mFormatterTime.format(mCalendar.getTime()));
		}
		
	}
}