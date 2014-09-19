package maximsblog.blogspot.com.timestatistic;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import maximsblog.blogspot.com.timestatistic.R.drawable;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class TopicActivity extends Activity {

	int mTextResourceId = 0;
	private AdView adView;

	/**
	 * onCreate
	 * 
	 * @param savedInstanceState
	 *            Bundle
	 */

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_topic);

		// Read the arguments from the Intent object.
		Intent in = getIntent();
		mTextResourceId = in.getIntExtra(HelpActivity.ARG_TEXT_ID, 0);
		if (mTextResourceId <= 0)
			mTextResourceId = R.string.no_help_available;
		setTitle(getString(R.string.help) + ": "
				+ in.getStringExtra(HelpActivity.ARG_TITLE));
		TextView textView = (TextView) findViewById(R.id.topic_text);
		textView.setMovementMethod(LinkMovementMethod.getInstance());
		textView.setText(Html.fromHtml(getString(mTextResourceId),
				this.imgGetter, null));
		adView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("CF95DC53F383F9A836FD749F3EF439CD").build();
		adView.loadAd(adRequest);
	}

	private ImageGetter imgGetter = new ImageGetter() {

		@Override
		public Drawable getDrawable(String source) {
			Drawable img = null;
			if (source.equals("hlp_main_act")) {
				img = (Drawable) getResources().getDrawable(
						R.drawable.hlp_main_act);
			} else if (source.equals("hlp_cutting_rec")) {
				img = (Drawable) getResources().getDrawable(
						R.drawable.hlp_cutting_rec);
			} else if (source.equals("hlp_edit_rec")) {
				img = (Drawable) getResources().getDrawable(
						R.drawable.hlp_edit_rec);
			} else if (source.equals("hlp_union_rec")) {
				img = (Drawable) getResources().getDrawable(
						R.drawable.hlp_union_rec);
			} else if (source.equals("hlp_union_dlg")) {
				img = (Drawable) getResources().getDrawable(
						R.drawable.hlp_union_dlg);
			}
			img.setBounds(0, 0, img.getIntrinsicWidth(),
					img.getIntrinsicHeight());
			return img;
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		adView.resume();
	}

	@Override
	protected void onPause() {
		adView.pause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		adView.destroy();
		super.onDestroy();
	};
} // end class
