package maximsblog.blogspot.com.timestatistic;

import maximsblog.blogspot.com.timestatistic.ColorPickerDialogFragment.ColorCounterDialog;
import android.R.anim;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class CountSettingsActivity extends FragmentActivity implements OnClickListener, OnSeekBarChangeListener {

	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private View mExampleView;

	private int mBackgroundResource;
	private int mCalendarTextColor;
	private int mBackgroundCounter;
	private TextView mValueText;
	private SeekBar mTransparentSeekBar;
	private View mBackgroundCounterExample;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_count_settings_activity);
		Button ok = (Button) findViewById(R.id.ok);
		ok.setOnClickListener(this);
		mExampleView = findViewById(R.id.background);
		mBackgroundCounterExample = findViewById(R.id.background_counter);
		mValueText = (TextView) findViewById(R.id.value_text);
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
				android.R.color.white, android.R.color.transparent,
				R.drawable.glass_bg_black, R.drawable.glass_bg_blackmatte,
				R.drawable.glass_bg_blood, R.drawable.glass_bg_blue,
				R.drawable.glass_bg_charcoal, R.drawable.glass_bg_clear,
				R.drawable.glass_bg_darkblue, R.drawable.glass_bg_darkteal,
				R.drawable.glass_bg_frost, R.drawable.glass_bg_gold,
				R.drawable.glass_bg_green, R.drawable.glass_bg_grey,
				R.drawable.glass_bg_lav, R.drawable.glass_bg_lime,
				R.drawable.glass_bg_metal, R.drawable.glass_bg_pink,
				R.drawable.glass_bg_platinum, R.drawable.glass_bg_purple,
				R.drawable.glass_bg_red, R.drawable.glass_bg_sapphire,
				R.drawable.glass_bg_skyblue, R.drawable.appwidget_bg, R.drawable.appwidget_bg_old, R.drawable.appwidget_dark_bg, R.drawable.appwidget_dark_bg_old };
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
			mBackgroundResource = R.drawable.glass_bg_blood;
			mCalendarTextColor = 0xFFFFFFFF;
			mExampleView.setBackgroundResource(mBackgroundResource);
			mValueText.setTextColor(mCalendarTextColor);
			mTransparentSeekBar.setProgress(255);
			mBackgroundCounter = 0xFFFF0000;
			mBackgroundCounterExample.setBackgroundColor(mBackgroundCounter);
		} else {
			mBackgroundResource = savedInstanceState
					.getInt("mBackgroundResource");
			mExampleView.setBackgroundResource(mBackgroundResource);
			mValueText.setText(savedInstanceState.getString("mValueText"));
			mCalendarTextColor = savedInstanceState
					.getInt("mCalendarTextColor");
			mValueText.setTextColor(mCalendarTextColor);
			mTransparentSeekBar.setProgress(savedInstanceState
					.getInt("mProgress"));
			mBackgroundCounter = savedInstanceState.getInt("mBackgroundCounter");
			mBackgroundCounterExample.setBackgroundColor(mBackgroundCounter);
		}
	}
	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("mBackgroundResource", mBackgroundResource);
		outState.putString("mValueText", mValueText.getText().toString());
		outState.putInt("mCalendarTextColor", mCalendarTextColor);
		outState.putInt("mProgress", mTransparentSeekBar.getProgress());
		outState.putInt("mBackgroundCounter", mBackgroundCounter);
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
}