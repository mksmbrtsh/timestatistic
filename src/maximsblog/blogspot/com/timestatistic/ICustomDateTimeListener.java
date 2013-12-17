package maximsblog.blogspot.com.timestatistic;

import java.util.Calendar;
import java.util.Date;

import android.app.Dialog;

public interface ICustomDateTimeListener {
	public void onSet(Dialog dialog, Calendar calendarSelected,
			Date dateSelected, int year, String monthFullName,
			String monthShortName, int monthNumber, int date,
			String weekDayFullName, String weekDayShortName, int hour24,
			int hour12, int min, int sec, String AM_PM);

	public void onCancel();
}