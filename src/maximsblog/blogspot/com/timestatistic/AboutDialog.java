package maximsblog.blogspot.com.timestatistic;

import maximsblog.blogspot.com.timestatistic.ColorPickerDialog.ColorPickerView;
import maximsblog.blogspot.com.timestatistic.ColorPickerDialog.OnColorChangedListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AboutDialog extends DialogFragment {
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.fragment_about,container, false);
		TextView t = (TextView) view.findViewById(R.id.textView1);
		t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		t.setClickable(true);
		Linkify.addLinks(t, Linkify.ALL);
		getDialog().setTitle(getString(R.string.app_name));
		try {
			String nameversion = getString(R.string.version) + ": " + getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
			((TextView)view.findViewById(R.id.version_txt)).setText(nameversion);
		} catch (NameNotFoundException e) {

		}
		return view;
	}
}
