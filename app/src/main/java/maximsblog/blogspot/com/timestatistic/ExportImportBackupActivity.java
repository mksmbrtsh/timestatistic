package maximsblog.blogspot.com.timestatistic;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExportImportBackupActivity extends PreferenceActivity
		implements OnPreferenceClickListener {

	private static final int OPENDB = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.export_import_backup);
		Preference p = findPreference("export");
		p.setOnPreferenceClickListener(this);
		p = findPreference("import");
		p.setOnPreferenceClickListener(this);
		p = findPreference("google_drive");
		p.setOnPreferenceClickListener(this);
		p = findPreference("export_to_gcalendar");
		p.setOnPreferenceClickListener(this);
		p = findPreference("export_to_csv");
		p.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals("export")) {
			OpenHelper o = new OpenHelper(getApplicationContext());
			String d = "";
			SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
			try {
				o.exportDatabase(
						getFilesDir(),
						d = getExternalFilesDir(null).getAbsolutePath()
								+ File.separator + "timestat"
								+ sdf.format(new Date()) + ".db");

			} catch (IOException e) {
				e.printStackTrace();
			}
			o.close();
			if (d.length() > 0)
				d = getString(R.string.exportok) + ":\n" + d;
			else
				d = getString(R.string.exportfail);
			Toast.makeText(getApplicationContext(), d, Toast.LENGTH_LONG)
					.show();

		} else if (preference.getKey().equals("google_drive")) {
			startActivity(new Intent(this, GdriveUpload.class));
		} else if (preference.getKey().equals("export_to_gcalendar")) {
			startActivity(new Intent(this, ExportToGoogleCalendarActivity.class));
		} else if(preference.getKey().equals("export_to_csv")){
			startActivity(new Intent(this, ExportToCSVActivity.class));
		} else {
			Intent intent = new Intent(this, FileDialog.class);
			intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { ".db",
					".sqlite" });
			intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
			startActivityForResult(intent, OPENDB);
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null && requestCode == OPENDB && resultCode == RESULT_OK) {
			String newDbOpen = data.getStringExtra(FileDialog.RESULT_PATH);
			OpenHelper o = new OpenHelper(getApplicationContext());
			try {
				o.importDatabase(getFilesDir(), newDbOpen);

			} catch (IOException e) {
				e.printStackTrace();
			}
			o.close();
			Toast.makeText(getApplicationContext(),
					getString(R.string.importok), Toast.LENGTH_LONG).show();
			getContentResolver().notifyChange(
					RecordsDbHelper.CONTENT_URI_TIMES, null);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
