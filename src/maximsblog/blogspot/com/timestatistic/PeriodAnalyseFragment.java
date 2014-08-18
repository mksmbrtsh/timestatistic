package maximsblog.blogspot.com.timestatistic;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.zip.DataFormatException;

import maximsblog.blogspot.com.timestatistic.MainActivity.MainFragments;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.TimeChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class PeriodAnalyseFragment extends Fragment implements
		LoaderCallbacks<PeriodData>, MainFragments {
	private final int LOADER_ID = 3;
	private final long PERIOD = 1000 * 60 * 60 * 24;

	/** The chart view that displays the data. */
	private GraphicalView mChartView;
	private View mLayout;
	private LoaderManager loadermanager;
	private TextView mLegendText;

	private SimpleCursorAdapter sca;

	private TextView mNotFoundText;
	private ProgressBar mProgressBar;
	private View mDiagramLayout;
	private long mStartDate;
	private long mEndDate;
	private PeriodData mPeriodData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		loadermanager = getLoaderManager();
		super.onCreate(savedInstanceState);
		mStartDate = app.getStartDate(getActivity()).date;
		mEndDate = app.getEndDate(getActivity()).date;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayout = inflater.inflate(R.layout.fragment_period, container, false);
		mLegendText = (TextView) mLayout.findViewById(R.id.legend);
		mNotFoundText = (TextView) mLayout.findViewById(R.id.not_found);
		mProgressBar = (ProgressBar) mLayout.findViewById(R.id.progress);
		mDiagramLayout = mLayout.findViewById(R.id.ScrollView1);
		mDiagramLayout.setVisibility(View.GONE);
		mNotFoundText.setVisibility(View.GONE);
		return mLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
	};

	@Override
	public void onResume() {
		super.onResume();
		loadermanager.initLoader(LOADER_ID, null, this);
	}

	@Override
	public void onPause() {
		super.onPause();
		ViewGroup chartLayout = (ViewGroup) mLayout.findViewById(R.id.chart);
		chartLayout.removeView(mChartView);
		mChartView = null;
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
	};

	public static PeriodAnalyseFragment newInstance() {
		PeriodAnalyseFragment fragment = new PeriodAnalyseFragment();
		return fragment;
	}

	public static int getRandomColor() {
		Random rnd = new Random();
		return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256),
				rnd.nextInt(256));
	}

	@Override
	public void onReload() {
		// TODO Auto-generated method stub

	}

	@Override
	public Loader<PeriodData> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		long start = app.getStartDate(getActivity()).date;
		long stop = app.getEndDate(getActivity()).date;
		return new XYMultipleSeriesDatasetLoader(getActivity(), start, stop);
	}

	@Override
	public void onLoadFinished(Loader<PeriodData> arg0, PeriodData arg1) {
		mProgressBar.setVisibility(View.GONE);
		if (arg1 == null) {
			mNotFoundText.setVisibility(View.VISIBLE);
			mDiagramLayout.setVisibility(View.GONE);
			return;
		} else {
			mNotFoundText.setVisibility(View.GONE);
			mDiagramLayout.setVisibility(View.VISIBLE);
		}
		if (mChartView != null) {
			ViewGroup chartLayout = (ViewGroup) mLayout
					.findViewById(R.id.chart);
			chartLayout.removeView(mChartView);
			mChartView = null;
		}

		final ViewGroup layout = (ViewGroup) mLayout.findViewById(R.id.chart);
		mChartView = ChartFactory.getTimeChartView(getActivity(), arg1.dataset,
				arg1.renderer, null);
		layout.post(new Runnable() {
			
			@Override
			public void run() {
				int height = mLayout.getHeight()
						- 2
						* ((SherlockFragmentActivity) getActivity())
								.getSupportActionBar().getHeight();
				int width = mLayout.getWidth();
				layout.addView(mChartView, new LayoutParams(width, height));

				if (mChartView != null) {
					mChartView.repaint();
				}
			}
		});
		
		//mLegendText.setText(Html.fromHtml(arg1.legend));
		mPeriodData = arg1;
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat df = new SimpleDateFormat("d MMMM");
		for(int i1 = 0, cnt = mPeriodData.dataset.getSeriesCount(); i1 < cnt; i1++) {
			XYSeries m = mPeriodData.dataset.getSeriesAt(i1);
			sb.append(m.getTitle()+"\n");
			for(int i2 = 0, cnt2 = m.getItemCount(); i2 < cnt2; i2++) {
				sb.append("x=");
				sb.append(df.format(new Date((long)m.getX(i2))));
				sb.append(" y=");
				sb.append(getLabel(m.getY(i2)));
				sb.append("\n");
			}
			sb.append("\n");
		}
		mLegendText.setText(sb.toString());
	}

	private String getLabel(double time) {
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
	        s = String.format("%d:%02d", hours + day*24, minutes);
	    return s;
	  }
	
	@Override
	public void onLoaderReset(Loader<PeriodData> arg0) {
		// TODO Auto-generated method stub

	}
}