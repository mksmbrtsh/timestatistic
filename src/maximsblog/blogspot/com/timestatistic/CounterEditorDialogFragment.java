package maximsblog.blogspot.com.timestatistic;

import maximsblog.blogspot.com.timestatistic.ColorPickerDialog.OnColorChangedListener;
import maximsblog.blogspot.com.timestatistic.ColorPickerDialogFragment.ColorCounterDialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class CounterEditorDialogFragment extends DialogFragment implements
		OnClickListener, ColorCounterDialog {
	private EditText mNameEditor;
	private String mName;
	private ICounterEditorDialog mListener;
	private int mId;
	private Button mDelButton;
	private boolean mIsRunning;
	private ImageButton mColorButton;
	private int mColor;
	
	public enum Status {
		ADD,
		EDIT,
		DEL
	}
	
	public interface ICounterEditorDialog {
		void onFinishDialog(String inputText, int id, Status status, boolean isRunning, int color);
	}

	public void setCounterDialogListener(ICounterEditorDialog listener) {
		mListener = listener;
	}
	
	public void setName(String name) {
		mName = name;
	}
	public void setColor(int color) {
		mColor = color;
	}
	
	public void setIdCounter(int id) {
		mId = id;
	}
	
	public void setIsRunning(boolean isRunning)	{
		mIsRunning = isRunning;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().setTitle(R.string.add_counter_dialog);
		View v = inflater.inflate(R.layout.fragment_counter_editor_dialog, null);
		v.findViewById(R.id.ok).setOnClickListener(this);
		v.findViewById(R.id.cancel).setOnClickListener(this);
		mDelButton = (Button) v.findViewById(R.id.del);
		mDelButton.setOnClickListener(this);
		mColorButton = (ImageButton)v.findViewById(R.id.color_imageButton);
		mColorButton.setOnClickListener(this);
		mNameEditor = (EditText) v.findViewById(R.id.name_editor);
		mNameEditor.requestFocus();
		
		getDialog().getWindow().setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return v;
	}
	
	@Override
	public void onResume() {
		mNameEditor.setText(mName);
		if(mId!=-1 && mId != 1){
			mDelButton.setVisibility(View.VISIBLE);
			getDialog().setTitle(R.string.edit_counter_dialog);
		} else {  
			mDelButton.setVisibility(View.INVISIBLE);
			getDialog().setTitle(R.string.add_counter_dialog);
		}
		super.onResume();
	};

	public void onClick(View v) {
		int id = v.getId(); 
		if (id == R.id.ok) {
			mListener.onFinishDialog(mNameEditor.getText().toString(), mId,mId != -1 ? Status.EDIT : Status.ADD, mIsRunning, mColor);
		} else if (id == R.id.del) {
			mListener.onFinishDialog(mNameEditor.getText().toString(), mId, Status.DEL, mIsRunning, mColor);
		} else if (id == R.id.color_imageButton) {
			ColorPickerDialogFragment mColorPickerDialogFragment = new ColorPickerDialogFragment();
			mColorPickerDialogFragment.setColorCounterDialogListener(this);
			mColorPickerDialogFragment.setColor(mColor);
			mColorPickerDialogFragment.show(getActivity().getSupportFragmentManager(),
					"dlg2");
			return;
		}
		dismiss();
	}

	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}

	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
	}
	

	@Override
	public void colorCounterChanged(int newcolor) {
		mColor = newcolor;
	}
}
