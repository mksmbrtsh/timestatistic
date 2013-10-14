package maximsblog.blogspot.com.timestatistic;

import java.util.ArrayList;
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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayout = inflater.inflate(R.layout.fragment_diagram, container, false);
		mRenderer.setZoomButtonsVisible(false);
		mRenderer.setStartAngle(180);
		mRenderer.setDisplayValues(true);
		mRenderer.setInScroll(false);
		return mLayout;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mChartView == null) {
			ViewGroup layout = (ViewGroup) mLayout.findViewById(R.id.chart);
			mChartView = ChartFactory.getPieChartView(this.getActivity(),
					mSeries, mRenderer);
			mRenderer.setClickEnabled(true);
			mRenderer.setPanEnabled(false);
			mRenderer.setZoomEnabled(false);
			mRenderer.setDisplayValues(false);
			mRenderer.setShowLabels(false);
			mRenderer.setLegendTextSize(30);
			mRenderer.setLegendHeight(50);
			
			mChartView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					SeriesSelection seriesSelection = mChartView
							.getCurrentSeriesAndPoint();
					if (seriesSelection == null) {

					} else {
						for (int i = 0; i < mSeries.getItemCount(); i++) {
							mRenderer.getSeriesRendererAt(i).setHighlighted(
									i == seriesSelection.getPointIndex());
						}
						mChartView.repaint();

					}
				}
			});
			layout.addView(mChartView, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		} else {
			mChartView.repaint();
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
		CursorLoader loader = new CursorLoader(this.getActivity(),
				RecordsDbHelper.CONTENT_URI_TIMES, null, null, null, null);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mRenderer.removeAllRenderers();
		if (cursor != null) {
			mSeries.clear();
			ArrayList<Color> c = new ArrayList<Color>();
			cursor.moveToFirst();
			long id = cursor.getLong(0);
			long t = cursor.getLong(2);
			long start = cursor.getLong(3);
			String s = cursor.getString(5);
			boolean isRunning = cursor.getInt(6) == 1;
			mSeries.add(s == null ? "" : s, isRunning ? t + new Date().getTime()
					- start : t);
			SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
			int color = cursor.getInt(7);
			renderer.setColor(color);
			mRenderer.addSeriesRenderer(renderer);
			while (cursor.moveToNext()) {
				id = cursor.getLong(0);
				t = cursor.getLong(2);
				start = cursor.getLong(3);
				s = cursor.getString(5);
				isRunning = cursor.getInt(6) == 1;
				mSeries.add(s == null ? "" : s,
						isRunning ? t + new Date().getTime() - start : t);
				renderer = new SimpleSeriesRenderer();
				color = cursor.getInt(7);
				renderer.setColor(color);
				mRenderer.addSeriesRenderer(renderer);
			}

			if (mChartView != null)
				mChartView.repaint();
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
		return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));   
	}
}
