package maximsblog.blogspot.com.timestatistic;

import maximsblog.blogspot.com.timestatistic.AreYouSureResetAllDialog.ResetAllDialog;
import maximsblog.blogspot.com.timestatistic.ColorPickerDialog.OnColorChangedListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public class ColorPickerDialogFragment extends DialogFragment implements OnColorChangedListener {
	
	private ColorCounterDialog mListener;
	private int mColor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mColor = savedInstanceState.getInt("mColor");
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("mColor", ((ColorPickerDialog)getDialog()).getColor());
		super.onSaveInstanceState(outState);
	}
	
	
	
	public interface ColorCounterDialog {
		void colorCounterChanged(int newcolor);
	}
	
	public void setColor(int color){
		mColor = color;
	}
	
	public void setColorCounterDialogListener(ColorCounterDialog listener) {
		mListener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	};
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	ColorPickerDialog cpd = new ColorPickerDialog(getActivity(), this, mColor);
    	return cpd;
    }
    
	@Override
	public void colorChanged(int color) {
		ColorPickerDialogFragment.this.dismiss();
		mListener.colorCounterChanged(color);
	}
}
