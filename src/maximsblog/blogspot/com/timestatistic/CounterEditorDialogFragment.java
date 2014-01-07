package maximsblog.blogspot.com.timestatistic;

import maximsblog.blogspot.com.timestatistic.ColorPickerDialog.OnColorChangedListener;
import maximsblog.blogspot.com.timestatistic.ColorPickerDialogFragment.ColorCounterDialog;
import maximsblog.blogspot.com.timestatistic.SplitRecordDialogFragment.CustomDateTimePickerFragment;
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
	private long mInterval;
	private ICounterEditorDialog mListener;
	private int mId;
	private Button mDelButton;
	private boolean mIsRunning;
	private ImageButton mColorButton;
	private int mColor;
	private EditText mIntervalEditor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mIsRunning = savedInstanceState.getBoolean("mIsRunning");
			mId = savedInstanceState.getInt("mId");
			mColor = savedInstanceState.getInt("mColor");
			mName = savedInstanceState.getString("mName");
			mInterval = savedInstanceState.getLong("mInterval");
		}
		ColorPickerDialogFragment mColorPickerDialogFragment = (ColorPickerDialogFragment) getActivity()
				.getSupportFragmentManager()
				.findFragmentByTag("mColorPickerDialogFragment");
		if (mColorPickerDialogFragment != null) {
			mColorPickerDialogFragment.setColorCounterDialogListener(this);
			mColorPickerDialogFragment.setColor(mColor);
			
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("mIsRunning", mIsRunning);
		outState.putInt("mId", mId);
		outState.putInt("mColor", mColor);
		outState.putString("mName", mNameEditor.getText().toString());
		outState.putLong("mInterval", Long.valueOf(mIntervalEditor.getText().toString()));
		super.onSaveInstanceState(outState);
	}

	
	
	public enum Status {
		ADD,
		EDIT,
		DEL
	}
	
	public interface ICounterEditorDialog {
		void onFinishDialog(String inputText, int id, Status status, boolean isRunning, int color, long interval);
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
		mIntervalEditor = (EditText)v.findViewById(R.id.interval_editor);
		
		getDialog().getWindow().setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return v;
	}
	
	@Override
	public void onResume() {
		mNameEditor.setText(mName);
		mIntervalEditor.setText(String.valueOf(mInterval));
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
			mListener.onFinishDialog(mNameEditor.getText().toString(), mId,mId != -1 ? Status.EDIT : Status.ADD, mIsRunning, mColor, Long.valueOf(mIntervalEditor.getText().toString()));
		} else if (id == R.id.del) {
			mListener.onFinishDialog(mNameEditor.getText().toString(), mId, Status.DEL, mIsRunning, mColor, Long.valueOf(mIntervalEditor.getText().toString()));
		} else if (id == R.id.color_imageButton) {
			ColorPickerDialogFragment mColorPickerDialogFragment = new ColorPickerDialogFragment();
			mColorPickerDialogFragment.setColorCounterDialogListener(this);
			mColorPickerDialogFragment.setColor(mColor);
			mColorPickerDialogFragment.show(getActivity().getSupportFragmentManager(),
					"mColorPickerDialogFragment");
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

	public void setInterval(long interval) {
		mInterval = interval;
	}
}
