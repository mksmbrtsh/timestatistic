package maximsblog.blogspot.com.timestatistic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FilterDialogFragment  extends DialogFragment implements IdateChange  {
	private IdateChange mListener;
	private boolean mStart;
	private long mSelectItem;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mStart = getArguments().getBoolean("start");
		mSelectItem = getArguments().getLong("f");
		if (savedInstanceState != null) {
			CustomDateTimePickerFragment customDateTimePickerFragment = (CustomDateTimePickerFragment) getActivity()
					.getFragmentManager()
					.findFragmentByTag("timePicker");
			if (customDateTimePickerFragment != null) {
				customDateTimePickerFragment.setDateChange(this);
			}
		} else {

		}

	};

	public void setDialogListener(IdateChange listener) {
		mListener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String[] items;
		if(mStart)
			items = getResources().getStringArray(R.array.StartFilters);
		else
			items = getResources().getStringArray(R.array.EndFilters);
		long selectItem;
		final Date startdate;
		if (mSelectItem < 6) {
			startdate = new Date();
		} else {
			startdate = new Date(mSelectItem);
			selectItem = 6;
		}
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(
				"dd.MM.yy HH:mm");
		items[6] = items[6] + " " + mSimpleDateFormat.format(startdate);
		final int checkedItem = (int) mSelectItem;
		return new AlertDialog.Builder(getActivity())
				.setTitle("")
				.setSingleChoiceItems(items, (int) checkedItem,
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (which < 6) {
									FilterDialogFragment.this.dismiss();
									mListener.timeChange(which);
								} else {
									CustomDateTimePickerFragment newFragment = new CustomDateTimePickerFragment();
									Bundle b = new Bundle();
									b.putLong("time", startdate.getTime());
									b.putInt("id", 1);
									newFragment.setArguments(b);
									newFragment
											.setDateChange(FilterDialogFragment.this);
									newFragment.show(getActivity()
											.getFragmentManager(),
											"timePicker");
								}
							}
						}).create();
	}

	@Override
	public void timeChange(long newvalue) {
		if (mStart && newvalue > new Date().getTime()) {
			Toast.makeText(getActivity(), R.string.more_max, Toast.LENGTH_LONG).show();
			FilterDialogFragment.this.dismiss();
		} else {
			FilterDialogFragment.this.dismiss();
			mListener.timeChange(newvalue);
		}
	}


	public static class CustomDateTimePickerFragment extends DialogFragment
			implements ICustomDateTimeListener {
		private IdateChange mIdateChange;
		private int id;

		private long start;
		private long end;

		@Override
		public void onSaveInstanceState(Bundle outState) {
			outState.putInt("id", id);
			outState.putLong("time",
					((CustomDateTimePicker) this.getDialog()).getTime());
			outState.putLong("start", start);
			outState.putLong("end", end);
			super.onSaveInstanceState(outState);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			long t;
			if (savedInstanceState == null) {
				id = getArguments().getInt("id");
				t = getArguments().getLong("time");
				start = getArguments().getLong("start");
				end = getArguments().getLong("end");
			} else {
				id = savedInstanceState.getInt("id");
				t = savedInstanceState.getLong("time");
				start = savedInstanceState.getLong("start");
				end = savedInstanceState.getLong("end");
			}
			CustomDateTimePicker customDateTimePicker = new CustomDateTimePicker(
					getActivity(), this, t, start, end);
			//customDateTimePicker.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
			super.dismiss();
			mIdateChange.timeChange(dateSelected.getTime());
		}

		@Override
		public void onCancel() {
			mIdateChange.cancel();
			super.dismiss();
		}
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}
}
