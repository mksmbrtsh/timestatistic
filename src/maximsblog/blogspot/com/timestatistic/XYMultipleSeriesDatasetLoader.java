package maximsblog.blogspot.com.timestatistic;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.util.MathHelper;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint.Align;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class XYMultipleSeriesDatasetLoader extends AsyncTaskLoader<PeriodData> {
	private long mStartDate;
	private long mEndDate;
	private long mPeriod;
	private Context mContext;
	private PeriodData data;
	private ArrayList<Integer> mIds;

	public XYMultipleSeriesDatasetLoader(Context context, long start,
			long stop, long period, ArrayList<Integer> ids) {
		super(context);
		mStartDate = start;
		mEndDate = stop;
		mContext = context;
		mPeriod = period;
		mIds = ids;
	}


	@Override
	public void deliverResult(PeriodData data) {
		if (isReset()) {
			// a query came in while the loader is stopped
			return;
		}

		this.data = data;

		super.deliverResult(data);
	}

	@Override
	protected void onStartLoading() {
		if (data != null) {
			deliverResult(data);
		}

		if (takeContentChanged() || data == null) {
			forceLoad();
		}
	}

	/**
	 * Handles a request to stop the Loader.
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	/**
	 * Handles a request to cancel a load.
	 */
	// @Override public void onCanceled(List<Autoinfo> apps) {
	// super.onCanceled(apps);

	// At this point we can release the resources associated with 'apps'
	// if needed.
	// onReleaseResources(apps);
	// }

	/**
	 * Handles a request to completely reset the Loader.
	 */
	@Override
	protected void onReset() {
		super.onReset();
		onStopLoading();
		// data = null;
	}

	@Override
	public PeriodData loadInBackground() {
		String[] selectionArgs = new String[] { String.valueOf(mStartDate),
				String.valueOf(mEndDate) };
		Cursor cursor = mContext.getContentResolver().query(
				RecordsDbHelper.CONTENT_URI_ALLTIMES, null,
				RecordsDbHelper.STARTTIME + " IS NOT NULL ", selectionArgs,
				null);
		PeriodData data = new PeriodData();
		XYMultipleSeriesDataset mDataset = data.dataset;
		XYMultipleSeriesRenderer mRenderer = data.renderer;
		mDataset.clear();
		mRenderer.removeAllRenderers();
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		float val = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12,
				metrics);
		double max = -1;
		if (cursor != null) {
			if (mEndDate <= mStartDate && mEndDate != -1) {
				return null;
			}
			if (mEndDate == -1) {
				mEndDate = new Date().getTime();
			}
			List<Integer> ids = new ArrayList<Integer>();
			List<String> names = new ArrayList<String>();
			List<Integer> colors = new ArrayList<Integer>();
			List<Item> items = new ArrayList<Item>();
			StringBuilder sb = new StringBuilder();
			Item item;
			cursor.moveToFirst();
			if (mIds.contains(cursor.getInt(0))) {
				item = new Item();
				item.start = cursor.getLong(2);
				item.stop = cursor.getLong(7);
				item.color = cursor.getInt(4);
				item.name = cursor.getString(3);
				item.id = cursor.getInt(0);

				items.add(item);
				ids.add(item.id);
				names.add(item.name);
				colors.add(item.color);

				sb.append("<font color=\"#");
				sb.append(String.format("%08X", item.color).substring(2));
				sb.append("\">");
				sb.append("<big>&#9679;</big> ");
				sb.append("</font>");
				sb.append(item.name);
				sb.append("<br>");
			}
			while (cursor.moveToNext()) {
				if (!mIds.contains(cursor.getInt(0)))
					continue;
				item = new Item();
				item.start = cursor.getLong(2);
				if (item.start < mStartDate)
					item.start = mStartDate;
				item.stop = cursor.getLong(7);
				if (item.stop > mEndDate)
					item.stop = mEndDate;
				if (item.stop == 0) {
					item.stop = new Date().getTime();
				}
				item.color = cursor.getInt(4);
				item.id = cursor.getInt(0);
				item.name = cursor.getString(3);
				items.add(item);
				if (!ids.contains(item.id)) {
					ids.add(item.id);
					names.add(item.name);
					colors.add(item.color);
					sb.append("<font color=\"#");
					sb.append(String.format("%08X", item.color).substring(2));
					sb.append("\">");
					sb.append("<big>&#9679;</big> ");
					sb.append("</font>");
					sb.append(item.name);
					sb.append("<br>");
				}
			}
			cursor.close();

			int index = -1;
			for (int i1 = 0; i1 < ids.size(); i1++) {
				int id = ids.get(i1);
				List<Item> currentItems = getItems(items, id);
				TimeSeries series = new TimeSeries(names.get(i1));
				long current = mStartDate;
				double sd = 0.0;
				do {
					double sum = 0.0;
					for (int i2 = 0; i2 < currentItems.size(); i2++) {
						long start = currentItems.get(i2).start;
						long stop = currentItems.get(i2).stop;
						if (stop < current) {
							continue;
						}
						if (start > current + mPeriod) {
							continue;
						}
						if (start < current)
							start = current;
						if (stop > current + mPeriod)
							stop = current + mPeriod;
						sum += (stop - start);
					}
					if (max < sum)
						max = (double) sum;
					sd += sum;
					if (sum == 0) {
						series.add(new Date((current + current + mPeriod) / 2),
								MathHelper.NULL_VALUE);
					} else
						series.add(new Date((current + current + mPeriod) / 2),
								sum);
					current += mPeriod;
				} while (current < mEndDate);
				XYSeriesRenderer r = new XYSeriesRenderer();
				r.setColor(colors.get(i1));
				r.setFillPoints(true);
				r.setLineWidth(2);
				r.setPointStyle(PointStyle.CIRCLE);
				r.setPointStrokeWidth(6);
				r.setDisplayChartValues(true);

				r.setChartValuesTextSize(val);
				mRenderer.addSeriesRenderer(r);
				mDataset.addSeries(series);

				index = sb.indexOf("<br>", index);
				String s = ": "
						+ getLabel((double) (sd / series.getItemCount()))
						+" (&#8721;= " + getLabel((double) (sd))+ ")" +"<br>";
				sb.replace(index, index + "<br>".length(), s);
				index += s.length();
			}
			data.legend = sb.toString();
		}
		mRenderer.setYAxisMax(max);
		mRenderer.setYAxisMin(0);
		mRenderer.setShowLabels(true);
		mRenderer.setShowLegend(false);
		mRenderer.setInScroll(false);
		mRenderer.setShowGrid(true);
		mRenderer.setXLabelsAlign(Align.CENTER);
		mRenderer.setYLabelsAngle(-90);
		mRenderer.setYLabelsPadding(5);
		mRenderer.setYLabelsVerticalPadding(5);

		mRenderer.setYLabelsAlign(Align.CENTER);
		mRenderer.setPointSize(6.0f);
		mRenderer.setLegendTextSize(val);
		mRenderer.setLabelsTextSize(val);
		mRenderer.setDisplayValues(true);
		mRenderer.setZoomButtonsVisible(false);
		mRenderer.setZoomEnabled(true);
		mRenderer.setXLabelsPadding(2.0f);
		mRenderer.setXAxisMax((double) mEndDate + mPeriod);
		mRenderer.setXAxisMin((double) mStartDate - mPeriod);
		mRenderer.setPanLimits(new double[] { (double) mStartDate - mPeriod,
				(double) mEndDate + mPeriod, 0.0, 2 * mPeriod });
		// mRenderer.setZoomLimits(new double[] {(double) mStartDate - PERIOD /
		// 2, (double) mEndDate + PERIOD / 2, 0.0, 1.5* PERIOD});
		mRenderer.setExternalZoomEnabled(true);
		return data;
	}

	private List<Item> getItems(List<Item> items, int id) {
		List<Item> i = new ArrayList<Item>();
		for (Item item : items) {
			if (item.id == id)
				i.add(item);
		}
		return i;
	}

	private String getLabel(double time) {
		int day;
		int hours;
		int minutes;
		int seconds;
		day = (int) (time / (24 * 60 * 60 * 1000));
		hours = (int) (time / (60 * 60 * 1000)) - day * 24;
		minutes = (int) (time / (60 * 1000)) - day * 24 * 60 - 60 * hours;
		seconds = (int) (time / 1000) - day * 24 * 60 * 60 - 60 * 60 * hours
				- 60 * minutes;
		String s = new String();
		s = String.format("%10d:%02d:%02d", hours + day * 24, minutes, seconds);
		return s;
	}
}
