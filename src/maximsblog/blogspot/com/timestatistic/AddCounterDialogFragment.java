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

public class AddCounterDialogFragment extends DialogFragment implements
		OnClickListener {
	private EditText mNameEditor;
	private String mName;
	private AddCounterDialog mListener;
	private int mId;
	
	public interface AddCounterDialog {
		void onFinishAddDialog(String inputText, int id);
		void onFinishDelDialog(int id);
		
	}

	public void setCounterDialogListener(AddCounterDialog listener) {
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

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().setTitle(R.string.add_counter_dialog);
		View v = inflater.inflate(R.layout.add_counter_dialog_fragment, null);
		v.findViewById(R.id.ok).setOnClickListener(this);
		v.findViewById(R.id.cancel).setOnClickListener(this);
		mNameEditor = (EditText) v.findViewById(R.id.name_editor);
		mNameEditor.requestFocus();
		mNameEditor.setText(mName);
		getDialog().getWindow().setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return v;
	}

	public void onClick(View v) {
		if (v.getId() == R.id.ok) {
			mListener.onFinishAddDialog(mNameEditor.getText().toString(), mId);
		} else
			mListener.onFinishAddDialog(null, -1);

		dismiss();
	}

	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}

	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
	}
}
