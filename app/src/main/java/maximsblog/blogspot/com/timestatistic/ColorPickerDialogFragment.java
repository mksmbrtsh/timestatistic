package maximsblog.blogspot.com.timestatistic;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import maximsblog.blogspot.com.timestatistic.ColorPickerDialog.OnColorChangedListener;

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
