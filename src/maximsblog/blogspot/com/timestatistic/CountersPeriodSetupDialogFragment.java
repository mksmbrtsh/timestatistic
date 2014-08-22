package maximsblog.blogspot.com.timestatistic;

import java.util.ArrayList;
import java.util.List;

import maximsblog.blogspot.com.timestatistic.AreYouSureResetAllDialogFragment.ResetAllDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class CountersPeriodSetupDialogFragment extends DialogFragment implements OnClickListener, OnMultiChoiceClickListener {
	
	private IPeriodSetupDialog mListener;
	private long mPeriod;
	public interface IPeriodSetupDialog {
		void setupNewPeriod(long time);
	}
	public void setPeriodSetupDialog(IPeriodSetupDialog listener) {
		mListener = listener;
	}
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	Cursor newtimers = getActivity().getContentResolver().query(
				RecordsDbHelper.CONTENT_URI_TIMES, null, null, null, RecordsDbHelper.SORTID);
    	int[] ids = new int[newtimers.getCount()];
    	String[] names = new String[newtimers.getCount()] ;
    	boolean[] checked = new boolean[newtimers.getCount()];
    	for (int i1 = 0, cnt1 = newtimers.getCount(); i1 < cnt1; i1++) {
			newtimers.moveToPosition(i1);
			ids[i1]= (newtimers.getInt(4));
			names[i1]=(newtimers.getString(5));
			checked[i1]=(false);
		}
    	newtimers.close();
        return new AlertDialog.Builder(getActivity())
            .setTitle(R.string.interval)
            .setMultiChoiceItems(names, checked , this)
            .create();
    }
	@Override
	public void onClick(DialogInterface dialog, int which) {
		long time;
		switch (which) {
		case 0:
			time = 1000*60*60;
			break;
		case 1:
			time = 1000*60*60*24;
			break;
		case 2:
			time = 1000*60*60*24 * 7;
			break;
		case 3:
			time = 1000*60*60 * 12;
			break;
		default:
			time = 1000*60*60;
			break;
		}
		mListener.setupNewPeriod(time);
		this.dismiss();
	}
	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		// TODO Auto-generated method stub
		
	}
}