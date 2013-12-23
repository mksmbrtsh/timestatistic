package maximsblog.blogspot.com.timestatistic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.ViewSwitcher;

public class CustomDateTimePicker extends Dialog implements OnClickListener {
	private DatePicker datePicker;
	private TimePicker timePicker;
	private ViewSwitcher viewSwitcher;

	private final int SET_DATE = 100, SET_TIME = 101, SET = 102, CANCEL = 103;

	private Button btn_setDate, btn_setTime, btn_set, btn_cancel;

	private Calendar calendar_date = null;

	private Context mContext;

	private ICustomDateTimeListener iCustomDateTimeListener = null;

	private boolean is24HourView = true, isAutoDismiss = true;

	private int selectedHour, selectedMinute;

	public CustomDateTimePicker(Context c,
			ICustomDateTimeListener customDateTimeListener, long date) {
		super(c);
		mContext = c;
		iCustomDateTimeListener = customDateTimeListener;
		calendar_date = Calendar.getInstance();
		calendar_date.setTimeInMillis(date);
	}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getDateTimePickerLayout());
		
		if (calendar_date == null)
			calendar_date = Calendar.getInstance();

		selectedHour = calendar_date.get(Calendar.HOUR_OF_DAY);
		selectedMinute = calendar_date.get(Calendar.MINUTE);

		
		btn_setTime.performClick();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		// Android TimePicker hour field disappears after orientation change
		// fix from http://stackoverflow.com/questions/2170497/android-timepicker-hour-field-disappears-after-orientation-change
		timePicker.setIs24HourView(is24HourView);
		timePicker.setCurrentHour(selectedHour);
		timePicker.setCurrentMinute(selectedMinute);

		datePicker.updateDate(calendar_date.get(Calendar.YEAR),
				calendar_date.get(Calendar.MONTH),
				calendar_date.get(Calendar.DATE));
		
		timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				selectedHour = hourOfDay;
				selectedMinute = minute;
			}
		});
	};

	public View getDateTimePickerLayout() {
		LinearLayout.LayoutParams linear_match_wrap = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		LinearLayout.LayoutParams linear_wrap_wrap = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		FrameLayout.LayoutParams frame_match_wrap = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);

		LinearLayout.LayoutParams button_params = new LinearLayout.LayoutParams(
				0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

		LinearLayout linear_main = new LinearLayout(mContext);
		linear_main.setLayoutParams(linear_match_wrap);
		linear_main.setOrientation(LinearLayout.VERTICAL);
		linear_main.setGravity(Gravity.CENTER);

		LinearLayout linear_child = new LinearLayout(mContext);
		linear_child.setLayoutParams(linear_wrap_wrap);
		linear_child.setOrientation(LinearLayout.VERTICAL);

		LinearLayout linear_top = new LinearLayout(mContext);
		linear_top.setLayoutParams(linear_match_wrap);

		btn_setDate = new Button(mContext);
		btn_setDate.setLayoutParams(button_params);
		btn_setDate.setText(mContext.getString(R.string.date));
		btn_setDate.setId(SET_DATE);
		btn_setDate.setOnClickListener(this);

		btn_setTime = new Button(mContext);
		btn_setTime.setLayoutParams(button_params);
		btn_setTime.setText(mContext.getString(R.string.time));
		btn_setTime.setId(SET_TIME);
		btn_setTime.setOnClickListener(this);

		linear_top.addView(btn_setDate);
		linear_top.addView(btn_setTime);

		viewSwitcher = new ViewSwitcher(mContext);
		viewSwitcher.setLayoutParams(frame_match_wrap);

		datePicker = new DatePicker(mContext);
		timePicker = new TimePicker(mContext);
		

		
		viewSwitcher.addView(datePicker);
		viewSwitcher.addView(timePicker);
		
		LinearLayout linear_bottom = new LinearLayout(mContext);
		linear_match_wrap.topMargin = 8;
		linear_bottom.setLayoutParams(linear_match_wrap);
		
		btn_cancel = new Button(mContext);
		btn_cancel.setLayoutParams(button_params);
		btn_cancel.setText(mContext.getString(android.R.string.cancel));
		btn_cancel.setId(CANCEL);
		btn_cancel.setOnClickListener(this);
		
		btn_set = new Button(mContext);
		btn_set.setLayoutParams(button_params);
		btn_set.setText(mContext.getString(android.R.string.ok));
		btn_set.setId(SET);
		btn_set.setOnClickListener(this);

		
		linear_bottom.addView(btn_cancel);
		linear_bottom.addView(btn_set);
		
		linear_child.addView(linear_top);
		linear_child.addView(viewSwitcher);
		linear_child.addView(linear_bottom);

		linear_main.addView(linear_child);
		ScrollView s = new ScrollView(mContext);
		s.addView(linear_main);
		s.setLayoutParams(linear_match_wrap);
		return s;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case SET_DATE:
			btn_setTime.setEnabled(true);
			btn_setDate.setEnabled(false);
			viewSwitcher.showNext();
			break;

		case SET_TIME:
			btn_setTime.setEnabled(false);
			btn_setDate.setEnabled(true);
			viewSwitcher.showPrevious();
			break;

		case SET:
			if (iCustomDateTimeListener != null) {
				int month = datePicker.getMonth();
				int year = datePicker.getYear();
				int day = datePicker.getDayOfMonth();

				calendar_date.set(year, month, day, selectedHour,
						selectedMinute);

				iCustomDateTimeListener.onSet(this, calendar_date,
						calendar_date.getTime(), calendar_date
								.get(Calendar.YEAR),
						getMonthFullName(calendar_date.get(Calendar.MONTH)),
						getMonthShortName(calendar_date.get(Calendar.MONTH)),
						calendar_date.get(Calendar.MONTH), calendar_date
								.get(Calendar.DAY_OF_MONTH),
						getWeekDayFullName(calendar_date
								.get(Calendar.DAY_OF_WEEK)),
						getWeekDayShortName(calendar_date
								.get(Calendar.DAY_OF_WEEK)), calendar_date
								.get(Calendar.HOUR_OF_DAY),
						getHourIn12Format(calendar_date
								.get(Calendar.HOUR_OF_DAY)), calendar_date
								.get(Calendar.MINUTE), calendar_date
								.get(Calendar.SECOND), getAMPM(calendar_date));
			}
			dismiss();
			break;
		case CANCEL:
			if (iCustomDateTimeListener != null)
				iCustomDateTimeListener.onCancel();
			dismiss();
			break;
		}
	}

	/**
	 * @param date
	 *            date in String
	 * @param fromFormat
	 *            format of your <b>date</b> eg: if your date is 2011-07-07
	 *            09:09:09 then your format will be <b>yyyy-MM-dd hh:mm:ss</b>
	 * @param toFormat
	 *            format to which you want to convert your <b>date</b> eg: if
	 *            required format is 31 July 2011 then the toFormat should be
	 *            <b>d MMMM yyyy</b>
	 * @return formatted date
	 */
	public static String convertDate(String date, String fromFormat,
			String toFormat) {
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fromFormat);
			Date d = simpleDateFormat.parse(date);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(d);

			simpleDateFormat = new SimpleDateFormat(toFormat);
			simpleDateFormat.setCalendar(calendar);
			date = simpleDateFormat.format(calendar.getTime());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return date;
	}

	private String getMonthFullName(int monthNumber) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, monthNumber);

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM");
		simpleDateFormat.setCalendar(calendar);
		String monthName = simpleDateFormat.format(calendar.getTime());

		return monthName;
	}

	private String getMonthShortName(int monthNumber) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, monthNumber);

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM");
		simpleDateFormat.setCalendar(calendar);
		String monthName = simpleDateFormat.format(calendar.getTime());

		return monthName;
	}

	private String getWeekDayFullName(int weekDayNumber) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, weekDayNumber);

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
		simpleDateFormat.setCalendar(calendar);
		String weekName = simpleDateFormat.format(calendar.getTime());

		return weekName;
	}

	private String getWeekDayShortName(int weekDayNumber) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, weekDayNumber);

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EE");
		simpleDateFormat.setCalendar(calendar);
		String weekName = simpleDateFormat.format(calendar.getTime());

		return weekName;
	}

	private int getHourIn12Format(int hour24) {
		int hourIn12Format = 0;

		if (hour24 == 0)
			hourIn12Format = 12;
		else if (hour24 <= 12)
			hourIn12Format = hour24;
		else
			hourIn12Format = hour24 - 12;

		return hourIn12Format;
	}

	private String getAMPM(Calendar calendar) {
		String ampm = (calendar.get(Calendar.AM_PM) == (Calendar.AM)) ? "AM"
				: "PM";
		return ampm;
	}

	private void resetData() {
		calendar_date = null;
		is24HourView = true;
	}
	
	public long getTime()
	{
		calendar_date.set(Calendar.HOUR_OF_DAY, selectedHour);
		calendar_date.set(Calendar.MINUTE, selectedMinute);
		return calendar_date.getTime().getTime();
	}

	public static String pad(int integerToPad) {
		if (integerToPad >= 10 || integerToPad < 0)
			return String.valueOf(integerToPad);
		else
			return "0" + String.valueOf(integerToPad);
	}
}
