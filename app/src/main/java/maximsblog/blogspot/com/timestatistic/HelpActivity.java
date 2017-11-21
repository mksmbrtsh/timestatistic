package maximsblog.blogspot.com.timestatistic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class HelpActivity extends Activity {

	public static final String ARG_TEXT_ID = "text_id";
	public static final String ARG_TITLE = "title";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		setTitle(getString(R.string.help));
		TextView textView = (TextView) findViewById(R.id.help_page_intro);
		if (textView != null) {
			textView.setMovementMethod(LinkMovementMethod.getInstance());
			textView.setText(Html.fromHtml(
					getString(R.string.help_page_intro_html)));
		}
	}

	/**
	 * Start a TopicActivity and show the text indicated by argument 1.
	 * 
	 * @param textId
	 *            int - resource id of the text to show
	 * @return void
	 */

	public void startInfoActivity(int textId, String text) {
		if (textId >= 0) {
			Intent intent = (new Intent(this, TopicActivity.class));
			intent.putExtra(ARG_TEXT_ID, textId);
			intent.putExtra(ARG_TITLE, text);
			startActivity(intent);
		} else {
			toast("No information is available for topic: " + textId, true);
		}
	} // end startInfoActivity

	/**
	 * Handle the click of one of the help buttons on the page. Start an
	 * activity to display the help text for the topic selected.
	 * 
	 * @param v
	 *            View
	 * @return void
	 */

	public void onClickHelp(View v) {
		int id = v.getId();
		int textId = -1;
		switch (id) {
		case R.id.help_button1:
			textId = R.string.topic_section1;
			break;
		case R.id.help_button2:
			textId = R.string.topic_section2;
			break;
		case R.id.help_button3:
			textId = R.string.topic_section3;
			break;
		case R.id.help_button4:
			textId = R.string.topic_section4;
			break;
		default:
			break;
		}

		if (textId >= 0)
			startInfoActivity(textId, ((Button)v).getText().toString());
		else
			toast("Detailed Help for that topic is not available.", true);
	}

	/**
	 * Show a string on the screen via Toast.
	 * 
	 * @param msg
	 *            String
	 * @param longLength
	 *            boolean - show message a long time
	 * @return void
	 */

	public void toast(String msg, boolean longLength) {
		Toast.makeText(getApplicationContext(), msg,
				(longLength ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT)).show();
	}

	
}
