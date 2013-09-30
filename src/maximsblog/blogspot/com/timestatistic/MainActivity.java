package maximsblog.blogspot.com.timestatistic;

import java.util.ArrayList;
import java.util.List;

import maximsblog.blogspot.com.timestatistic.AddCounterDialogFragment.AddCounterDialog;
import maximsblog.blogspot.com.timestatistic.AreYouSureResetAllDialog.ResetAllDialog;
import maximsblog.blogspot.com.timestatistic.MainActivity.MainFragments;

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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity implements ResetAllDialog, AddCounterDialog   {

	private AddCounterDialogFragment mAddCounterDialogFragment;
	private String[] mTitles;
	private ArrayList<MainFragments> mFragments;
	
	public interface MainFragments {
		void onReload();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTitles = getResources().getStringArray(R.array.TitlePages);
		// prepare ViewPagerIndicator
		FragmentPagerAdapter adapter = new PagesAdapter(getSupportFragmentManager());
        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(adapter);
        TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        mFragments = new ArrayList<MainFragments>();
        // add dialogs
        mAddCounterDialogFragment = new AddCounterDialogFragment();
        mAddCounterDialogFragment.setCounterDialogListener(this);
	}

    class PagesAdapter extends FragmentPagerAdapter {
        public PagesAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	Fragment f;
        	if(position ==0) {      
        		CountersFragment fg = CountersFragment.newInstance();
        		mFragments.add(fg);
        		f = fg;
        	}
        	else {
        		DiagramFragment fg = DiagramFragment.newInstance();
        		mFragments.add(fg);
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
		switch (item.getItemId()) {
		case R.id.item_add:
			mAddCounterDialogFragment.show(this.getSupportFragmentManager(), "dlg1");
			break;
		case R.id.item_reset_all:
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			AreYouSureResetAllDialog newFragment = new AreYouSureResetAllDialog ();
			newFragment.setResetAllDialogListener(this);
            newFragment.show(ft, "dialog");
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onResetAllDialog() {
		getContentResolver().delete(RecordsDbHelper.CONTENT_URI_RESETCOUNTERS, null, null);
		reloadFragments();
	}
	@Override
	public void onFinishAddDialog(String inputText) {
		ContentValues cv = new ContentValues();
		cv.put(RecordsDbHelper.NAME, inputText);
		Uri row  = getContentResolver().insert(
				RecordsDbHelper.CONTENT_URI_TIMERS, cv);
		int id = Integer.valueOf(row.getLastPathSegment());
		cv.clear();
		cv.put(RecordsDbHelper.TIMERSID, id);
		getContentResolver().insert(
				RecordsDbHelper.CONTENT_URI_TIMES, cv);
		reloadFragments();
	}
	
	private void reloadFragments()
	{
		for (MainFragments fragments : mFragments) {
			fragments.onReload();
		}
	}
	

}
