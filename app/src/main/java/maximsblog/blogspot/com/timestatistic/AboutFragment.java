package maximsblog.blogspot.com.timestatistic;

import android.app.Fragment;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.fragment_about,container, false);
		TextView t = (TextView) view.findViewById(R.id.note_text);
		t.setText(getResources().getText(R.string.about_text));
		Linkify.addLinks(t, Linkify.ALL);
		t.setMovementMethod(LinkMovementMethod.getInstance());
		return view;
	}

	public static AboutFragment newInstance() {
		AboutFragment fragment = new AboutFragment();
		return fragment;
	}
}