package maximsblog.blogspot.com.timestatistic;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.DialogFragment;

public class CalendarSetupDialogFragment extends DialogFragment implements OnClickListener {
	
	private ICalendarSetupDialog mListener;
	
	public interface ICalendarSetupDialog {
		void setupCalendar(String id, String name);
	}
	public void setCalendarSetupDialog(ICalendarSetupDialog listener) {
		mListener = listener;
	}
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	GetGcalendars();
    	String id = getArguments().getString("calendar_id");
    	int selectedCalendarIndex = calIDs.indexOf(id);
    	CharSequence[] calendarNames = new CharSequence[calnames.size()];
    	for(int i=0;i<calnames.size(); i++){
    		calendarNames[i] = calnames.get(i);
    	}
    	
        return new AlertDialog.Builder(getActivity())
            .setTitle(R.string.select_gcalendar)
            .setSingleChoiceItems(calendarNames, selectedCalendarIndex , this)
            .create();
    }
	
	
	private ArrayList<String> calnames;
	private ArrayList<String> calIDs;
	
	@SuppressLint("NewApi")
	public boolean GetGcalendars() {

		calnames = new ArrayList<String>();
		calIDs = new ArrayList<String>();
		String[] CALENDAR_QUERY_COLUMNS = { CalendarContract.Calendars._ID,
				CalendarContract.Calendars.NAME,
				CalendarContract.Calendars.VISIBLE,
				CalendarContract.Calendars.OWNER_ACCOUNT };

		ContentResolver contentResolver = getActivity().getContentResolver();
		final Cursor cursor = contentResolver.query(
				CalendarContract.Calendars.CONTENT_URI, CALENDAR_QUERY_COLUMNS,
				null, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				final String calID = cursor.getString(0);
				final String calName = cursor.getString(1);
				final Boolean selected = !cursor.getString(2).equals("0");
				final String accountName = cursor.getString(3);
				if (calName != null) {
					calnames.add(calName);
					calIDs.add(calID);
				}
			}
			cursor.close();
		}
		return calIDs.size() > 0;

	}
	@Override
	public void onClick(DialogInterface dialog, int which) {
		this.dismiss();
		mListener.setupCalendar(calIDs.get(which), calnames.get(which));
	}
}