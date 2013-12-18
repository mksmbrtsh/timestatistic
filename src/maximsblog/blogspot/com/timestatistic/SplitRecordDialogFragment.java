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
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.Spinner;

public class SplitRecordDialogFragment extends DialogFragment implements
		OnClickListener, IdateChange {
	private ISplitRecordDialog mListener;

	private Spinner mCurrentCounter;
	private Spinner mAfterCounter;
	private Spinner mBeforeCounter;

	// original values
	private int mOriginalPosition;
	private long mOriginalStart;
	private long mOriginalLenght;
	private int mIDtimer;
	private int mIDrecord;

	// edit values
	private int mCurrentPosition;
	private long mCurrentStart;
	private long mCurrentLenght;

	private Calendar mCalendar = Calendar.getInstance();
	private SimpleCursorAdapter mCurrentCounterAdapter;
	private SimpleCursorAdapter mAfterCounterAdapter;
	private SimpleCursorAdapter mBeforeCounterAdapter;

	private Button mCurrentStopDateTime;
	private Button mCurrentStartDateTime;

	DateFormat mFormatterDateTime = DateFormat.getDateTimeInstance(
			DateFormat.SHORT, DateFormat.SHORT);
	private View mAfterLayout;
	private View mBeforeLayout;
	private TextView mAfterText;
	private TextView mBeforeText;

	private int mBeforePosition;

	private int mAfterPosition;

	public interface ISplitRecordDialog {
		void onFinishDialog();
	}

	public void setCounterDialogListener(ISplitRecordDialog listener) {
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
			
			mCurrentPosition = savedInstanceState.getInt("mCurrentPosition");
			mCurrentStart = savedInstanceState.getLong("mCurrentStart");
			mCurrentLenght = savedInstanceState.getLong("mCurrentLenght");
			
			mBeforePosition = savedInstanceState.getInt("mBeforeCounter");
			mAfterPosition = savedInstanceState.getInt("mAfterCounter");
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

		// edit values
		outState.putInt("mCurrentPosition", mCurrentCounter.getSelectedItemPosition());
		outState.putLong("mCurrentStart", mCurrentStart);
		outState.putLong("mCurrentLenght", mCurrentLenght);
		
		outState.putInt("mBeforeCounter", mBeforeCounter.getSelectedItemPosition());
		outState.putInt("mAfterCounter", mAfterCounter.getSelectedItemPosition());
	};
	

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().setTitle(getString(R.string.edit));
		View v = inflater.inflate(R.layout.fragment_splitrecord_dialog, null);
		v.findViewById(R.id.ok).setOnClickListener(this);
		v.findViewById(R.id.cancel).setOnClickListener(this);

		mCurrentCounter = (Spinner) v.findViewById(R.id.current_counter);
		mAfterCounter = (Spinner) v.findViewById(R.id.after_record_counter);
		mBeforeCounter = (Spinner) v.findViewById(R.id.before_record_counter);

		String[] from = { RecordsDbHelper.NAME };
		int[] to = { android.R.id.text1 };

		mCurrentCounterAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_spinner_item, null, from, to);
		mCurrentCounterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCurrentCounter.setAdapter(mCurrentCounterAdapter);

		mAfterCounterAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_spinner_item, null, from, to);
		mAfterCounterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAfterCounter.setAdapter(mAfterCounterAdapter);

		mBeforeCounterAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_spinner_item, null, from, to);
		mBeforeCounterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mBeforeCounter.setAdapter(mBeforeCounterAdapter);

		mCurrentStartDateTime = (Button) v
				.findViewById(R.id.current_startdatetime);
		mCurrentStopDateTime = (Button) v
				.findViewById(R.id.current_stopdatetime);

		mCurrentStartDateTime.setOnClickListener(this);
		mCurrentStopDateTime.setOnClickListener(this);

		mAfterLayout = v.findViewById(R.id.after_record);
		mBeforeLayout = v.findViewById(R.id.before_record);
		mAfterLayout.setVisibility(View.GONE);
		mBeforeLayout.setVisibility(View.GONE);

		mAfterText = (TextView) v.findViewById(R.id.after_period_value);
		mBeforeText = (TextView) v.findViewById(R.id.before_period_value);

		
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
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

		((SimpleCursorAdapter) mCurrentCounter.getAdapter())
				.swapCursor(newtimers);
		((SimpleCursorAdapter) mAfterCounter.getAdapter())
				.swapCursor(newtimers);
		((SimpleCursorAdapter) mBeforeCounter.getAdapter())
				.swapCursor(newtimers);

		mCurrentCounter.setSelection(mCurrentPosition);
		mAfterCounter.setSelection(mAfterPosition);
		mBeforeCounter.setSelection(mBeforePosition);
		
		mCalendar.setTimeInMillis(mCurrentStart);

		String startString = mFormatterDateTime.format(mCalendar.getTime());
		mCurrentStartDateTime.setText(startString);
		if (mOriginalLenght != 0) {
			mCalendar.setTimeInMillis(mCurrentStart + mCurrentLenght);
			String stopString = mFormatterDateTime.format(mCalendar.getTime());
			mCurrentStopDateTime.setText(stopString);
		} else
		{
			mCurrentStopDateTime.setText(getString(R.string.now));
		}
	};

	private void showPickerFragment(int id, long time, boolean istime) {
		CustomDateTimePickerFragment newFragment = new CustomDateTimePickerFragment();
		Bundle b = new Bundle();
		b.putLong("time", time);
		b.putInt("id", id);
		newFragment.setArguments(b);
		newFragment.setDateChange(this);
		newFragment.show(getActivity().getSupportFragmentManager(),
				"timePicker");
	}

	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.ok) {
			if (mCurrentPosition != mCurrentCounter.getSelectedItemPosition()
					|| mOriginalStart != mCurrentStart
					|| mOriginalLenght != mCurrentLenght)
				editRecord();
			mListener.onFinishDialog();
			dismiss();
		} else if (id == R.id.cancel) {
			dismiss();
		} else if (id == R.id.current_startdatetime) {
			showPickerFragment(id, mCurrentStart, true);
		} else if (id == R.id.current_stopdatetime) {
			if(mOriginalLenght != 0)
				showPickerFragment(id, mCurrentStart + mCurrentLenght, true);
			else
				showPickerFragment(id, new Date().getTime(), true);
		}

	}

	private void editRecord() {
		ContentValues cv = new ContentValues();
		Cursor c = ((SimpleCursorAdapter) mCurrentCounter.getAdapter())
				.getCursor();
		c.moveToPosition(mCurrentCounter.getSelectedItemPosition());
		cv.put(RecordsDbHelper.TIMERSID, c.getInt(1));
		cv.put(RecordsDbHelper.STARTTIME, mCurrentStart);
		cv.put(RecordsDbHelper.LENGHT, mCurrentLenght);
		
		getActivity().getContentResolver().update(
				RecordsDbHelper.CONTENT_URI_TIMES, cv,
				RecordsDbHelper.ID2 + "=?",
				new String[] { String.valueOf(mIDrecord) });
		cv.clear();
		if (mOriginalStart != mCurrentStart) {
			c.moveToPosition(mBeforeCounter.getSelectedItemPosition());
			cv.put(RecordsDbHelper.TIMERSID, c.getInt(1));
			cv.put(RecordsDbHelper.STARTTIME, mOriginalStart);
			cv.put(RecordsDbHelper.LENGHT, mCurrentStart - mOriginalStart);
			getActivity().getContentResolver().insert(
					RecordsDbHelper.CONTENT_URI_TIMES, cv);
			cv.clear();
		}

		if (mOriginalLenght != mCurrentLenght) {
			c.moveToPosition(mAfterCounter.getSelectedItemPosition());
			cv.put(RecordsDbHelper.TIMERSID, c.getInt(1));
			cv.put(RecordsDbHelper.STARTTIME, mCurrentStart + mCurrentLenght);
			if(mOriginalLenght!=0)
				cv.put(RecordsDbHelper.LENGHT, mOriginalStart + mOriginalLenght
					- (mCurrentStart + mCurrentLenght));
			else
				cv.put(RecordsDbHelper.LENGHT, 0);
			getActivity().getContentResolver().insert(
					RecordsDbHelper.CONTENT_URI_TIMES, cv);
			cv.clear();
			if(mOriginalLenght == 0) {
				cv.put(RecordsDbHelper.ISRUNNING, 1);
				getActivity().getContentResolver().update(
						RecordsDbHelper.CONTENT_URI_TIMERS, cv, RecordsDbHelper.ID + " = ?", new String[] { String.valueOf(c.getInt(1)) });
			}
		}
	}

	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}

	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
	}

	public void setValues(int id, int idRecord, long start, long lenght) {
		mIDtimer = id;
		mIDrecord = idRecord;
		mOriginalStart = start;
		mOriginalLenght = lenght;

		mCurrentStart = start;
		mCurrentLenght = lenght;
	}

	public static class CustomDateTimePickerFragment extends DialogFragment
			implements ICustomDateTimeListener {
		private IdateChange mIdateChange;
		private int id;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			id = getArguments().getInt("id");
			CustomDateTimePicker customDateTimePicker = new CustomDateTimePicker(
					getActivity(), this, getArguments().getLong("time"));
			return customDateTimePicker;
		}

		public void setDateChange(IdateChange idc) {
			mIdateChange = idc;
		}

		@Override
		public void onSet(Dialog dialog, Calendar calendarSelected,
				Date dateSelected, int year, String monthFullName,
				String monthShortName, int monthNumber, int date,
				String weekDayFullName, String weekDayShortName, int hour24,
				int hour12, int min, int sec, String AM_PM) {
			mIdateChange.timeChange(id, dateSelected.getTime());
			super.dismiss();
		}

		@Override
		public void onCancel() {
			super.dismiss();
		}
	}

	@Override
	public void timeChange(int id, long newvalue) {
		int result = 0;
		if (id == R.id.current_startdatetime) {
			mCurrentStart = newvalue;
			result = validateStartValue();
		} else if (id == R.id.current_stopdatetime) {
			mCurrentLenght = newvalue - mCurrentStart;
			result = validateStopValue();
		}
		switch (result) {
		case 0:
			break;
		case 1:
			Toast.makeText(getActivity(), getString(R.string.more_max),
					Toast.LENGTH_SHORT).show();
			break;
		case -1:
			Toast.makeText(getActivity(), getString(R.string.less_min),
					Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
	}

	private int validateStartValue() {
		long lenght;
		if(mOriginalLenght == 0 )
			lenght = new Date().getTime() - mOriginalStart;
		else
			lenght = mOriginalLenght;
		mCurrentLenght = mOriginalStart + lenght - mCurrentStart;
		if (mCurrentStart > mOriginalStart) {
			if (mCurrentStart > mOriginalStart + lenght) {
				mBeforeLayout.setVisibility(View.GONE);
				mCurrentStart = mOriginalStart;
				mCurrentLenght = mOriginalLenght;
				return 1;
				
			} else
				mBeforeLayout.setVisibility(View.VISIBLE);
		} else {
			mBeforeLayout.setVisibility(View.GONE);
			mCurrentStart = mOriginalStart;
			mCurrentLenght = mOriginalLenght;
			return -1;
			
		}
		mCalendar.setTimeInMillis(mOriginalStart);
		StringBuilder sb = new StringBuilder(
				mFormatterDateTime.format(mCalendar.getTime()));
		sb.append(" - ");
		mCalendar.setTimeInMillis(mCurrentStart);
		mCurrentStartDateTime.setText(mFormatterDateTime.format(mCalendar
				.getTime()));
		sb.append(mCurrentStartDateTime.getText());
		mBeforeText.setText(sb.toString());
		return 0;
	}
	
	private int validateStopValue(){
		long lenght;
		if(mOriginalLenght == 0 )
			lenght = new Date().getTime() - mOriginalStart;
		else
			lenght = mOriginalLenght;
		if (mCurrentStart + mCurrentLenght < mOriginalStart
				+ lenght) {
			if (mCurrentStart + mCurrentLenght < mOriginalStart) {
				mAfterLayout.setVisibility(View.GONE);
				mCurrentLenght = mOriginalStart + mOriginalLenght
						- mCurrentStart;
				return -1;
			} else
				mAfterLayout.setVisibility(View.VISIBLE);
		} else {
			mAfterLayout.setVisibility(View.GONE);
			mCurrentLenght = mOriginalStart + mOriginalLenght
					- mCurrentStart;
			return 1;
		}
		mCalendar.setTimeInMillis(mCurrentStart + mCurrentLenght);
		mCurrentStopDateTime.setText(mFormatterDateTime.format(mCalendar
				.getTime()));
		StringBuilder sb = new StringBuilder(
				mFormatterDateTime.format(mCalendar.getTime()));
		sb.append(" - ");
		mCalendar.setTimeInMillis(mOriginalStart + mOriginalLenght);
		if(mOriginalLenght == 0)
			sb.append(getString(R.string.now));
		else
			sb.append(mFormatterDateTime.format(mCalendar.getTime()));
		mAfterText.setText(sb.toString());
		return 0;
	}
}