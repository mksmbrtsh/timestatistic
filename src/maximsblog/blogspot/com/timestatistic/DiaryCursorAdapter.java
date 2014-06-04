package maximsblog.blogspot.com.timestatistic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.InputFilter.LengthFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

public class DiaryCursorAdapter extends SimpleCursorAdapter {

	private SimpleDateFormat mSimpleTimeFormat;
	private SimpleDateFormat mSimpleDateFormat;
	private Drawable mIndicator;
	private HashMap<Integer, Boolean> mSelected;
	private int mSelectedPosition;
	private long mStartdate;
	public static final int NORMAL_MODE = -1;
	private static final int VIEW_TYPE_GROUP_START = 0;
    private static final int VIEW_TYPE_GROUP_CONT = 1;
    private static final int VIEW_TYPE_COUNT = 2;

	public DiaryCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags, long startdate) {
		super(context, layout, c, from, to, flags);
		mSimpleTimeFormat = new SimpleDateFormat("HH:mm");
		mSimpleDateFormat = new SimpleDateFormat("EEEE, d MMMM");
		mStartdate = startdate;
		int[] attrs = { android.R.attr.listChoiceIndicatorMultiple };
		TypedArray ta = context.getTheme().obtainStyledAttributes(attrs);
		mIndicator = ta.getDrawable(0);
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
		public TextView time;
		public View LinearLayout01;
		public TextView name;
		public TextView note_text;
		public TextView dateHeader;
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
			holder.time = (TextView) view.findViewById(R.id.time);
			holder.dateHeader = (TextView) view.findViewById(R.id.date_header);
			holder.LinearLayout01 = view.findViewById(R.id.color_record);
			holder.note_text = (TextView) view.findViewById(R.id.note_text);
			holder.name = (TextView) view.findViewById(R.id.name);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		long start = cursor.getLong(2);
		long stop = cursor.getLong(7);
		holder.note_text.setText("   " + cursor.getString(8));
		Date d = new Date(start);
		if(nViewType == VIEW_TYPE_GROUP_START){
			holder.dateHeader.setText(mSimpleDateFormat.format(d));
			holder.dateHeader.setVisibility(View.VISIBLE);
		}
		else
			holder.dateHeader.setVisibility(View.GONE);
		StringBuilder sb = new StringBuilder(mSimpleTimeFormat.format(d));
		if (cursor.getLong(1) == 0){
			sb.append(" - ");
			sb.append(mContext.getString(R.string.now));
		}
		else {
			d = new Date(cursor.getLong(1) + cursor.getLong(2));
			sb.append(" - ");
			sb.append(mSimpleTimeFormat.format(d));
		}
		holder.time.setText(sb.toString());
		holder.LinearLayout01.setBackgroundColor(cursor.getInt(4));
		setTime(holder.name,
				stop - start > 0 ? stop - start : new Date().getTime() - start);
	}


	public void setChoiceUnionMode(int position) {
		mSelectedPosition = position;
		mSelected.put(position, true);
		if (position + 1 < mCursor.getCount())
			mSelected.put(position + 1, false);
		if (position - 1 >= 0)
			mSelected.put(position - 1, false);
	}
	
	private void setTime(TextView t, long time) {
		if (time == 0) {
			t.setText(getCursor().getString(3));
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
				s = String.format(getCursor().getString(3) + " (%s\n%02d:%02d:%02d)",
						getTimeString("day", day), hours, minutes, seconds);
			} else
				s = String.format(getCursor().getString(3) + " (%02d:%02d:%02d)", hours, minutes, seconds);
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
	private boolean isNewGroup(Cursor cursor, int position) {
		long now_start = (cursor.getLong(2) / 1000 / 60 / 60 / 24) * 1000 * 60 * 60* 24;
		long now_stop = (cursor.getLong(7) / 1000 / 60 / 60 / 24) * 1000 * 60 * 60* 24;
        cursor.moveToPosition(position - 1);
        long before_start = (cursor.getLong(2) / 1000 / 60 / 60 / 24) * 1000 * 60 * 60* 24;
        long before_stop = (cursor.getLong(7) / 1000 / 60 / 60 / 24) * 1000 * 60 * 60* 24;
        cursor.moveToPosition(position);    
        //if ((now != before_start || before_stop != now) && before_stop != 0 ) {
        if((before_start != before_stop || before_stop != now_start) && before_stop !=0){
            return true;
        }

        return false;
    }
}
