package maximsblog.blogspot.com.timestatistic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import maximsblog.blogspot.com.timestatistic.SplitRecordDialogFragment.CustomDateTimePickerFragment;

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
				"dd/MM/yy HH:mm");
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
									mListener.timeChange(mStart ? 1: 2, which);
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
	public void timeChange(int id, long newvalue) {
		if (mStart && newvalue > new Date().getTime()) {
			Toast.makeText(getActivity(), R.string.more_max, Toast.LENGTH_LONG).show();
			FilterDialogFragment.this.dismiss();
		} else {
			FilterDialogFragment.this.dismiss();
			mListener.timeChange(mStart ? 1: 2, newvalue);
		}
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}
}
