package maximsblog.blogspot.com.timestatistic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

public class XYMultipleSeriesDatasetLoader extends
	AsyncTaskLoader<PeriodData> {
	private static final long PERIOD = 1000 * 60 * 60 * 24;
	private long mStartDate;
	private long mEndDate;
	private Context mContext;
	private PeriodData data;
	
	public XYMultipleSeriesDatasetLoader(Context context, long start, long stop) {
		super(context);
		mStartDate = start;
		mEndDate = stop;
		mContext = context;
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
    @Override protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
   // @Override public void onCanceled(List<Autoinfo> apps) {
   //     super.onCanceled(apps);

        // At this point we can release the resources associated with 'apps'
        // if needed.
   //     onReleaseResources(apps);
   // }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override protected void onReset() {
    	super.onReset();
        onStopLoading();
      //  data = null;
    }


	@Override
	public PeriodData loadInBackground() {
		String[] selectionArgs = new String[] {
				String.valueOf(mStartDate),
				String.valueOf(mEndDate) };
		Cursor cursor = mContext.getContentResolver().query(RecordsDbHelper.CONTENT_URI_ALLTIMES, null,
				RecordsDbHelper.STARTTIME + " IS NOT NULL ", selectionArgs ,
				null);
		PeriodData data = new PeriodData();
		XYMultipleSeriesDataset mDataset = data.dataset;
		XYMultipleSeriesRenderer mRenderer = data.renderer;
		mDataset.clear();
		mRenderer.removeAllRenderers();
		double max = -1;
		if (cursor != null) {
			if (mEndDate <= mStartDate && mEndDate != -1) {
				return null;
			}
			if(mEndDate == -1) {
				mEndDate = new Date().getTime();
			}
			
			cursor.moveToFirst();
			Item item = new Item();
			item.start = cursor.getLong(2);
			item.stop = cursor.getLong(7);
			item.color = cursor.getInt(4);
			item.name = cursor.getString(3);
			item.id = cursor.getInt(0);
			List<Integer> ids = new ArrayList<Integer>();
			List<String> names = new ArrayList<String>();
			List<Integer> colors = new ArrayList<Integer>();
			List<Item> items = new ArrayList<Item>();
			items.add(item);
			ids.add(item.id);
			names.add(item.name);
			colors.add(item.color);
			while (cursor.moveToNext()) {
				item = new Item();
				item.start = cursor.getLong(2);
				if (item.start < mStartDate)
					item.start = mStartDate;
				item.stop = cursor.getLong(7);
				if (item.stop > mEndDate)
					item.stop = mEndDate;
				if(item.stop == 0){
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
				}
			}
			cursor.close();
			for (int i1 = 0; i1 < ids.size(); i1++) {
				int id = ids.get(i1);
				List<Item> currentItems = getItems(items, id);
				TimeSeries series = new TimeSeries(names.get(i1));
				long current = mStartDate;
				do {
					double sum = 0.0;
					for (int i2 = 0; i2 < currentItems.size(); i2++) {
						long start = currentItems.get(i2).start;
						long stop = currentItems.get(i2).stop;
						if (stop < current){
							continue;
						}
						if (start > current + PERIOD){
							continue;
						}
						if (start < current)
							start = current;
						if (stop > current + PERIOD)
							stop = current + PERIOD;
						sum += (stop - start);
					}
					if (max < sum)
						max = (double) sum;
					series.add(new Date(current), sum);
					current += PERIOD;
					series.add(new Date(current), sum);
				} while (current < mEndDate);
				XYSeriesRenderer r = new XYSeriesRenderer();
				//r.setPointStyle(PointStyle.);
				r.setColor(colors.get(i1));
				r.setFillPoints(true);
				mRenderer.addSeriesRenderer(r);
				mDataset.addSeries(series);
			}

		}
		mRenderer.setYAxisMax(max);
		mRenderer.setPanLimits(new double[] {(double) mStartDate - PERIOD, (double) mEndDate + PERIOD, 0.0, max});
		
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


}
