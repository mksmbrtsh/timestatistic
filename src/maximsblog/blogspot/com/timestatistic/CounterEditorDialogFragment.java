package maximsblog.blogspot.com.timestatistic;

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

public class CounterEditorDialogFragment extends DialogFragment implements
		OnClickListener {
	private EditText mNameEditor;
	private String mName;
	private ICounterEditorDialog mListener;
	private int mId;
	private Button mDelButton;
	private boolean mIsRunning;
	
	public enum Status {
		ADD,
		EDIT,
		DEL
	}
	
	public interface ICounterEditorDialog {
		void onFinishDialog(String inputText, int id, Status status, boolean isRunning);
	}

	public void setCounterDialogListener(ICounterEditorDialog listener) {
		mListener = listener;
	}
	
	public void setName(String name)
	{
		mName = name;
	}
	
	public void setIdCounter(int id)
	{
		mId = id;
	}
	
	public void setIsRunning(boolean isRunning)
	{
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
		mNameEditor = (EditText) v.findViewById(R.id.name_editor);
		mNameEditor.requestFocus();
		
		getDialog().getWindow().setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return v;
	}
	
	@Override
	public void onResume() {
		mNameEditor.setText(mName);
		mDelButton.setVisibility((mId!=-1 && mId != 1) ? View.VISIBLE : View.GONE);
		super.onResume();
	};

	public void onClick(View v) {
		int id = v.getId(); 
		if (id == R.id.ok) {
			mListener.onFinishDialog(mNameEditor.getText().toString(), mId,mId != -1 ? Status.EDIT : Status.ADD, mIsRunning);
		} else if(id == R.id.del)
		{
			mListener.onFinishDialog(mNameEditor.getText().toString(), mId, Status.DEL, mIsRunning);
		}

		dismiss();
	}

	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}

	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
	}
}
