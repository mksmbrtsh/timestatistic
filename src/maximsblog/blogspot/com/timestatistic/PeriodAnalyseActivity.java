package maximsblog.blogspot.com.timestatistic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import maximsblog.blogspot.com.timestatistic.PeriodSetupDialogFragment.IPeriodSetupDialog;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class PeriodAnalyseActivity extends SherlockFragmentActivity implements
		IRecordDialog, IPeriodSetupDialog {

	private static final String PERIOD = "period_interval";
	private long mPeriod;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("");
		if (savedInstanceState == null) {
			mPeriod = PreferenceManager.getDefaultSharedPreferences(
					this).getLong(PeriodAnalyseActivity.PERIOD, 1000*60*60);
			PeriodAnalyseFragment details = new PeriodAnalyseFragment(mPeriod);

			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, details).commit();
		} else {
			mPeriod = savedInstanceState.getLong("period");
			FragmentManager fm = getSupportFragmentManager();
			FilterDateSetDialogFragment startDateSetDialogFragment = (FilterDateSetDialogFragment) fm
					.findFragmentByTag("mStartDateSetDialogFragment");
			if (startDateSetDialogFragment != null)
				startDateSetDialogFragment.setDialogListener(this);
			PeriodSetupDialogFragment periodSetupDialogFragment = (PeriodSetupDialogFragment) fm
					.findFragmentByTag("periodSetupDialogFragment");
			if (periodSetupDialogFragment != null)
				periodSetupDialogFragment.setPeriodSetupDialog(this);
		}
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("period", mPeriod);
		super.onSaveInstanceState(outState);
	};

	@Override
	public void onRefreshFragmentsValue() {
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(
				"dd/MM/yy HH:mm");
		FilterDateOption startDateOption = app.getStartDate(this);
		long startdate = startDateOption.date;
		if(!startDateOption.dateName.equals(getResources().getStringArray(R.array.StartFilters)[6]))
			((SherlockFragmentActivity)this).getSupportActionBar().setTitle(startDateOption.dateName);
		else {
			((SherlockFragmentActivity)this).getSupportActionBar().setTitle(startDateOption.dateName + " " + mSimpleDateFormat.format(new Date(startdate)));
		}
		startDateOption = app.getEndDate(this);
		long enddate = startDateOption.date;
		if(!startDateOption.dateName.equals(getResources().getStringArray(R.array.EndFilters)[6]))
			((SherlockFragmentActivity)this).getSupportActionBar().setSubtitle(startDateOption.dateName);
		else {
			((SherlockFragmentActivity)this).getSupportActionBar().setSubtitle(startDateOption.dateName + " " + mSimpleDateFormat.format(new Date(enddate)));
		}
		mPeriod = PreferenceManager.getDefaultSharedPreferences(
				this).getLong(PeriodAnalyseActivity.PERIOD, 1000*60*60);
		PeriodAnalyseFragment details = new PeriodAnalyseFragment(mPeriod);
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
		FilterDateOption startDateOption = app.getStartDate(this);
		long startdate = startDateOption.date;
		if (!startDateOption.dateName.equals(getResources().getStringArray(
				R.array.StartFilters)[6]))
			getSupportActionBar().setTitle(startDateOption.dateName);
		else {

			getSupportActionBar().setTitle(
					startDateOption.dateName + " "
							+ mSimpleDateFormat.format(new Date(startdate)));
		}
		FilterDateOption endDateOption = app.getEndDate(this);
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
		case R.id.item_starts:
			FilterDateSetDialogFragment startDateSetDialogFragment = new FilterDateSetDialogFragment();
			startDateSetDialogFragment.setDialogListener(this);
			startDateSetDialogFragment.show(this.getSupportFragmentManager(),
					"mStartDateSetDialogFragment");
			break;
		case R.id.item_interval:
			PeriodSetupDialogFragment periodSetupDialogFragment = new PeriodSetupDialogFragment();
			periodSetupDialogFragment.setPeriodSetupDialog(this);
			periodSetupDialogFragment.show(this.getSupportFragmentManager(),
					"periodSetupDialogFragment");
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void setupNewPeriod(long time) {
		mPeriod = time;
		PeriodAnalyseFragment details = new PeriodAnalyseFragment(mPeriod);
		getSupportFragmentManager().beginTransaction()
				.replace(android.R.id.content, details).commit();
	}

}
