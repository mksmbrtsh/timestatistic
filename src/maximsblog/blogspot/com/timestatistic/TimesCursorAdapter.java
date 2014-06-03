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
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class TimesCursorAdapter extends SimpleCursorAdapter implements
ListView.OnScrollListener {

	private SimpleDateFormat mSimpleDateFormat;
	// private ITimes mListener;
	private Drawable mIndicator;
	private HashMap<Integer, Boolean> mSelected;
	private int mSelectedPosition;
	private long mStartdate;
	private boolean mBusy = false;
	public static final int NORMAL_MODE = -1;

	public TimesCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags, long startdate) {
		super(context, layout, c, from, to, flags);
		mSimpleDateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
		mStartdate = startdate;
		int[] attrs = { android.R.attr.listChoiceIndicatorMultiple };
		TypedArray ta = context.getTheme().obtainStyledAttributes(attrs);
		mIndicator = ta.getDrawable(0);
	}

	public interface ITimes {
		void onTimeRecordChange();
	}

	public static class ViewHolder {
		public TextView start;
		public TextView stop;
		public TextView lenght;
		public View LinearLayout01;
		public CheckBox check;
		public View note;
		public int id;
	}

	@Override
	public void bindView(View view, Context context, final Cursor cursor) {
		super.bindView(view, context, cursor);
		final ViewHolder holder;
		final int position = cursor.getPosition();
		if (view.getTag() == null) {
			holder = new ViewHolder();
			holder.start = (TextView) view.findViewById(R.id.start);
			holder.stop = (TextView) view.findViewById(R.id.stop);
			holder.lenght = (TextView) view.findViewById(R.id.lenght);
			holder.LinearLayout01 = view.findViewById(R.id.before_record);
			holder.check = (CheckBox) view.findViewById(R.id.check);
			holder.note = view.findViewById(R.id.note);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		long start = cursor.getLong(2);
		long stop = cursor.getLong(7);

		Date d = new Date(start);
		holder.start.setText(mSimpleDateFormat.format(d));
		holder.id = cursor.getInt(5);
		if (cursor.getLong(1) == 0)
			holder.stop.setText("");
		else {
			d = new Date(cursor.getLong(1) + cursor.getLong(2));
			holder.stop.setText(mSimpleDateFormat.format(d));
		}
		setTime(holder.lenght,
				stop - start > 0 ? stop - start : new Date().getTime() - start);
		if (mSelectedPosition != -1) {
			if (mSelected.get(position) != null) {
				holder.check.setChecked(mSelected.get(position));
				holder.check.setVisibility(View.VISIBLE);
			} else {
				holder.check.setChecked(false);
				holder.check.setVisibility(View.INVISIBLE);
			}
			holder.lenght.setVisibility(View.INVISIBLE);
		} else {
			holder.check.setChecked(false);
			holder.check.setVisibility(View.INVISIBLE);
			holder.lenght.setVisibility(View.VISIBLE);
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
				s = String.format("%s\n%02d:%02d:%02d",
						getTimeString("day", day), hours, minutes, seconds);
			} else
				s = String.format("%02d:%02d:%02d", hours, minutes, seconds);
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
		if (position + 1 < mCursor.getCount())
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
	
}
