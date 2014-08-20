package maximsblog.blogspot.com.timestatistic;

import maximsblog.blogspot.com.timestatistic.AreYouSureResetAllDialogFragment.ResetAllDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class PeriodSetupDialogFragment extends DialogFragment implements OnClickListener {
	
	private IPeriodSetupDialog mListener;
	public interface IPeriodSetupDialog {
		void setupNewPeriod(long time);
	}
	
	public void setPeriodSetupDialog(IPeriodSetupDialog listener) {
		mListener = listener;
	}
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
            .setTitle(R.string.interval)
            .setSingleChoiceItems(R.array.periods, -1, this)
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