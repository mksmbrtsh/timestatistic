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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FilterDateSetDialogFragment extends DialogFragment implements
		IdateChange, android.view.View.OnClickListener, OnItemSelectedListener {

	private IRecordDialog mListener;
	private Spinner mStart;
	private Spinner mEnd;
	private Button mOk;
	private Button mCancel;
	private Date startdate;
	private Date enddate;
	private SimpleDateFormat mSimpleDateFormat;
	private long mSelectStartItem;
	private long mSelectEndItem;

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

	@Override
	public View onCreateView(android.view.LayoutInflater inflater,
			android.view.ViewGroup container, Bundle savedInstanceState) {
		getDialog().setTitle(R.string.filterdateset);
		View v = inflater.inflate(R.layout.fragment_filter_date_set_dialog,
				null);
		mStart = (Spinner) v.findViewById(R.id.spinner1);
		mEnd = (Spinner) v.findViewById(R.id.spinner2);
		mOk = (Button) v.findViewById(R.id.filter_ok);
		mCancel = (Button) v.findViewById(R.id.filter_cancel);
		mOk.setOnClickListener(this);
		mCancel.setOnClickListener(this);
		String[] items = getResources().getStringArray(R.array.StartFilters);
		mSelectStartItem = PreferenceManager.getDefaultSharedPreferences(
				FilterDateSetDialogFragment.this.getActivity()).getLong(
				SettingsActivity.STARTTIMEFILTER, 5);
		if (mSelectStartItem < 6) {
			startdate = new Date();
		} else {
			startdate = new Date(mSelectStartItem);
			mSelectStartItem = 6;
		}
		mSimpleDateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
		items[6] = items[6] + " " + mSimpleDateFormat.format(startdate);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mStart.setAdapter(adapter);
		mStart.setTag(1);
		mStart.setSelection((int) mSelectStartItem);
		mStart.setOnItemSelectedListener(this);
		items = getResources().getStringArray(R.array.EndFilters);
		mSelectEndItem = PreferenceManager.getDefaultSharedPreferences(
				FilterDateSetDialogFragment.this.getActivity()).getLong(
				SettingsActivity.ENDTIMEFILTER, 5);
		if (mSelectEndItem < 6) {
			enddate = new Date();
		} else {
			enddate = new Date(mSelectEndItem);
			mSelectEndItem = 6;
		}
		items[6] = items[6] + " " + mSimpleDateFormat.format(enddate);
		adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mEnd.setAdapter(adapter);
		mEnd.setTag(1);
		mEnd.setSelection((int) mSelectEndItem);
		mEnd.setOnItemSelectedListener(this);
		return v;
	};

	public void setDialogListener(IRecordDialog listener) {
		mListener = listener;
	}

	@Override
	public void timeChange(int id, long newvalue) {
		if (id == R.id.spinner1 && newvalue > new Date().getTime()) {
			Toast.makeText(getActivity(), R.string.more_max, Toast.LENGTH_LONG)
					.show();
			cancel();
		} else if (id == R.id.spinner1) {
			startdate = new Date(newvalue);
			String[] items = getResources()
					.getStringArray(R.array.StartFilters);
			items[6] = items[6] + " " + mSimpleDateFormat.format(startdate);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getActivity(), android.R.layout.simple_spinner_item, items);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mStart.setAdapter(adapter);
			mStart.setTag(1);
			mStart.setSelection(6);
		} else {
			enddate = new Date(newvalue);
			String[] items = getResources().getStringArray(R.array.EndFilters);
			items[6] = items[6] + " " + mSimpleDateFormat.format(enddate);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getActivity(), android.R.layout.simple_spinner_item, items);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mEnd.setAdapter(adapter);
			mEnd.setTag(1);
			mEnd.setSelection(6);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.filter_ok) {
			String setting = SettingsActivity.STARTTIMEFILTER;
			long newValue;
			if (mStart.getSelectedItemPosition() == 6)
				newValue = startdate.getTime();
			else
				newValue = mStart.getSelectedItemPosition();
			Editor editor = PreferenceManager.getDefaultSharedPreferences(
					FilterDateSetDialogFragment.this.getActivity()).edit();
			editor.putLong(setting, newValue);
			if (mEnd.getSelectedItemPosition() == 6)
				newValue = enddate.getTime();
			else
				newValue = mEnd.getSelectedItemPosition();
			setting = SettingsActivity.ENDTIMEFILTER;
			editor.putLong(setting, newValue);
			editor.commit();
			FilterDateSetDialogFragment.this.dismiss();
			mListener.onRefreshFragmentsValue();
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> spinner, View arg1, int which,
			long arg3) {
		if (spinner.getTag() != null) {
			spinner.setTag(null);
		} else if (which < 6) {

		} else {
			
			int id;
			if (spinner.getId() == R.id.spinner1) {
				id = R.id.spinner1;
			} else {
				id = R.id.spinner2;
			}
			CustomDateTimePickerFragment newFragment = new CustomDateTimePickerFragment();
			Bundle b = new Bundle();
			b.putLong("time", startdate.getTime());
			b.putInt("id", id);
			newFragment.setArguments(b);
			newFragment.setDateChange(FilterDateSetDialogFragment.this);
			newFragment.show(getActivity().getSupportFragmentManager(),
					"timePicker");
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> spinner) {

	}

	@Override
	public void cancel() {
		mStart.setTag(null);
		mEnd.setTag(null);
		mStart.setSelection((int) mSelectStartItem);
		mEnd.setSelection((int) mSelectEndItem);
	}
	
	
}
