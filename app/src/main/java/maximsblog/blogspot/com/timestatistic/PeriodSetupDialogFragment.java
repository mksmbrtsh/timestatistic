package maximsblog.blogspot.com.timestatistic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class PeriodSetupDialogFragment extends DialogFragment implements OnClickListener {
	
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
    	mPeriod = getArguments().getLong(PeriodAnalyseActivity.PERIOD);
    	int index=0;
    	if(mPeriod == 1000*60*60)
    		index = 0;
    	else if(mPeriod == 1000*60*60*24)
    		index = 1;
        	else
        		if(mPeriod == 1000*60*60*24 * 7)
        			index = 2;
        	    	else
        	    		if(mPeriod == 1000*60*60 * 12)
        	    			index = 3;
        return new AlertDialog.Builder(getActivity())
            .setTitle(R.string.interval)
            .setSingleChoiceItems(R.array.periods, index, this)
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
}