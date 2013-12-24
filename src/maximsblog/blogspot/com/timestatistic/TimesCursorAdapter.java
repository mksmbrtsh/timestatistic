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

public class TimesCursorAdapter extends SimpleCursorAdapter {

	private SimpleDateFormat mSimpleDateFormat;
	private ITimes mListener;
	private Drawable mIndicator;
	private HashMap<Integer, Boolean> mSelected = new HashMap<Integer, Boolean>();
	private int mSelectedPosition;
	public static final int NORMAL_MODE = -1;

	public TimesCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags, ITimes listener) {
		super(context, layout, c, from, to, flags);
		mSimpleDateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
		mListener = listener;

		int[] attrs = { android.R.attr.listChoiceIndicatorMultiple };
		TypedArray ta = context.getTheme().obtainStyledAttributes(attrs);
		mIndicator = ta.getDrawable(0);
	}
	
	public interface ITimes
	{
		void onTimeRecordChange();
	}

	private static class ViewHolder {
		public TextView start;
		public TextView stop;
		public TextView lenght;
		public View LinearLayout01;
		public CheckBox check;
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
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		Date d = new Date(cursor.getLong(2));
		holder.start.setText(mSimpleDateFormat.format(d));
		if (cursor.getLong(1) == 0)
			holder.stop.setText("");
		else {
			d = new Date(cursor.getLong(1) + cursor.getLong(2));
			holder.stop.setText(mSimpleDateFormat.format(d));
		}
		setTime(holder.lenght, cursor.getLong(1));
		if (mSelectedPosition !=-1) {
			holder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
					if (isChecked) {
						mSelected.put(position, true);
						if(position + 1  < mCursor.getCount() && mSelected.get(position + 1) == null )
							mSelected.put(position + 1, false);
						if(position - 1 > 0 && mSelected.get(position - 1) == null)
							mSelected.put(position - 1, false);
						mListener.onTimeRecordChange();
					} else {
						
						if(position == mSelectedPosition) {
							mSelectedPosition = NORMAL_MODE;
							mSelected.clear();
							mListener.onTimeRecordChange();
							return;
						}
						mSelected.put(position, false);
						if(position > mSelectedPosition){
							HashMap<Integer, Boolean> newSelected = new HashMap<Integer, Boolean>();
							for(Entry<Integer, Boolean> iterable_element : mSelected.entrySet()) {
								if(iterable_element.getKey() <= position)
									newSelected.put(iterable_element.getKey(), iterable_element.getValue());
							}
							mSelected = newSelected;
							mListener.onTimeRecordChange();
						}
						if(position < mSelectedPosition){
							HashMap<Integer, Boolean> newSelected = new HashMap<Integer, Boolean>();
							for(Entry<Integer, Boolean> iterable_element : mSelected.entrySet()) {
								if(iterable_element.getKey() >= position)
									newSelected.put(iterable_element.getKey(), iterable_element.getValue());
							}
							mSelected = newSelected;
							mListener.onTimeRecordChange();
						}
					}
				}

			});
			if(mSelected.get(position) != null) {
				holder.check.setVisibility(View.VISIBLE);
				holder.check.setChecked(mSelected.get(position));
			}
			else
				holder.check.setVisibility(View.GONE);
		} else
			holder.check.setVisibility(View.GONE);
		holder.LinearLayout01.setBackgroundColor(cursor.getInt(4));
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
		if(position + 1  < mCursor.getCount())
			mSelected.put(position + 1, false);
		if(position - 1 > 0)
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
}
