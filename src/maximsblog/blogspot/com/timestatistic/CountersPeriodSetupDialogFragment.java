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

public class CountersPeriodSetupDialogFragment extends DialogFragment implements OnMultiChoiceClickListener, OnClickListener {
	
	private IPeriodSetupDialog mListener;
	private long mPeriod;
	private boolean[] mChecked;
	private int[] mIds;
	
	public interface IPeriodSetupDialog {
		void setupCounters(int[] ids, boolean[] checked);
	}
	public void setPeriodSetupDialog(IPeriodSetupDialog listener) {
		mListener = listener;
	}
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	mChecked = getArguments().getBooleanArray("checked");
    	mIds = getArguments().getIntArray("ids");
    	Cursor newtimers = getActivity().getContentResolver().query(
				RecordsDbHelper.CONTENT_URI_TIMES, null, null, null, RecordsDbHelper.SORTID);
    	//mIds = new int[newtimers.getCount()];
    	String[] names = new String[newtimers.getCount()] ;
    	//mChecked = new boolean[newtimers.getCount()];
    	for (int i1 = 0, cnt1 = newtimers.getCount(); i1 < cnt1; i1++) {
			newtimers.moveToPosition(i1);
			//mIds[i1]= (newtimers.getInt(4));
			names[i1]=(newtimers.getString(5));
			//mChecked[i1]=(false);
		}
    	newtimers.close();
        return new AlertDialog.Builder(getActivity())
            .setTitle(R.string.interval)
            .setMultiChoiceItems(names, mChecked , this).setPositiveButton(android.R.string.ok, this)
            .create();
    }
	
	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		//mChecked[which] = !mChecked[which];		
	}
	@Override
	public void onClick(DialogInterface dialog, int which) {
		this.dismiss();
		mListener.setupCounters(mIds, mChecked);
	}
	
}