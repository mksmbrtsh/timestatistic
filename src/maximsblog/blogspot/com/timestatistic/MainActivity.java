package maximsblog.blogspot.com.timestatistic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import maximsblog.blogspot.com.timestatistic.CounterEditorDialogFragment.ICounterEditorDialog;
import maximsblog.blogspot.com.timestatistic.CounterEditorDialogFragment.Status;
import maximsblog.blogspot.com.timestatistic.AreYouSureResetAllDialog.ResetAllDialog;
import maximsblog.blogspot.com.timestatistic.MainActivity.MainFragments;
import maximsblog.blogspot.com.timestatistic.MainActivity.PagesAdapter;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity implements
		ResetAllDialog, ICounterEditorDialog, OnPageChangeListener {

	public CounterEditorDialogFragment mAddCounterDialogFragment;

	private String[] mTitles;
	private PagesAdapter adapter;
	private ViewPager pager;

	public interface MainFragments {
		void onReload();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTitles = getResources().getStringArray(R.array.TitlePages);
		// prepare ViewPagerIndicator
		adapter = new PagesAdapter(getSupportFragmentManager());
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);
		TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(pager);
		indicator.setOnPageChangeListener(this);
		mAddCounterDialogFragment = new CounterEditorDialogFragment();
		mAddCounterDialogFragment.setCounterDialogListener(this);

	}

	public Fragment findFragmentByPosition(int position) {

		return getSupportFragmentManager().findFragmentByTag(
				"android:switcher:" + pager.getId() + ":"
						+ adapter.getItemId(position));
	}

	class PagesAdapter extends FragmentPagerAdapter {
		public PagesAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment f;
			if (position == 0) {
				CountersFragment fg = CountersFragment.newInstance();
				f = fg;
			} else if(position == 1) {
				TimeRecordsFragment fg = TimeRecordsFragment.newInstance();
				f = fg;
			} else {
				DiagramFragment fg = DiagramFragment.newInstance();
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

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main_activity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		FragmentTransaction ft;
		switch (item.getItemId()) {
		case R.id.item_add:
			mAddCounterDialogFragment.setIdCounter(-1);
			mAddCounterDialogFragment.setName("");
			Random rnd = new Random();
			int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256),
					rnd.nextInt(256));
			mAddCounterDialogFragment.setColor(color);
			mAddCounterDialogFragment.show(this.getSupportFragmentManager(),
					"dlg1");
			break;
		case R.id.item_reset_all: 
			ft = getSupportFragmentManager()
					.beginTransaction();
			AreYouSureResetAllDialog newFragment = new AreYouSureResetAllDialog();
			newFragment.setResetAllDialogListener(this);
			newFragment.show(ft, "dialog");
			
			break;
		case R.id.item_about:
			ft = getSupportFragmentManager()
					.beginTransaction();
			AboutDialog aboutFragment = new AboutDialog();
			aboutFragment.show(ft, "aboutDialog");
			break;
		case R.id.action_settings:
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
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
			boolean isRunning, int color) {
		if (status == Status.ADD) {
			ContentValues cv = new ContentValues();
			cv.put(RecordsDbHelper.NAME, inputText);
			cv.put(RecordsDbHelper.COLOR, color);
			Uri row = getContentResolver().insert(
					RecordsDbHelper.CONTENT_URI_TIMERS, cv);
			int iDcounters = Integer.valueOf(row.getLastPathSegment());
			cv.clear();
			cv.put(RecordsDbHelper.TIMERSID, iDcounters);
			getContentResolver().insert(RecordsDbHelper.CONTENT_URI_TIMES, cv);
			reloadFragments();
		} else if (status == Status.EDIT) {
			ContentValues cv = new ContentValues();
			cv.put(RecordsDbHelper.NAME, inputText);
			cv.put(RecordsDbHelper.COLOR, color);
			getContentResolver().update(
					RecordsDbHelper.CONTENT_URI_RENAMECOUNTER, cv,
					RecordsDbHelper.ID + "=?",
					new String[] { String.valueOf(id) });
			reloadFragments();
		} else if (status == Status.DEL) {
			if (isRunning) {
				ContentValues cv = new ContentValues();
				cv.put(RecordsDbHelper.ISRUNNING, 1);
				getContentResolver().update(RecordsDbHelper.CONTENT_URI_TIMERS,
						cv, RecordsDbHelper.ID + " = ?",
						new String[] { String.valueOf(1) });
			}
			getContentResolver().delete(RecordsDbHelper.CONTENT_URI_TIMERS,
					null, new String[] { String.valueOf(id) });
			reloadFragments();
		}
	}

	private void reloadFragments() {
		((MainFragments) findFragmentByPosition(0)).onReload();
		((MainFragments) findFragmentByPosition(1)).onReload();
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
		if (position == 2)
			((MainFragments) findFragmentByPosition(position)).onReload();
	}

}
