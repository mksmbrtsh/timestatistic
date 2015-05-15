package maximsblog.blogspot.com.timestatistic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HistoryFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.fragment_about,container, false);
		TextView t = (TextView) view.findViewById(R.id.note_text);
		t.setText(getResources().getText(R.string.history_text));
		t.setMovementMethod(LinkMovementMethod.getInstance());
		return view;
	}

	public static HistoryFragment newInstance() {
		HistoryFragment fragment = new HistoryFragment();
		return fragment;
	}
}