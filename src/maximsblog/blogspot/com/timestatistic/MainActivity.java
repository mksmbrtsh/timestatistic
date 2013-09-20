package maximsblog.blogspot.com.timestatistic;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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

public class MainActivity extends SherlockFragmentActivity   {

	private AddCounterDialogFragment mAddCounterDialogFragment;
	private String[] mTitles;
	
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
        // add dialogs
        mAddCounterDialogFragment = new AddCounterDialogFragment();
	}

    class PagesAdapter extends FragmentPagerAdapter {
        public PagesAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	Fragment f;
        	if(position ==0) {      
        		f = CountersFragment.newInstance();
        		mAddCounterDialogFragment.setCounterDialogListener((AddCounterDialogFragment.AddCounterDialog)f);
        	}
        	else
        		f = new DiagramFragment();
            
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
            newFragment.show(ft, "dialog");
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	

}
