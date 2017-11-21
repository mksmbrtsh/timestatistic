package maximsblog.blogspot.com.timestatistic;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	
	
	public static final String STARTTIMEFILTER = "startdatetimefilter";
	public static final String ENDTIMEFILTER = "enddatetimefilter";

	public static final String STARTTIMEFILTERPERIOD = "startdatetimefilterperiod";

	public static final String ENDTIMEFILTERPERIOD = "enddatetimefilterperiod";

	public static final String ENDTIMEFILTEREXPORT = "enddatetimefilterexport";
	public static final String STARTTIMEFILTEREXPORT = "startdatetimefilterexport";
	
	public static final String ENDTIMEFILTEREXPORTCSV = "enddatetimefilterexportcsv";
	public static final String STARTTIMEFILTEREXPORTCSV = "startdatetimefilterexportcsv";
	
	public static final class STARTTIMEFILTERS
	{
		public static final int TODAY = 0;
		public static final int YESTERDAY = 1;
		public static final int WEEK = 2;
		public static final int MOUNTH = 3;
		public static final int YEAR = 4;
		public static final int ALLTIME = 5;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		Preference p = findPreference("visible_notif");
		p.setOnPreferenceChangeListener(this);
		p = findPreference("alarm");
		p.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals("alarm")) {
			if ((Boolean) newValue) {
				app.setRunningCounterAlarmSettings(getApplicationContext());
			} else {
				app.delAlarm(getApplicationContext());
			}
			 Editor e = preference.getEditor();
			 e.putBoolean("alarm", (Boolean)newValue);
			 e.commit();
			 app.setStatusBar(this);
		} else {
			Editor e = preference.getEditor();
			e.putBoolean("visible_notif", (Boolean)newValue);
			e.commit();
			app.setStatusBar(this);
		}
		return true;
	}

}
