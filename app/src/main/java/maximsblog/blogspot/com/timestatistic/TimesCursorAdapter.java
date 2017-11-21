package maximsblog.blogspot.com.timestatistic;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class TimesCursorAdapter extends SimpleCursorAdapter implements
ListView.OnScrollListener {

	private SimpleDateFormat mSimpleTimeFormat;
	private SimpleDateFormat mSimpleDateFormat;
	// private ITimes mListener;
	private Drawable mIndicator;
	private HashMap<Integer, Boolean> mSelected;
	private int mSelectedPosition;
	private long mStartdate;
	private boolean mBusy = false;
	public static final int NORMAL_MODE = -1;
	
	private static final int VIEW_TYPE_GROUP_START = 0;
    private static final int VIEW_TYPE_GROUP_CONT = 1;
    private static final int VIEW_TYPE_COUNT = 2;
	private Context mContext;
	public TimesCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags, long startdate) {
		super(context, layout, c, from, to, flags);
		mSimpleTimeFormat = new SimpleDateFormat("HH:mm");
		mSimpleDateFormat = new SimpleDateFormat("EEEE, d MMMM");
		mStartdate = startdate;
		int[] attrs = { android.R.attr.listChoiceIndicatorMultiple };
		TypedArray ta = context.getTheme().obtainStyledAttributes(attrs);
		mIndicator = ta.getDrawable(0);
		mContext = context;
	}

	public interface ITimes {
		void onTimeRecordChange();
	}
	
	@Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
	
	@Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_GROUP_START;
        }
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        boolean newGroup = isNewGroup(cursor, position);

        if (newGroup) {
            return VIEW_TYPE_GROUP_START;
        } else {
            return VIEW_TYPE_GROUP_CONT;
        }
    }

	public static class ViewHolder {
		public TextView times;
		public View LinearLayout01;
		public CheckBox check;
		public View note;
		public int id;
		public TextView dateHeader;
		public TextView lenghtRecord;
	}

	@Override
	public void bindView(View view, Context context, final Cursor cursor) {
		super.bindView(view, context, cursor);
		final ViewHolder holder;
		final int position = cursor.getPosition();
		int nViewType;
		
        if (position == 0) {
            nViewType = VIEW_TYPE_GROUP_START;
        } else {
            boolean newGroup = isNewGroup(cursor, position);
            if (newGroup) {
                nViewType = VIEW_TYPE_GROUP_START;
            } else {
                nViewType = VIEW_TYPE_GROUP_CONT;
            }
        }
        
		if (view.getTag() == null) {
			holder = new ViewHolder();
			holder.times = (TextView) view.findViewById(R.id.times);
			holder.LinearLayout01 = view.findViewById(R.id.before_record);
			holder.check = (CheckBox) view.findViewById(R.id.check);
			holder.note = view.findViewById(R.id.note);
			holder.dateHeader = (TextView)view.findViewById(R.id.date_header);
			holder.lenghtRecord = (TextView)view.findViewById(R.id.lenght_record);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		long start = cursor.getLong(2);
		long stop = cursor.getLong(7);

		Date d = new Date(start);
		StringBuilder sb = new StringBuilder();
		sb.append(mSimpleTimeFormat.format(d));
		holder.id = cursor.getInt(5);
		
		if(nViewType == VIEW_TYPE_GROUP_START){
			holder.dateHeader.setText(mSimpleDateFormat.format(d));
			holder.dateHeader.setVisibility(View.VISIBLE);
		}
		else
			holder.dateHeader.setVisibility(View.GONE);
		sb.append(" - ");
		if (cursor.getLong(1) == 0){

		} else {
			d = new Date(cursor.getLong(1) + cursor.getLong(2));
			sb.append(mSimpleTimeFormat.format(d));
		}
		setTime(holder.lenghtRecord,
				stop - start > 0 ? stop - start : new Date().getTime() - start);
		
		
		if (mSelectedPosition != -1) {
			if (mSelected.get(position) != null) {
				holder.check.setChecked(mSelected.get(position));
				holder.check.setVisibility(View.VISIBLE);
			} else {
				holder.check.setChecked(false);
				holder.check.setVisibility(View.INVISIBLE);
			}
		} else {
			holder.check.setChecked(false);
			holder.check.setVisibility(View.INVISIBLE);
		}
		holder.LinearLayout01.setBackgroundColor(cursor.getInt(4));
		if (!mBusy) {
			Cursor c = mContext.getContentResolver().query(RecordsDbHelper.CONTENT_URI_NOTES, new String[] { RecordsDbHelper.ID3, RecordsDbHelper.NOTE },RecordsDbHelper.ID3+ "=?" , new String[] { String.valueOf(holder.id) }, null);
			if(c.getCount() == 1) {
				holder.note.setVisibility(View.VISIBLE);
			} else
				holder.note.setVisibility(View.INVISIBLE);	
			c.close();
		} else 
			holder.note.setVisibility(View.INVISIBLE);
		
		holder.times.setText(sb.toString());
	}

	private void setTime(TextView t, long time) {
		if (time == 0) {
			t.setText("");
		} else {
			int day;
			int hours;
			int minutes;
			int seconds;
			day = (int) (time / (24 * 60 * 60 * 1000));
			hours = (int) (time / (60 * 60 * 1000)) - day * 24;
			minutes = (int) (time / (60 * 1000)) - day * 24 * 60 - 60 * hours;
			seconds = (int) (time / 1000) - day * 24 * 60 * 60 - 60 * 60
					* hours - 60 * minutes;
			String s = new String();
			if (day > 0) {
				s = String.format(" (+%s\n%02d:%02d:%02d)",
						getTimeString("day", day), hours, minutes, seconds);
			} else
				s = String.format(" (+%02d:%02d:%02d)", hours, minutes, seconds);
			t.setText(s);
		}
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

	public void setChoiceUnionMode(int position) {
		mSelectedPosition = position;
		mSelected.put(position, true);
		if (position + 1 < getCursor().getCount())
			mSelected.put(position + 1, false);
		if (position - 1 >= 0)
			mSelected.put(position - 1, false);
	}

	public void setSelectedPosition(int position) {
		mSelectedPosition = position;
	}

	public int getChoiceUnionMode() {
		return mSelectedPosition;
	}

	public HashMap<Integer, Boolean> getSelected() {
		return mSelected;
	}

	public void setSelected(HashMap<Integer, Boolean> selected) {
		mSelected = selected;
	}

	public void setStartDate(long startdate) {
		mStartdate = startdate;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_IDLE:
			mBusy = false;
			int first = view.getFirstVisiblePosition();
			int count = view.getChildCount();
			for (int i = 0; i < count; i++) {
				ViewHolder holder = (ViewHolder) view.getChildAt(i).getTag();
				Cursor c = mContext.getContentResolver().query(RecordsDbHelper.CONTENT_URI_NOTES, new String[] { RecordsDbHelper.ID3, RecordsDbHelper.NOTE },RecordsDbHelper.ID3+ "=?" , new String[] { String.valueOf(holder.id) }, null);
				if(c.getCount() == 1) {
					holder.note.setVisibility(View.VISIBLE);
				} else
					holder.note.setVisibility(View.INVISIBLE);	
				c.close();
			}

			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
			mBusy = true;
			break;
		case OnScrollListener.SCROLL_STATE_FLING:
			mBusy = true;
			break;
		}
	}

	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}
	
	private boolean isNewGroup(Cursor cursor, int position) {
		cursor.moveToPosition(position - 1);
		Calendar cal = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal.setTimeInMillis(cursor.getLong(2));
		cal2.setTimeInMillis(cursor.getLong(7));
        //
        //long before_start = (cursor.getLong(2) / 1000 / 60 / 60 / 24) * 1000 * 60 * 60* 24;
        //long before_stop = (cursor.getLong(7) / 1000 / 60 / 60 / 24) * 1000 * 60 * 60* 24;
        cursor.moveToPosition(position);    
        //if ((now != before_start || before_stop != now) && before_stop != 0 ) {
        if(cal.get(Calendar.DATE) != cal2.get(Calendar.DATE)) {//((before_start != before_stop) && before_stop !=0){
            return true;
        }

        return false;
    }
	
}
