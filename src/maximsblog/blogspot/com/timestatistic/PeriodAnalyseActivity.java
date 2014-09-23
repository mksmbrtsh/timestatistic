package maximsblog.blogspot.com.timestatistic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import maximsblog.blogspot.com.timestatistic.PeriodSetupDialogFragment.IPeriodSetupDialog;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class PeriodAnalyseActivity extends SherlockFragmentActivity
		implements
		IRecordDialog,
		IPeriodSetupDialog,
		maximsblog.blogspot.com.timestatistic.CountersPeriodSetupDialogFragment.IPeriodSetupDialog {

	public static final String PERIOD = "period_interval";
	public static final String IDS = "ids";
	public static final String CHECKED = "selected_ids";
	private long mPeriod;
	private int[] mIDs;
	private boolean[] mChecked;
	private AdView adView;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("");
		if (savedInstanceState == null) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			mPeriod = prefs.getLong(PeriodAnalyseActivity.PERIOD,
					1000 * 60 * 60);
			String s = prefs.getString(IDS, null);
			if (s != null) {
				String[] ids = s.split(";");
				mIDs = new int[ids.length];
				mChecked = new boolean[ids.length];
				for (int i = 0; i < ids.length; i++) {
					String[] split = ids[i].split("\\.");
					mIDs[i] = Integer.valueOf(split[0]);
					mChecked[i] = split[1].equals("1");
				}
			} else {
				Cursor newtimers = getContentResolver().query(
						RecordsDbHelper.CONTENT_URI_TIMES, null, null, null,
						RecordsDbHelper.SORTID);
				mIDs = new int[newtimers.getCount()];
				mChecked = new boolean[newtimers.getCount()];
				for (int i1 = 0, cnt1 = newtimers.getCount(); i1 < cnt1; i1++) {
					newtimers.moveToPosition(i1);
					mIDs[i1] = (newtimers.getInt(4));
					mChecked[i1] = true;
				}
				newtimers.close();
			}

			PeriodAnalyseFragment details = new PeriodAnalyseFragment();
			Bundle b = new Bundle();
			b.putLong(PERIOD, mPeriod);
			b.putIntArray(IDS, mIDs);
			b.putBooleanArray(CHECKED, mChecked);
			details.setArguments(b);
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, details).commit();

		} else {
			mPeriod = savedInstanceState.getLong("period");
			mIDs = savedInstanceState.getIntArray("ids");
			mChecked = savedInstanceState.getBooleanArray("checked");
			FragmentManager fm = getSupportFragmentManager();
			FilterDateSetDialogFragment startDateSetDialogFragment = (FilterDateSetDialogFragment) fm
					.findFragmentByTag("mStartDateSetDialogFragment");
			if (startDateSetDialogFragment != null)
				startDateSetDialogFragment.setDialogListener(this);
			PeriodSetupDialogFragment periodSetupDialogFragment = (PeriodSetupDialogFragment) fm
					.findFragmentByTag("periodSetupDialogFragment");
			if (periodSetupDialogFragment != null)
				periodSetupDialogFragment.setPeriodSetupDialog(this);
			CountersPeriodSetupDialogFragment countersPeriodSetupDialogFragment = (CountersPeriodSetupDialogFragment) fm
					.findFragmentByTag("countersPeriodSetupDialogFragment");
			if (countersPeriodSetupDialogFragment != null)
				countersPeriodSetupDialogFragment.setPeriodSetupDialog(this);
		}
		
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("period", mPeriod);
		outState.putBooleanArray("checked", mChecked);
		outState.putIntArray("ids", mIDs);
		super.onSaveInstanceState(outState);
	};

	@Override
	public void onRefreshFragmentsValue() {
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(
				"dd/MM/yy HH:mm");
		FilterDateOption startDateOption = app.getStartDate(this);
		long startdate = startDateOption.date;
		if (!startDateOption.dateName.equals(getResources().getStringArray(
				R.array.StartFilters)[6]))
			((SherlockFragmentActivity) this).getSupportActionBar().setTitle(
					startDateOption.dateName);
		else {
			((SherlockFragmentActivity) this).getSupportActionBar().setTitle(
					startDateOption.dateName + " "
							+ mSimpleDateFormat.format(new Date(startdate)));
		}
		startDateOption = app.getEndDate(this);
		long enddate = startDateOption.date;
		if (!startDateOption.dateName.equals(getResources().getStringArray(
				R.array.EndFilters)[6]))
			((SherlockFragmentActivity) this).getSupportActionBar()
					.setSubtitle(startDateOption.dateName);
		else {
			((SherlockFragmentActivity) this).getSupportActionBar()
					.setSubtitle(
							startDateOption.dateName
									+ " "
									+ mSimpleDateFormat
											.format(new Date(enddate)));
		}
		mPeriod = PreferenceManager.getDefaultSharedPreferences(this).getLong(
				PeriodAnalyseActivity.PERIOD, 1000 * 60 * 60);
		PeriodAnalyseFragment details = new PeriodAnalyseFragment();
		Bundle b = new Bundle();
		b.putLong(PERIOD, mPeriod);
		b.putIntArray(IDS, mIDs);
		b.putBooleanArray(CHECKED, mChecked);
		details.setArguments(b);
		getSupportFragmentManager().beginTransaction()
				.replace(android.R.id.content, details).commit();
	}

	@Override
	public void onDiaryFragmentsRefresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.period_activity, menu);
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		MenuItem searchMenuItem = ((MenuItem) menu.findItem(R.id.item_search));
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(
				"dd/MM/yy HH:mm");
		FilterDateOption startDateOption = app.getStartDatePeriod(this);
		long startdate = startDateOption.date;
		if (!startDateOption.dateName.equals(getResources().getStringArray(
				R.array.StartFilters)[6]))
			getSupportActionBar().setTitle(startDateOption.dateName);
		else {

			getSupportActionBar().setTitle(
					startDateOption.dateName + " "
							+ mSimpleDateFormat.format(new Date(startdate)));
		}
		FilterDateOption endDateOption = app.getEndDatePeriod(this);
		long endDate = endDateOption.date;
		if (!endDateOption.dateName.equals(getResources().getStringArray(
				R.array.EndFilters)[6]))
			getSupportActionBar().setSubtitle(endDateOption.dateName);
		else {
			getSupportActionBar().setSubtitle(
					endDateOption.dateName + " "
							+ mSimpleDateFormat.format(new Date(endDate)));
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		FragmentTransaction ft;
		switch (item.getItemId()) {
		case R.id.item_starts: {
			FilterDateSetDialogFragment startDateSetDialogFragment = new FilterDateSetDialogFragment();
			Bundle args = new Bundle();
			long selectStartItem = PreferenceManager.getDefaultSharedPreferences(
					this).getLong(
					SettingsActivity.STARTTIMEFILTERPERIOD, 5);
			long selectEndItem = PreferenceManager.getDefaultSharedPreferences(
					this).getLong(
					SettingsActivity.ENDTIMEFILTERPERIOD, 5);
			args.putLong("start", selectStartItem);
			args.putLong("stop", selectEndItem);
			startDateSetDialogFragment.setArguments(args);
			startDateSetDialogFragment.setDialogListener(this);
			startDateSetDialogFragment.show(this.getSupportFragmentManager(),
					"mStartDateSetDialogFragment");
		}
			break;
		case R.id.item_interval:
			PeriodSetupDialogFragment periodSetupDialogFragment = new PeriodSetupDialogFragment();
			Bundle b = new Bundle();
			b.putLong(PERIOD, mPeriod);
			periodSetupDialogFragment.setArguments(b);
			periodSetupDialogFragment.setPeriodSetupDialog(this);
			periodSetupDialogFragment.show(this.getSupportFragmentManager(),
					"periodSetupDialogFragment");
			break;
		case R.id.item_counters: {
			CountersPeriodSetupDialogFragment countersPeriodSetupDialogFragment = new CountersPeriodSetupDialogFragment();
			countersPeriodSetupDialogFragment.setPeriodSetupDialog(this);
			Bundle args = new Bundle();
			args.putBooleanArray("checked", mChecked);
			args.putIntArray("ids", mIDs);
			countersPeriodSetupDialogFragment.setArguments(args);
			countersPeriodSetupDialogFragment.show(
					this.getSupportFragmentManager(),
					"countersPeriodSetupDialogFragment");
		}
			break;
		case R.id.item_pro: {
			final String appPackageName = "maximsblog.blogspot.com.timestatistic.pro";
			try {
			    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
			} catch (android.content.ActivityNotFoundException anfe) {
			    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
			}
		}
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void setupNewPeriod(long time) {
		mPeriod = time;
		PreferenceManager.getDefaultSharedPreferences(this).edit()
				.putLong(PERIOD, mPeriod).commit();
		PeriodAnalyseFragment details = new PeriodAnalyseFragment();
		Bundle b = new Bundle();
		b.putLong(PERIOD, mPeriod);
		b.putIntArray(IDS, mIDs);
		b.putBooleanArray(CHECKED, mChecked);
		details.setArguments(b);
		getSupportFragmentManager().beginTransaction()
				.replace(android.R.id.content, details).commit();
	}

	@Override
	public void setupCounters(int[] ids, boolean[] checked) {
		mIDs = ids;
		mChecked = checked;
		Editor edit = PreferenceManager.getDefaultSharedPreferences(this)
				.edit();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ids.length; i++) {
			sb.append(mIDs[i]);
			sb.append('.');
			if (mChecked[i])
				sb.append('1');
			else
				sb.append('0');
			sb.append(';');
		}
		sb.deleteCharAt(sb.length() - 1);
		edit.putString(IDS, sb.toString());
		edit.commit();

		PeriodAnalyseFragment details = new PeriodAnalyseFragment();
		Bundle b = new Bundle();
		b.putLong(PERIOD, mPeriod);
		b.putIntArray(IDS, mIDs);
		b.putBooleanArray(CHECKED, mChecked);
		details.setArguments(b);
		getSupportFragmentManager().beginTransaction()
				.replace(android.R.id.content, details).commit();
	}

	@Override
	public void onFilterDateSet(long startdate, long enddate) {
		String setting = SettingsActivity.STARTTIMEFILTERPERIOD;
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this)
				.edit();
		editor.putLong(setting, startdate);
		setting = SettingsActivity.ENDTIMEFILTERPERIOD;
		editor.putLong(setting, enddate);
		editor.commit();

		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(
				"dd/MM/yy HH:mm");
		FilterDateOption startDateOption = app.getStartDatePeriod(this);
		startdate = startDateOption.date;
		if (!startDateOption.dateName.equals(getResources().getStringArray(
				R.array.StartFilters)[6]))
			((SherlockFragmentActivity) this).getSupportActionBar().setTitle(
					startDateOption.dateName);
		else {
			((SherlockFragmentActivity) this).getSupportActionBar().setTitle(
					startDateOption.dateName + " "
							+ mSimpleDateFormat.format(new Date(startdate)));
		}
		startDateOption = app.getEndDatePeriod(this);
		enddate = startDateOption.date;
		if (!startDateOption.dateName.equals(getResources().getStringArray(
				R.array.EndFilters)[6]))
			((SherlockFragmentActivity) this).getSupportActionBar()
					.setSubtitle(startDateOption.dateName);
		else {
			((SherlockFragmentActivity) this).getSupportActionBar()
					.setSubtitle(
							startDateOption.dateName
									+ " "
									+ mSimpleDateFormat
											.format(new Date(enddate)));
		}
		mPeriod = PreferenceManager.getDefaultSharedPreferences(this).getLong(
				PeriodAnalyseActivity.PERIOD, 1000 * 60 * 60);
		PeriodAnalyseFragment details = new PeriodAnalyseFragment();
		Bundle b = new Bundle();
		b.putLong(PERIOD, mPeriod);
		b.putIntArray(IDS, mIDs);
		b.putBooleanArray(CHECKED, mChecked);
		details.setArguments(b);
		getSupportFragmentManager().beginTransaction()
				.replace(android.R.id.content, details).commit();
	}
}
