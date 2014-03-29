package maximsblog.blogspot.com.timestatistic;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.Set;

import maximsblog.blogspot.com.timestatistic.MainActivity.MainFragments;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class DiagramFragment extends Fragment implements
		LoaderCallbacks<Cursor>, MainFragments {

	/** The main series that will include all the data. */
	private CategorySeries mSeries = new CategorySeries("");
	/** The main renderer for the main dataset. */
	private DefaultRenderer mRenderer = new DefaultRenderer();

	/** The chart view that displays the data. */
	private GraphicalView mChartView;
	private View mLayout;
	private LoaderManager loadermanager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		loadermanager = getLoaderManager();
		super.onCreate(savedInstanceState);
		mRenderer.setZoomButtonsVisible(false);
		mRenderer.setStartAngle(180);
		mRenderer.setDisplayValues(false);
		mRenderer.setInScroll(false);
		mRenderer.setClickEnabled(false);
		mRenderer.setPanEnabled(false);
		mRenderer.setZoomEnabled(false);
		mRenderer.setShowLabels(true);
		DisplayMetrics metrics = getActivity().getResources()
				.getDisplayMetrics();
		float val = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15,
				metrics);
		mRenderer.setLegendTextSize(val);
		mRenderer.setLabelsTextSize(val);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayout = inflater.inflate(R.layout.fragment_diagram, container, false);
		if (mChartView == null) {
			ViewGroup layout = (ViewGroup) mLayout.findViewById(R.id.chart);
			mChartView = ChartFactory.getPieChartView(this.getActivity(),
					mSeries, mRenderer);
			layout.addView(mChartView, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
		return mLayout;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mChartView == null) {
			ViewGroup layout = (ViewGroup) mLayout.findViewById(R.id.chart);
			mChartView = ChartFactory.getPieChartView(this.getActivity(),
					mSeries, mRenderer);
			layout.addView(mChartView, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
		loadermanager.initLoader(1, null, this);
	}

	@Override
	public void onPause() {
		super.onPause();
		ViewGroup chartLayout = (ViewGroup) mLayout.findViewById(R.id.chart);
		chartLayout.removeView(mChartView);
		mChartView = null;

	};

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] selectionArgs = new String[] { String.valueOf(app
				.getStartDate(getActivity()).startDate) };
		CursorLoader loader = new CursorLoader(this.getActivity(),
				RecordsDbHelper.CONTENT_URI_TIMES, null, null, selectionArgs,
				null);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mRenderer.removeAllRenderers();
		if (cursor != null) {
			mSeries.clear();
			ArrayList<Color> c = new ArrayList<Color>();
			cursor.moveToFirst();
			ArrayList<Double> values = new ArrayList<Double>();
			ArrayList<String> nvalues = new ArrayList<String>();
			long id = cursor.getLong(0);
			long t = cursor.getLong(2);
			long start = cursor.getLong(3);
			long startdate = app.getStartDate(getActivity()).startDate;
			if (start < startdate)
				start = startdate;
			String s = cursor.getString(5);

			boolean isRunning = cursor.getInt(6) == 1;
			Double sum = 0.0;
			sum = (double) (isRunning ? t + new Date().getTime() - start : t);

			SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
			int color = cursor.getInt(7);
			renderer.setColor(color);
			NumberFormat nf = NumberFormat.getPercentInstance();
			nf.setMaximumFractionDigits(1);
			renderer.setChartValuesFormat(nf);
			if (sum != 0.0) {
				nvalues.add(s);
				values.add(sum);
				mRenderer.addSeriesRenderer(renderer);
			}
			while (cursor.moveToNext()) {
				id = cursor.getLong(0);
				t = cursor.getLong(2);
				start = cursor.getLong(3);
				s = cursor.getString(5);
				
				isRunning = cursor.getInt(6) == 1;
				double v = (double) (isRunning ? t + new Date().getTime()
						- start : t);
				sum += v;
				
				renderer = new SimpleSeriesRenderer();
				color = cursor.getInt(7);
				renderer.setColor(color);
				renderer.setChartValuesFormat(nf);
				if (v != 0.0) {
					nvalues.add(s);
					values.add(v);
					mRenderer.addSeriesRenderer(renderer);
				}
			}
			for (int i1 = 0, cnt1 = values.size(); i1 < cnt1; i1++) {

				mSeries.add(nvalues.get(i1), values.get(i1) / sum);
			}

			if (mChartView != null) {
				mChartView.repaint();
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReload() {
		loadermanager.restartLoader(1, null, this);
	}

	public static DiagramFragment newInstance() {
		DiagramFragment fragment = new DiagramFragment();
		return fragment;
	}

	public static int getRandomColor() {
		Random rnd = new Random();
		return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256),
				rnd.nextInt(256));
	}
}
