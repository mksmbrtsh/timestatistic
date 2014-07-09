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
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class DiagramFragment extends Fragment implements
		LoaderCallbacks<Cursor>, MainFragments {
	private final int LOADER_ID = 3;
	
	/** The main series that will include all the data. */
	private CategorySeries mSeries = new CategorySeries("");
	/** The main renderer for the main dataset. */
	private DefaultRenderer mRenderer = new DefaultRenderer();

	/** The chart view that displays the data. */
	private GraphicalView mChartView;
	private View mLayout;
	private LoaderManager loadermanager;
	private TextView mLegendText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		loadermanager = getLoaderManager();
		super.onCreate(savedInstanceState);
		mRenderer.setZoomButtonsVisible(false);
		mRenderer.setStartAngle(270);
		mRenderer.setDisplayValues(false);
		mRenderer.setInScroll(false);
		mRenderer.setClickEnabled(false);
		mRenderer.setPanEnabled(false);
		mRenderer.setZoomEnabled(false);
		mRenderer.setShowLabels(true);
		mRenderer.setFitLegend(false);
		mRenderer.setShowLegend(false);
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
		mLegendText = (TextView)mLayout.findViewById(R.id.legend);
		mLayout.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
		return mLayout;
	}
	private OnGlobalLayoutListener mGlobalLayoutListener = new OnGlobalLayoutListener() {
	    public void onGlobalLayout() {
	        mLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	        if (mChartView == null) {
				ViewGroup layout = (ViewGroup) mLayout.findViewById(R.id.chart);
				mChartView = ChartFactory.getPieChartView(getActivity(),
						mSeries, mRenderer);
		        int height = mLayout.getHeight() - 2
						* ((SherlockFragmentActivity) getActivity())
						.getSupportActionBar().getHeight();;
		        int width = mLayout.getWidth();
				

				layout.addView(mChartView, new LayoutParams(width, height));
			}
	    }
	};
	
	@Override
	public void onResume() {
		super.onResume();
		if (mChartView == null) {
			mLayout.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
		}
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
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] selectionArgs = new String[] {
				String.valueOf(app.getStartDate(getActivity()).date),
				String.valueOf(app.getEndDate(getActivity()).date) };
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
			ArrayList<Integer> c = new ArrayList<Integer>();
			cursor.moveToFirst();
			ArrayList<Double> values = new ArrayList<Double>();
			ArrayList<String> nvalues = new ArrayList<String>();
			long id = cursor.getLong(0);
			long t = cursor.getLong(2);
			long start = cursor.getLong(3);
			long startdate = app.getStartDate(getActivity()).date;
			long enddate = app.getEndDate(getActivity()).date;
			if(enddate < startdate && enddate != -1) {
				
				return;
			}
			long now = new Date().getTime();
			if (start < startdate)
				start = startdate;
			String s = cursor.getString(5);
			boolean isRunning = cursor.getInt(6) == 1;
			Double sum = 0.0;
			if (isRunning) {
				if (now > enddate && enddate != -1) {
					sum = (double) enddate - start;
				} else
					sum = (double) t + now - start;
			} else {
				if(start + t < enddate || enddate == -1)
					sum = (double) t;
				else
					sum = (double) (enddate - start);
			}

			SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
			int color = cursor.getInt(7);
			
			renderer.setColor(color);
			NumberFormat nf = NumberFormat.getPercentInstance();
			nf.setMaximumFractionDigits(1);
			renderer.setChartValuesFormat(nf);
			if (sum != 0.0) {
				nvalues.add(s);
				values.add(sum);
				c.add(color);
				mRenderer.addSeriesRenderer(renderer);
			}
			while (cursor.moveToNext()) {
				id = cursor.getLong(0);
				t = cursor.getLong(2);
				start = cursor.getLong(3);
				s = cursor.getString(5);
				isRunning = cursor.getInt(6) == 1;
				double v;
				if (isRunning) {
					if (now > enddate && enddate != -1) {
						v = (double) enddate - start;
					} else
						v = (double) t + now - start;
				} else {
					if(start + t < enddate || enddate == -1)
						v = (double) t;
					else
						v = (double) (enddate - start);
				}
				sum += v;

				renderer = new SimpleSeriesRenderer();
				color = cursor.getInt(7);
				renderer.setColor(color);
				renderer.setChartValuesFormat(nf);
				if (v != 0.0) {
					nvalues.add(s);
					values.add(v);
					c.add(color);
					mRenderer.addSeriesRenderer(renderer);
				}
			}
			if (now < enddate && enddate != -1) {
				renderer = new SimpleSeriesRenderer();
				color = 0;
				c.add(color);
				renderer.setColor(color);
				renderer.setChartValuesFormat(nf);
				mRenderer.addSeriesRenderer(renderer);
				nvalues.add(getString(R.string.future));
				values.add((double) enddate - now);
				sum += enddate - now;
			}
			StringBuilder sb = new StringBuilder();
			for (int i1 = 0, cnt1 = values.size(); i1 < cnt1; i1++) {
				sb.append("<font color=\"#");
				sb.append(String.format("%08X", c.get(i1)).substring(2));
				sb.append("\">");
				sb.append("<big>&#9679;</big> ");
				sb.append("</font>");
				sb.append(nvalues.get(i1));
				sb.append(':');
					sb.append(' ');
				sb.append(nf.format(values.get(i1) / sum));
				sb.append(' ');
				sb.append('(');
				sb.append(getTime(values.get(i1)));
				sb.append(')');
				sb.append("<br>");
				mSeries.add(nvalues.get(i1), values.get(i1) / sum);
			}

			if (mChartView != null) {
				mChartView.repaint();
			}
			mLegendText.setText(Html.fromHtml(sb.toString()));
		}
	}

	public String getTime(double time)
	{
		int day;
		int hours;
		int minutes;
		int seconds;
		day = (int) (time / (24 * 60 * 60 * 1000));
		hours = (int) (time / (60 * 60 * 1000)) - day * 24;
		minutes = (int) (time / (60 * 1000)) - day * 24 * 60 - 60* hours;
		seconds = (int) (time / 1000) - day * 24 * 60 * 60 - 60 * 60
				* hours - 60 * minutes;
		String s = new String();
		if(day>0)
		{
			s = String.format("%s\n%02d:%02d:%02d",getTimeString("day", day), hours, minutes, seconds);
		} else
			s = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		return s;
	}
	
	private String getTimeString(String res, int l) {
		StringBuilder s = new StringBuilder();
		s.append(l);
		s.append(' ');
		if (l == 1 || (l % 10 == 1 && l != 11)) {
			s.append(getString(getResources().getIdentifier(
					res + "1", "string", getActivity().getPackageName())));
		} else if ((l % 10 == 2 || l % 10 == 3 || l % 10 == 4) && l != 12
				&& l != 13 && l != 14) {
			s.append(getString(getResources().getIdentifier(
					res + "234", "string", getActivity().getPackageName())));
		} else
			s.append(getString(getResources().getIdentifier(
					res + "s", "string", getActivity().getPackageName())));
		return s.toString();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReload() {
		loadermanager.restartLoader(LOADER_ID, null, this);
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