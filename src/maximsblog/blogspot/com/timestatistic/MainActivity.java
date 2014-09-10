package maximsblog.blogspot.com.timestatistic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import maximsblog.blogspot.com.timestatistic.CounterEditorDialogFragment.ICounterEditorDialog;
import maximsblog.blogspot.com.timestatistic.CounterEditorDialogFragment.Status;
import maximsblog.blogspot.com.timestatistic.AreYouSureResetAllDialogFragment.ResetAllDialog;
import maximsblog.blogspot.com.timestatistic.MainActivity.MainFragments;
import maximsblog.blogspot.com.timestatistic.MainActivity.PagesAdapter;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.TabPageIndicator;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.CursorAdapter;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.actionbarsherlock.widget.SearchView.OnSuggestionListener;

public class MainActivity extends SherlockFragmentActivity implements
		ResetAllDialog, ICounterEditorDialog, OnPageChangeListener,
		IRecordDialog, OnQueryTextListener, OnSuggestionListener {

	private String[] mTitles;
	private FragmentPagerAdapter adapter;
	private ViewPager pager;
	private SearchView mSearchView;
	private int[] mIcons;

	public interface MainFragments {
		void onReload();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("");
		setContentView(R.layout.activity_main);
		mTitles = getResources().getStringArray(R.array.TitlePages);
		mIcons = new int[] {R.drawable.ic_counter_title, R.drawable.ic_interval_title, R.drawable.ic_diagram_title, R.drawable.ic_diary_title};
		// prepare ViewPagerIndicator
		adapter = new PagesAdapter(getSupportFragmentManager());
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setOffscreenPageLimit(3);// all fragments upload, fix not switch counters
		pager.setAdapter(adapter);
		TabPageIndicator  indicator = (TabPageIndicator ) findViewById(R.id.indicator);
		indicator.setViewPager(pager);
		indicator.setOnPageChangeListener(this);
		if (savedInstanceState == null) {

		} else {
			FragmentManager fm = getSupportFragmentManager();
			SplitRecordDialogFragment splitRecordDialog = (SplitRecordDialogFragment) fm
					.findFragmentByTag("mSplitRecordDialog");
			if (splitRecordDialog != null)
				splitRecordDialog.setCounterDialogListener(this);
			CounterEditorDialogFragment counterEditorDialogFragment = (CounterEditorDialogFragment) fm
					.findFragmentByTag("mCounterEditorDialogFragment");
			if (counterEditorDialogFragment != null)
				counterEditorDialogFragment.setCounterDialogListener(this);
			AreYouSureResetAllDialogFragment areYouSureResetAllDialog = (AreYouSureResetAllDialogFragment) fm
					.findFragmentByTag("mAreYouSureResetAllDialog");
			if (areYouSureResetAllDialog != null)
				areYouSureResetAllDialog.setResetAllDialogListener(this);
			UnionRecordDialogFragment unionRecordDialogFragment = (UnionRecordDialogFragment) fm
					.findFragmentByTag("mUnionRecordDialog");
			if (unionRecordDialogFragment != null)
				unionRecordDialogFragment.setDialogListener(this);
			FilterDateSetDialogFragment startDateSetDialogFragment = (FilterDateSetDialogFragment) fm
					.findFragmentByTag("mStartDateSetDialogFragment");
			if (startDateSetDialogFragment != null)
				startDateSetDialogFragment.setDialogListener(this);
			DiaryEditorDialogFragment diaryEditorDialogFragment = (DiaryEditorDialogFragment) fm
					.findFragmentByTag("mDiaryEditorDialogFragment");
			if (diaryEditorDialogFragment != null)
				diaryEditorDialogFragment.setCounterDialogListener(this);
		}
	}

	public Fragment findFragmentByPosition(int position) {

		return getSupportFragmentManager().findFragmentByTag(
				"android:switcher:" + pager.getId() + ":"
						+ adapter.getItemId(position));
	}

	class PagesAdapter extends FragmentPagerAdapter implements IconPagerAdapter  {
		public PagesAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment f;
			if (position == 0) {
				CountersFragment fg = CountersFragment.newInstance();
				f = fg;
			} else if (position == 1) {
				TimeRecordsFragment fg = TimeRecordsFragment.newInstance();
				f = fg;
			} else if (position == 2) {
				DiagramFragment fg = DiagramFragment.newInstance();
				f = fg;
			} else {
				DiaryFragment fg = DiaryFragment.newInstance();
				f = fg;
			}
			return f;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mTitles[position % mTitles.length];
		}

		@Override
		public int getCount() {
			return mTitles.length;
		}

		@Override
		public int getIconResId(int index) {
			
			return mIcons[index];
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main_activity, menu);
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		MenuItem searchMenuItem = ((MenuItem) menu.findItem(R.id.item_search));
		mSearchView = (SearchView) searchMenuItem.getActionView();
		if (pager.getCurrentItem() == 3) {
			getSupportActionBar().setTitle("");
			getSupportActionBar().setSubtitle("");
			searchMenuItem.setVisible(true);
		} else {
			SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(
					"dd/MM/yy HH:mm");
			FilterDateOption startDateOption = app.getStartDate(this);
			long startdate = startDateOption.date;
			if (!startDateOption.dateName.equals(getResources().getStringArray(
					R.array.StartFilters)[6]))
				getSupportActionBar().setTitle(startDateOption.dateName);
			else {

				getSupportActionBar()
						.setTitle(
								startDateOption.dateName
										+ " "
										+ mSimpleDateFormat.format(new Date(
												startdate)));
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

			searchMenuItem.setVisible(false);
			if (mSearchView.isShown())
				mSearchView.setIconified(true);
		}
		SearchableInfo info = searchManager
				.getSearchableInfo(getComponentName());
		mSearchView.setSearchableInfo(info);
		mSearchView.setOnQueryTextListener(this);
		mSearchView.setOnSuggestionListener(this);
		menu.findItem(R.id.item_add).setVisible(pager.getCurrentItem() == 0);
		menu.findItem(R.id.item_reset_all).setVisible(
				pager.getCurrentItem() == 0);
		menu.findItem(R.id.item_starts).setVisible(pager.getCurrentItem() != 3);
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
		case R.id.item_add:
			CounterEditorDialogFragment counterEditorDialogFragment = new CounterEditorDialogFragment();
			counterEditorDialogFragment.setIdCounter(-1);
			counterEditorDialogFragment.setCounterDialogListener(this);
			counterEditorDialogFragment.setName("");
			counterEditorDialogFragment.setInterval(900000);
			counterEditorDialogFragment.setSortId(1);
			Random rnd = new Random();
			int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256),
					rnd.nextInt(256));
			counterEditorDialogFragment.setColor(color);
			counterEditorDialogFragment.show(this.getSupportFragmentManager(),
					"mCounterEditorDialogFragment");
			break;
		case R.id.item_reset_all:
			ft = getSupportFragmentManager().beginTransaction();
			AreYouSureResetAllDialogFragment areYouSureResetAllDialog = new AreYouSureResetAllDialogFragment();
			areYouSureResetAllDialog.setResetAllDialogListener(this);
			areYouSureResetAllDialog.show(ft, "mAreYouSureResetAllDialog");
			break;
		case R.id.item_about:
			ft = getSupportFragmentManager().beginTransaction();
			AboutDialogFragment aboutFragment = new AboutDialogFragment();
			aboutFragment.show(ft, "aboutDialog");
			break;
		case R.id.action_settings: {
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
		}
			break;
		case R.id.item_help: {
			Intent i = new Intent(this, HelpActivity.class);
			startActivity(i);
			break;
		}
		case R.id.item_periods: {
			Intent i = new Intent(this, PeriodAnalyseActivity.class);
			startActivity(i);
			break;
		}
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResetAllDialog() {
		getContentResolver().delete(RecordsDbHelper.CONTENT_URI_RESETCOUNTERS,
				null, null);
		reloadFragments();
	}

	@Override
	public void onFinishDialog(String inputText, int id, Status status,
			boolean isRunning, int color, long interval, int sortid) {
		if (status == Status.ADD) {
			ContentValues cv = new ContentValues();
			cv.put(RecordsDbHelper.NAME, inputText);
			cv.put(RecordsDbHelper.COLOR, color);
			cv.put(RecordsDbHelper.INTERVAL, interval);
			cv.put(RecordsDbHelper.SORTID, sortid);
			Uri row = getContentResolver().insert(
					RecordsDbHelper.CONTENT_URI_TIMERS, cv);
			int iDcounters = Integer.valueOf(row.getLastPathSegment());
			cv.clear();
			cv.put(RecordsDbHelper.TIMERSID, iDcounters);
			getContentResolver().insert(RecordsDbHelper.CONTENT_URI_TIMES, cv);
			reloadFragments();
		} else if (status == Status.EDIT) {
			Cursor c = getContentResolver()
					.query(RecordsDbHelper.CONTENT_URI_TIMERS,
							new String[] { RecordsDbHelper.ID,
									RecordsDbHelper.SORTID },
							RecordsDbHelper.SORTID + " >= ?",
							new String[] { String.valueOf(sortid) },
							RecordsDbHelper.SORTID);
			ContentValues cv = new ContentValues();
			if (c.getCount() > 0) {
				c.moveToFirst();
				
				int index = sortid + 1;
				do {
					cv.clear();
					cv.put(RecordsDbHelper.SORTID, index);
					index++;
					getContentResolver().update(
							RecordsDbHelper.CONTENT_URI_RENAMECOUNTER, cv,
							RecordsDbHelper.ID + "=?",
							new String[] { String.valueOf(c.getInt(0)) });
				} while (c.moveToNext());
			}
			c.close();
			cv.clear();
			cv.put(RecordsDbHelper.NAME, inputText);
			cv.put(RecordsDbHelper.COLOR, color);
			cv.put(RecordsDbHelper.INTERVAL, interval);
			cv.put(RecordsDbHelper.SORTID, sortid);
			getContentResolver().update(
					RecordsDbHelper.CONTENT_URI_RENAMECOUNTER, cv,
					RecordsDbHelper.ID + "=?",
					new String[] { String.valueOf(id) });
			app.loadRunningCounterAlarm(getApplicationContext());
			reloadFragments();
		} else if (status == Status.DEL) {
			if (isRunning) {
				ContentValues cv = new ContentValues();
				cv.put(RecordsDbHelper.ISRUNNING, 1);
				getContentResolver().update(RecordsDbHelper.CONTENT_URI_TIMERS,
						cv, RecordsDbHelper.ID + " = ?",
						new String[] { String.valueOf(1) });
				app.loadRunningCounterAlarm(getApplicationContext());
			}
			getContentResolver().delete(RecordsDbHelper.CONTENT_URI_TIMERS,
					null, new String[] { String.valueOf(id) });
			reloadFragments();
		}
	}

	private void reloadFragments() {
		((MainFragments) findFragmentByPosition(0)).onReload();
		TimeRecordsFragment timeRecordsFragment = (TimeRecordsFragment) ((MainFragments) findFragmentByPosition(1));
		timeRecordsFragment.setNormalMode();
		timeRecordsFragment.onReload();
		DiagramFragment diagramFragment = (DiagramFragment) ((MainFragments) findFragmentByPosition(2));
		if (diagramFragment != null)
			diagramFragment.onReload();
		DiaryFragment diaryFragment = (DiaryFragment) ((MainFragments) findFragmentByPosition(3));
		if (diaryFragment != null)
			diaryFragment.onReload();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int position) {
		if (position == 2 || position == 1 || position == 3)
			((MainFragments) findFragmentByPosition(position)).onReload();
		supportInvalidateOptionsMenu();
	}

	@Override
	public void onRefreshFragmentsValue() {
		reloadFragments();
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		DiaryFragment diaryFragment = (DiaryFragment) ((MainFragments) findFragmentByPosition(3));
		if (diaryFragment != null) {
			diaryFragment.setFilter(query);
			diaryFragment.onReload();
		}
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		if (newText.length() == 0) {
			DiaryFragment diaryFragment = (DiaryFragment) ((MainFragments) findFragmentByPosition(3));
			if (diaryFragment != null) {
				diaryFragment.setFilter(newText);
				diaryFragment.onReload();
			}
		}
		return false;
	}

	@Override
	public boolean onSuggestionSelect(int position) {
		Cursor c = mSearchView.getSuggestionsAdapter().getCursor();
		c.moveToPosition(position);
		mSearchView.setQuery(c.getString(c
				.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)), true);
		return false;
	}

	@Override
	public boolean onSuggestionClick(int position) {
		Cursor c = mSearchView.getSuggestionsAdapter().getCursor();
		c.moveToPosition(position);
		mSearchView.setQuery(c.getString(c
				.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)), true);
		return false;
	}

	@Override
	public void onDiaryFragmentsRefresh() {
		DiaryFragment diaryFragment = (DiaryFragment) ((MainFragments) findFragmentByPosition(3));
		diaryFragment.onReload();
	}

	@Override
	public void onFilterDateSet(long startdate, long enddate) {
		String setting = SettingsActivity.STARTTIMEFILTER;
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putLong(setting, startdate);
		setting = SettingsActivity.ENDTIMEFILTER;
		editor.putLong(setting, enddate);
		editor.commit();
		reloadFragments();
	}

}
