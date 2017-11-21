package maximsblog.blogspot.com.timestatistic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class AreYouSureResetAllDialogFragment extends DialogFragment {
	
	private ResetAllDialog mListener;
	public interface ResetAllDialog {
		void onResetAllDialog();
	}
	
	public void setResetAllDialogListener(ResetAllDialog listener) {
		mListener = listener;
	}
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
            .setTitle(R.string.reset_all)
            .setMessage(R.string.are_you_sure)
            .setNegativeButton(android.R.string.no, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing (will close dialog)
                	AreYouSureResetAllDialogFragment.this.dismiss();
                }
            })
            .setPositiveButton(android.R.string.yes,  new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	AreYouSureResetAllDialogFragment.this.dismiss();
                	mListener.onResetAllDialog();
                }
            })
            .create();
    }
}