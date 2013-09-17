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
		if(mID == id)
		{
			Cursor timeCursor = context.getContentResolver().query(RecordsDbHelper.CONTENT_URI_TIMES, new String[] { RecordsDbHelper.TIMERSID, RecordsDbHelper.STARTTIME, RecordsDbHelper.LENGHT }, RecordsDbHelper.LENGHT + " IS NULL", null , null);
			timeCursor.moveToFirst();
			long start = timeCursor.getLong(1);
			long now = new Date().getTime();
			timeCursor.close();
			TextView t = (TextView) view.findViewById(R.id.current);
			t.setText("+" + String.valueOf(now - start));
		} else {
			Cursor timeCursor = context.getContentResolver().query(RecordsDbHelper.CONTENT_URI_TIMES, new String[] { RecordsDbHelper.TIMERSID, RecordsDbHelper.STARTTIME, RecordsDbHelper.LENGHT }, RecordsDbHelper.TIMERSID+"=?", new String[]{String.valueOf(id)}, null);
			long lenght = 0;
			if(timeCursor.moveToLast())
				lenght = timeCursor.getLong(2);
			timeCursor.close();
			TextView t = (TextView) view.findViewById(R.id.current);
			t.setText(mContext.getString(R.string.last_value)+":"+ String.valueOf(lenght));
		}
	}

}
