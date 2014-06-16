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

public class StartDateSetDialogFragment extends DialogFragment implements
		IdateChange {

	private IRecordDialog mListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
		String[] items = getResources().getStringArray(R.array.StartFilters);
		long selectItem = PreferenceManager.getDefaultSharedPreferences(
				StartDateSetDialogFragment.this.getActivity()).getLong(
				SettingsActivity.STARTTIMEFILTER, 5);
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
									Editor editor = PreferenceManager
											.getDefaultSharedPreferences(
													StartDateSetDialogFragment.this
															.getActivity())
											.edit();
									editor.putLong(
											SettingsActivity.STARTTIMEFILTER,
											which);
									editor.commit();
									StartDateSetDialogFragment.this.dismiss();
									mListener.onRefreshFragmentsValue();
								} else {
									CustomDateTimePickerFragment newFragment = new CustomDateTimePickerFragment();
									Bundle b = new Bundle();
									b.putLong("time", startdate.getTime());
									b.putInt("id", 1);
									newFragment.setArguments(b);
									newFragment
											.setDateChange(StartDateSetDialogFragment.this);
									newFragment.show(getActivity()
											.getSupportFragmentManager(),
											"timePicker");
								}
							}
						}).create();
	}

	@Override
	public void timeChange(int id, long newvalue) {
		if (newvalue > new Date().getTime()) {
			Toast.makeText(getActivity(), R.string.more_max, Toast.LENGTH_LONG).show();
			StartDateSetDialogFragment.this.dismiss();
		} else {
			Editor editor = PreferenceManager.getDefaultSharedPreferences(
					StartDateSetDialogFragment.this.getActivity()).edit();
			editor.putLong(SettingsActivity.STARTTIMEFILTER, newvalue);
			editor.commit();
			StartDateSetDialogFragment.this.dismiss();
			mListener.onRefreshFragmentsValue();
		}
	}
}
