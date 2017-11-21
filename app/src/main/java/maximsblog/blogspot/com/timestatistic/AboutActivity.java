package maximsblog.blogspot.com.timestatistic;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.TabPageIndicator;

public class AboutActivity extends Activity {

	private String[] mTitles;
	private int[] mIcons;
	private maximsblog.blogspot.com.timestatistic.AboutActivity.PagesAdapter mAdapter;
	private ViewPager mPager;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.about_activity);
	    mTitles = getResources().getStringArray(R.array.AboutPages);
		mIcons = new int[] {R.drawable.ic_counter_title, R.drawable.ic_diary_title};
		// prepare ViewPagerIndicator
		mAdapter = new PagesAdapter(getFragmentManager());
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setOffscreenPageLimit(3);// all fragments upload, fix not switch counters
		mPager.setAdapter(mAdapter);
		TabPageIndicator  indicator = (TabPageIndicator ) findViewById(R.id.indicator);
		indicator.setViewPager(mPager);
		
		try {
			String nameversion = getString(R.string.version) + ": " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			getActionBar().setTitle(getString(R.string.app_name));
			getActionBar().setSubtitle(nameversion);
		} catch (NameNotFoundException e) {

		}
	}

	class PagesAdapter extends FragmentPagerAdapter implements IconPagerAdapter  {
		public PagesAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment f;
			if (position == 0) {
				AboutFragment fg = AboutFragment.newInstance();
				f = fg;
			} else {
				HistoryFragment fg = HistoryFragment.newInstance();
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
}
