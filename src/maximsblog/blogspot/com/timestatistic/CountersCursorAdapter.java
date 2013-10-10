package maximsblog.blogspot.com.timestatistic;

import java.util.Date;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.InputFilter.LengthFilter;
import android.view.View;
import android.widget.TextView;

public class CountersCursorAdapter extends SimpleCursorAdapter {

	public CountersCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		super.bindView(view, context, cursor);
		boolean isRunning = cursor.getInt(6) == 1;
		if (isRunning) {
			
				long start = cursor.getLong(3);
				long now = new Date().getTime();
				long lenght = now - start +  cursor.getLong(2);
				TextView t = (TextView) view.findViewById(R.id.current);
				setTime(t, lenght);
		} else {
			long lenght = cursor.getLong(2);
			TextView t = (TextView) view.findViewById(R.id.current);
			setTime(t, lenght);
		}
		
		view.setBackgroundColor(cursor.getInt(7));
	}
	
	private void setTime(TextView t, long time)
	{
		int day;
		int hours;
		int minutes;
		int seconds;
		day = (int) (time / (24 * 60 * 60 * 1000));
		hours = (int) (time / (60 * 60 * 1000)) - day * 24;
		minutes = (int) (time / (60 * 1000)) - day * 24 * 60 - 60* hours;
		seconds = (int) (time / 1000) - day * 24 * 60 * 60 - 60 * 60
				* hours - 60 * minutes;
		String s = new String();
		if(day>0)
		{
			s = String.format("%s\n%02d:%02d:%02d",getTimeString("day", day), hours, minutes, seconds);
		} else
			s = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		t.setText(s);
	}
	
	private String getTimeString(String res, int l) {
		StringBuilder s = new StringBuilder();
		s.append(l);
		s.append(' ');
		if (l == 1 || (l % 10 == 1 && l != 11)) {
			s.append(mContext.getString(mContext.getResources().getIdentifier(
					res + "1", "string", mContext.getPackageName())));
		} else if ((l % 10 == 2 || l % 10 == 3 || l % 10 == 4) && l != 12
				&& l != 13 && l != 14) {
			s.append(mContext.getString(mContext.getResources().getIdentifier(
					res + "234", "string", mContext.getPackageName())));
		} else
			s.append(mContext.getString(mContext.getResources().getIdentifier(
					res + "s", "string", mContext.getPackageName())));
		return s.toString();
	}
	

}
