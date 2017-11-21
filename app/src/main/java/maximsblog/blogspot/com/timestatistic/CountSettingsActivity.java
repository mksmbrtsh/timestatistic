package maximsblog.blogspot.com.timestatistic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class CountSettingsActivity extends Activity implements OnClickListener, OnSeekBarChangeListener, OnCheckedChangeListener {

	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private View mExampleView;

	private int mBackgroundResource;
	private int mCalendarTextColor;
	private int mBackgroundCounter;
	private TextView mValueText;
	private SeekBar mTransparentSeekBar;
	private View mBackgroundCounterExample;
	private EditText mFontSize;
	private CheckBox mSwitchOnOff;
	private Spinner mCountersSpinner;

	/** Called when the activity is first created. */
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_count_settings_activity);
		Button ok = (Button) findViewById(R.id.ok);
		ok.setOnClickListener(this);
		mExampleView = findViewById(R.id.background);
		mBackgroundCounterExample = findViewById(R.id.background_counter);
		mValueText = (TextView) findViewById(R.id.status_text);
		mFontSize = (EditText)findViewById(R.id.fontSize_txt);
		mSwitchOnOff = (CheckBox)findViewById(R.id.switch_onoff);
		mSwitchOnOff.setOnCheckedChangeListener(this);
		mCountersSpinner = (Spinner)findViewById(R.id.counters_spinner);
		mCountersSpinner.setEnabled(false);
		mTransparentSeekBar = (SeekBar)findViewById(R.id.transparent_background);
		mTransparentSeekBar.setOnSeekBarChangeListener(this);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
			return;
		}
		

		LinearLayout mBackgroundsGallery = (LinearLayout) findViewById(R.id.background_gallery);
		int[] backgrounds = new int[] { android.R.color.black,
				android.R.color.white, android.R.color.transparent};
		for (final int background : backgrounds) {
			final ImageView imageView = new ImageView(getApplicationContext());
			imageView.setLayoutParams(new ViewGroup.LayoutParams(70, 70));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setImageResource(background);
			imageView.setPadding(10, 10, 5, 5);
			imageView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {
						mBackgroundResource = background;
						mExampleView.setBackgroundResource(background);
				}
			});
			mBackgroundsGallery.addView(imageView);
		}
		if (savedInstanceState == null) {
			mBackgroundResource = android.R.color.black;
			mCalendarTextColor = 0xFFFFFFFF;
			mExampleView.setBackgroundResource(mBackgroundResource);
			mValueText.setTextColor(mCalendarTextColor);
			mFontSize.setText("20");
			mValueText.setTextSize(20);
			mTransparentSeekBar.setProgress(255);
			mBackgroundCounter = 0xFFFF0000;
			mBackgroundCounterExample.setBackgroundColor(mBackgroundCounter);
		} else {
			mBackgroundResource = savedInstanceState
					.getInt("mBackgroundResource");
			mExampleView.setBackgroundResource(mBackgroundResource);
			mValueText.setText(savedInstanceState.getString("mValueText"));
			mValueText.setTextSize(savedInstanceState.getInt("mValueSize"));
			mFontSize.setText(String.valueOf(mValueText.getTextSize()));
			mCalendarTextColor = savedInstanceState
					.getInt("mCalendarTextColor");
			mValueText.setTextColor(mCalendarTextColor);
			mTransparentSeekBar.setProgress(savedInstanceState
					.getInt("mProgress"));
			mBackgroundCounter = savedInstanceState.getInt("mBackgroundCounter");
			mBackgroundCounterExample.setBackgroundColor(mBackgroundCounter);
			mSwitchOnOff.setChecked(savedInstanceState.getBoolean("mSwitchOnOff"));
		}
		mFontSize.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					int size = Integer.valueOf(s.toString());
					mValueText.setTextSize(size);
				}
				catch(NumberFormatException e){
					
				}
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		Cursor timers = getContentResolver().query(
				RecordsDbHelper.CONTENT_URI_TIMES, null, null, null, RecordsDbHelper.SORTID);
		String[] from = { RecordsDbHelper.NAME };
		int[] to = { android.R.id.text1 };
		SimpleCursorAdapter currentCounterAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_spinner_item, timers, from, to,0);
		currentCounterAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCountersSpinner.setAdapter(currentCounterAdapter);
	}
	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("mBackgroundResource", mBackgroundResource);
		outState.putString("mValueText", mValueText.getText().toString());
		outState.putInt("mCalendarTextColor", mCalendarTextColor);
		outState.putInt("mProgress", mTransparentSeekBar.getProgress());
		outState.putInt("mBackgroundCounter", mBackgroundCounter);
		outState.putInt("mValueSize", (int)mValueText.getTextSize());
		outState.putBoolean("mSwitchOnOff", mSwitchOnOff.isChecked());
		super.onSaveInstanceState(outState);
	};

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.ok) {
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					mAppWidgetId);
			Editor editor = PreferenceManager.getDefaultSharedPreferences(this)
					.edit();
			editor.putInt("dc_background_" + mAppWidgetId, mBackgroundResource);
			editor.putInt("dc_transparent_" + mAppWidgetId, mTransparentSeekBar.getProgress());
			editor.putInt("dc_fontsize_" + mAppWidgetId, (int)(mValueText.getTextSize()/ getResources().getDisplayMetrics().scaledDensity));
			Cursor c = ((SimpleCursorAdapter) mCountersSpinner.getAdapter())
					.getCursor();
			c.moveToPosition(mCountersSpinner.getSelectedItemPosition());
			if(mSwitchOnOff.isChecked())
				editor.putInt("dc_selected_count_" + mAppWidgetId, c.getInt(4));
			editor.commit();
			setResult(RESULT_OK, resultValue);
			CountWidgetProvider.updateWidget(this, mAppWidgetId);
			finish();
			return;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int backgroundCounterAlpha,
			boolean fromUser) {
		int color = mBackgroundCounter;
		int intColor = ~0xFFFFFF | (0xFFFFFF & ~color);
		color = ( backgroundCounterAlpha << 24 ) | ( color & 0x00ffffff );
		mBackgroundCounterExample.setBackgroundColor(mBackgroundCounter = color);
		mValueText.setTextColor(intColor);
	}


	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			mCountersSpinner.setEnabled(isChecked);
	}
}