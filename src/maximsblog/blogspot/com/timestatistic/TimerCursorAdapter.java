package maximsblog.blogspot.com.timestatistic;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.InputFilter.LengthFilter;
import android.view.View;
import android.widget.TextView;

public class TimerCursorAdapter extends SimpleCursorAdapter {

	private long mID;

	public TimerCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		mID = -1;
	}

	public void setCurrentTimerId(long mCurrentTimerId) {
		mID = mCurrentTimerId;
	}

	public long getCurrentTimerId() {
		return mID;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		super.bindView(view, context, cursor);
		int id = cursor.getInt(0);
		Cursor timeCursor = context.getContentResolver().query(
				RecordsDbHelper.CONTENT_URI_TIMES,
				new String[] { " SUM("+ RecordsDbHelper.LENGHT + ") AS " +RecordsDbHelper.LENGHT  },
				RecordsDbHelper.TIMERSID + "=?",
				new String[] { String.valueOf(id) }, null);
		long lenght = 0;
		if (timeCursor.moveToLast())
			lenght = timeCursor.getLong(0);
		timeCursor.close();
		
		if (mID == id) {
			timeCursor = context.getContentResolver()
					.query(RecordsDbHelper.CONTENT_URI_TIMES,
							new String[] { RecordsDbHelper.TIMERSID,
									RecordsDbHelper.STARTTIME,
									RecordsDbHelper.LENGHT },
							RecordsDbHelper.LENGHT + " IS NULL", null, null);
			if (timeCursor.getCount() == 1) {
				timeCursor.moveToFirst();
				long start = timeCursor.getLong(1);
				long now = new Date().getTime();
				lenght += now - start;
			}
			timeCursor.close();
		}
		
		TextView t = (TextView) view.findViewById(R.id.current);
		setTime(t, lenght);

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
		s = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		t.setText(s);
	}
	

}
