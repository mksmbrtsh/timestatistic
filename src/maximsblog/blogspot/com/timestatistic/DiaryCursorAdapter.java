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

	private SimpleDateFormat mSimpleDateFormat;
	// private ITimes mListener;
	private Drawable mIndicator;
	private HashMap<Integer, Boolean> mSelected;
	private int mSelectedPosition;
	private long mStartdate;
	public static final int NORMAL_MODE = -1;

	public DiaryCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags, long startdate) {
		super(context, layout, c, from, to, flags);
		mSimpleDateFormat = new SimpleDateFormat("dd/MM/yy\nHH:mm");
		mStartdate = startdate;
		int[] attrs = { android.R.attr.listChoiceIndicatorMultiple };
		TypedArray ta = context.getTheme().obtainStyledAttributes(attrs);
		mIndicator = ta.getDrawable(0);
	}

	public interface ITimes {
		void onTimeRecordChange();
	}

	public static class ViewHolder {
		public TextView time;
		public TextView stop;
		public TextView lenght;
		public View LinearLayout01;
		public TextView note_text;
	}
	
	@Override
	public void bindView(View view, Context context, final Cursor cursor) {
		super.bindView(view, context, cursor);
		final ViewHolder holder;
		final int position = cursor.getPosition();
		if (view.getTag() == null) {
			holder = new ViewHolder();
			holder.time = (TextView) view.findViewById(R.id.time);
			holder.stop = (TextView) view.findViewById(R.id.stop);
			holder.LinearLayout01 = view.findViewById(R.id.before_record);
			holder.note_text = (TextView) view.findViewById(R.id.note_text);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		long start = cursor.getLong(2);
		long stop = cursor.getLong(7);
		holder.note_text.setText(cursor.getString(8));
		Date d = new Date(start);
		String t = mSimpleDateFormat.format(d);
		if (cursor.getLong(1) == 0){
			holder.stop.setText(mContext.getString(R.string.now));
		}
		else {
			d = new Date(cursor.getLong(1) + cursor.getLong(2));
			holder.stop.setText(mSimpleDateFormat.format(d));
		}
		holder.time.setText(t);
		holder.LinearLayout01.setBackgroundColor(cursor.getInt(4));
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
	
}
