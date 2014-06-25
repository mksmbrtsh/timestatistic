package maximsblog.blogspot.com.timestatistic;

import java.text.SimpleDateFormat;
import java.util.Date;

import maximsblog.blogspot.com.timestatistic.SplitRecordDialogFragment.CustomDateTimePickerFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

public class FilterDateSetDialogFragment extends DialogFragment implements
		IdateChange {

	private IRecordDialog mListener;
	private boolean mStart;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mStart = getArguments().getBoolean("start");
		if (savedInstanceState != null) {
			CustomDateTimePickerFragment customDateTimePickerFragment = (CustomDateTimePickerFragment) getActivity()
					.getSupportFragmentManager()
					.findFragmentByTag("timePicker");
			if (customDateTimePickerFragment != null) {
				customDateTimePickerFragment.setDateChange(this);
			}
		} else {

		}

	};

	public void setDialogListener(IRecordDialog listener) {
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
		if(mStart)
			selectItem = PreferenceManager.getDefaultSharedPreferences(
				FilterDateSetDialogFragment.this.getActivity()).getLong(
				SettingsActivity.STARTTIMEFILTER, 5);
		else
			selectItem = PreferenceManager.getDefaultSharedPreferences(
					FilterDateSetDialogFragment.this.getActivity()).getLong(
					SettingsActivity.ENDTIMEFILTER, 5);
		final Date startdate;
		if (selectItem < 6) {
			startdate = new Date();
		} else {
			startdate = new Date(selectItem);
			selectItem = 6;
		}
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(
				"dd/MM/yy HH:mm");
		items[6] = items[6] + " " + mSimpleDateFormat.format(startdate);
		final int checkedItem = (int) selectItem;
		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.startdateset)
				.setSingleChoiceItems(items, (int) checkedItem,
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (which < 6) {
									String setting;
									if(mStart)
										setting = SettingsActivity.STARTTIMEFILTER;
									else
										setting = SettingsActivity.ENDTIMEFILTER;
									Editor editor = PreferenceManager
											.getDefaultSharedPreferences(
													FilterDateSetDialogFragment.this
															.getActivity())
											.edit();
									editor.putLong(
											setting,
											which);
									editor.commit();
									FilterDateSetDialogFragment.this.dismiss();
									mListener.onRefreshFragmentsValue();
								} else {
									CustomDateTimePickerFragment newFragment = new CustomDateTimePickerFragment();
									Bundle b = new Bundle();
									b.putLong("time", startdate.getTime());
									b.putInt("id", 1);
									newFragment.setArguments(b);
									newFragment
											.setDateChange(FilterDateSetDialogFragment.this);
									newFragment.show(getActivity()
											.getSupportFragmentManager(),
											"timePicker");
								}
							}
						}).create();
	}

	@Override
	public void timeChange(int id, long newvalue) {
		if (mStart && newvalue > new Date().getTime()) {
			Toast.makeText(getActivity(), R.string.more_max, Toast.LENGTH_LONG).show();
			FilterDateSetDialogFragment.this.dismiss();
		} else {
			String setting;
			if(mStart)
				setting = SettingsActivity.STARTTIMEFILTER;
			else
				setting = SettingsActivity.ENDTIMEFILTER;
			Editor editor = PreferenceManager.getDefaultSharedPreferences(
					FilterDateSetDialogFragment.this.getActivity()).edit();
			editor.putLong(setting, newvalue);
			editor.commit();
			FilterDateSetDialogFragment.this.dismiss();
			mListener.onRefreshFragmentsValue();
		}
	}
}