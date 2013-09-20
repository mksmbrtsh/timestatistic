package maximsblog.blogspot.com.timestatistic;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class DiagramFragment extends Fragment{
	
	/** Colors to be used for the pie slices. */
	  private static int[] COLORS = new int[] { Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN };
	  /** The main series that will include all the data. */
	  private CategorySeries mSeries = new CategorySeries("");
	  /** The main renderer for the main dataset. */
	  private DefaultRenderer mRenderer = new DefaultRenderer();
	  /** Button for adding entered data to the current series. */
	  private Button mAdd;
	  /** Edit text field for entering the slice value. */
	  private EditText mValue;
	  /** The chart view that displays the data. */
	  private GraphicalView mChartView;
	private View mLayout;

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayout = inflater.inflate(R.layout.fragment_diagram,container, false);
		mValue = (EditText) mLayout.findViewById(R.id.xValue);
	    mRenderer.setZoomButtonsVisible(true);
	    mRenderer.setStartAngle(180);
	    mRenderer.setDisplayValues(true);

	    mAdd = (Button) mLayout.findViewById(R.id.add);
	    mAdd.setEnabled(true);
	    mValue.setEnabled(true);

	    mAdd.setOnClickListener(new View.OnClickListener() {
	      public void onClick(View v) {
	        double value = 0;
	        try {
	          value = Double.parseDouble(mValue.getText().toString());
	        } catch (NumberFormatException e) {
	          mValue.requestFocus();
	          return;
	        }
	        mValue.setText("");
	        mValue.requestFocus();
	        mSeries.add("Series " + (mSeries.getItemCount() + 1), value);
	        SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
	        renderer.setColor(COLORS[(mSeries.getItemCount() - 1) % COLORS.length]);
	        mRenderer.addSeriesRenderer(renderer);
	        mChartView.repaint();
	      }
	    });
	    return mLayout;
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    if (mChartView == null) {
	      LinearLayout layout = (LinearLayout) mLayout.findViewById(R.id.chart);
	      mChartView = ChartFactory.getPieChartView(this.getActivity(), mSeries, mRenderer);
	      mRenderer.setClickEnabled(true);
	      mChartView.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	          SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
	          if (seriesSelection == null) {
	            Toast.makeText(DiagramFragment.this.getActivity(), "No chart element selected", Toast.LENGTH_SHORT)
	                .show();
	          } else {
	            for (int i = 0; i < mSeries.getItemCount(); i++) {
	              mRenderer.getSeriesRendererAt(i).setHighlighted(i == seriesSelection.getPointIndex());
	            }
	            mChartView.repaint();
	            Toast.makeText(
	                DiagramFragment.this.getActivity(),
	                "Chart data point index " + seriesSelection.getPointIndex() + " selected"
	                    + " point value=" + seriesSelection.getValue(), Toast.LENGTH_SHORT).show();
	          }
	        }
	      });
	      layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
	          LayoutParams.FILL_PARENT));
	    } else {
	      mChartView.repaint();
	    }
	  }

}
