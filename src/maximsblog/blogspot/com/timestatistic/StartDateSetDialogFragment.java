package maximsblog.blogspot.com.timestatistic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;

public class StartDateSetDialogFragment extends DialogFragment {
	
	private IRecordDialog mListener;

	
	public void setDialogListener(IRecordDialog listener) {
		mListener = listener;
	}
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	String[] items = getResources().getStringArray(R.array.StartFilters); 
    	int checkedItem = PreferenceManager.getDefaultSharedPreferences(StartDateSetDialogFragment.this.getActivity()).getInt(SettingsActivity.STARTTIMEFILTER, 0);
        return new AlertDialog.Builder(getActivity())
            .setTitle(R.string.startdateset).setSingleChoiceItems(items, checkedItem, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Editor editor = PreferenceManager.getDefaultSharedPreferences(StartDateSetDialogFragment.this.getActivity())
							.edit();
					editor.putInt(SettingsActivity.STARTTIMEFILTER, which);
					editor.commit();
					StartDateSetDialogFragment.this.dismiss();
					mListener.onRefreshFragmentsValue();
				}
			}).create();
    }
}
