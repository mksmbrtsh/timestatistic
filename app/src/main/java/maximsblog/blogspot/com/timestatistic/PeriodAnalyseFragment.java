package maximsblog.blogspot.com.timestatistic;

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import maximsblog.blogspot.com.timestatistic.MainActivity.MainFragments;

public class PeriodAnalyseFragment extends Fragment implements
		LoaderCallbacks<PeriodData>, MainFragments, OnClickListener {
	private final int LOADER_ID = 3;
	private long mPeriod;

	/** The chart view that displays the data. */
	private GraphicalView mChartView;
	private View mLayout;
	private LoaderManager loadermanager;
	private EditText mLegendText;

	private SimpleCursorAdapter sca;

	private TextView mNotFoundText;
	private ProgressBar mProgressBar;
	private View mDiagramLayout;
	private long mStartDate;
	private long mEndDate;
	private PeriodData mPeriodData;
	private XYMultipleSeriesRenderer mRenderer;
	private int[] mIds;
	private boolean[] mChecked;
	private ImageButton mDiagramShare;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadermanager = getLoaderManager();
		mPeriod = getArguments().getLong(PeriodAnalyseActivity.PERIOD);
		mIds = getArguments().getIntArray(PeriodAnalyseActivity.IDS);
		mChecked = getArguments().getBooleanArray(PeriodAnalyseActivity.CHECKED);
		mStartDate = app.getStartDatePeriod(getActivity()).date;
		mEndDate = app.getEndDatePeriod(getActivity()).date;
		if (mEndDate == -1) {
			mEndDate = new Date().getTime();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayout = inflater.inflate(R.layout.fragment_period, container, false);
		mLegendText = (EditText) mLayout.findViewById(R.id.legend);
		mNotFoundText = (TextView) mLayout.findViewById(R.id.not_found);
		mProgressBar = (ProgressBar) mLayout.findViewById(R.id.progress);
		mDiagramLayout = mLayout.findViewById(R.id.ScrollView1);
		mDiagramLayout.setVisibility(View.GONE);
		mNotFoundText.setVisibility(View.GONE);
		mLayout.findViewById(R.id.pad_plus).setOnClickListener(this);
		mLayout.findViewById(R.id.pad_minus).setOnClickListener(this);
		mLayout.findViewById(R.id.pad_reset).setOnClickListener(this);
		mLegendText.setMovementMethod(ScrollingMovementMethod.getInstance());
		mDiagramShare = (ImageButton) mLayout.findViewById(R.id.pad_share);
		mDiagramShare.setOnClickListener(this);
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
		//long start = app.getStartDatePeriod(getActivity()).date;
		//long stop = app.getEndDatePeriod(getActivity()).date;
		ArrayList<Integer> ids = new ArrayList<Integer>(); 
		for (int i = 0; i < mIds.length; i++) {
			if(mChecked[i])
				ids.add(mIds[i]);
		}
		return new XYMultipleSeriesDatasetLoader(getActivity(), mStartDate, mEndDate, mPeriod, ids);
	}

	@Override
	public void onLoadFinished(Loader<PeriodData> arg0, PeriodData arg1) {
		mProgressBar.setVisibility(View.GONE);
		if (arg1 == null || arg1.dataset.getSeriesCount() == 0) {
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
		mRenderer = arg1.renderer;
		mChartView = ChartFactory.getTimeChartView(getActivity(), arg1.dataset,
				arg1.renderer, null);
		layout.addView(mChartView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		if (mChartView != null) {
			mChartView.repaint();
		}
		mLegendText.setText(Html.fromHtml(arg1.legend));
		mPeriodData = arg1;
		/*StringBuilder sb = new StringBuilder();
		SimpleDateFormat df = new SimpleDateFormat("d MMMM");
		for (int i1 = 0, cnt = mPeriodData.dataset.getSeriesCount(); i1 < cnt; i1++) {
			XYSeries m = mPeriodData.dataset.getSeriesAt(i1);
			sb.append(m.getTitle() + "\n");
			for (int i2 = 0, cnt2 = m.getItemCount(); i2 < cnt2; i2++) {
				sb.append("x=");
				sb.append(df.format(new Date((long) m.getX(i2))));
				sb.append(" y=");
				sb.append(getLabel(m.getY(i2) == MathHelper.NULL_VALUE ? 0 : m.getY(i2)));
				sb.append("\n");
			}
			sb.append("\n");
		}
		mLegendText.setText(sb.toString());*/

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
		s = String.format("%d:%02d", hours + day * 24, minutes);
		return s;
	}

	@Override
	public void onLoaderReset(Loader<PeriodData> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pad_plus:
			mChartView.zoomIn();
			return;
		case R.id.pad_minus:
			mChartView.zoomOut();
			return;
		case R.id.pad_reset:
			mChartView.zoomReset();
			return;
		case R.id.pad_share:
			Bitmap b = mChartView.toBitmap();
			app.BitmapShare(getActivity(), b);
			b.recycle();
			return;
		default:
			break;
		}
		double oldMaxX = mRenderer.getXAxisMax();
		double oldMinX = mRenderer.getXAxisMin();
		double oldMaxY = mRenderer.getYAxisMax();
		double oldMinY = mRenderer.getYAxisMin();
		double deltaX = oldMaxX - oldMinX;
		double deltaY = oldMaxY - oldMinY;
		double maxX = oldMaxX;
		double minX = oldMinX;
		double maxY = oldMaxY;
		double minY = oldMinY;
		
		mRenderer.setXAxisMin(minX);
		mRenderer.setXAxisMax(maxX);
		mRenderer.setYAxisMin(minY);
		mRenderer.setYAxisMax(maxY);
		mChartView.repaint();
	}
}