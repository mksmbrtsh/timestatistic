package maximsblog.blogspot.com.timestatistic;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import maximsblog.blogspot.com.timestatistic.ColorPickerDialogFragment.ColorCounterDialog;

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
	private int mSortId;
	private int mOriginSortId;
	private EditText mIntervalHoursEditor;
	private EditText mIntervalMinutesEditor;
	private EditText mSortidEditor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mIsRunning = savedInstanceState.getBoolean("mIsRunning");
			mId = savedInstanceState.getInt("mId");
			mColor = savedInstanceState.getInt("mColor");
			mName = savedInstanceState.getString("mName");
			mInterval = savedInstanceState.getLong("mInterval");
			mSortId = savedInstanceState.getInt("mSortId");
			mOriginSortId = savedInstanceState.getInt("mOriginSortId");
		}
		ColorPickerDialogFragment mColorPickerDialogFragment = (ColorPickerDialogFragment) getActivity()
				.getFragmentManager().findFragmentByTag(
						"mColorPickerDialogFragment");
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
		outState.putLong("mInterval", getInterval());
		outState.putInt("mSortId", mSortidEditor.getText().toString().length() == 0? 1: Integer.valueOf(mSortidEditor.getText().toString()));
		outState.putInt("mOriginSortId", mOriginSortId);
		super.onSaveInstanceState(outState);
	}

	private long getInterval() {
		return Integer.valueOf(mIntervalHoursEditor.getText().toString()) * 60 * 60 * 1000
				+ Integer.valueOf(mIntervalMinutesEditor.getText().toString()) * 60 * 1000;
	}

	public enum Status {
		ADD, EDIT, DEL
	}

	public interface ICounterEditorDialog {
		void onFinishDialog(String inputText, int id, Status status,
				boolean isRunning, int color, long interval, int sortid);
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
	
	public void setSortId(int sortid) {
		mSortId = sortid;
		mOriginSortId = sortid;
	}

	public void setIdCounter(int id) {
		mId = id;
	}

	public void setIsRunning(boolean isRunning) {
		mIsRunning = isRunning;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().setTitle(R.string.add_counter_dialog);
		View v = inflater
				.inflate(R.layout.fragment_counter_editor_dialog, null);
		v.findViewById(R.id.ok).setOnClickListener(this);
		v.findViewById(R.id.cancel).setOnClickListener(this);
		mDelButton = (Button) v.findViewById(R.id.del);
		mDelButton.setOnClickListener(this);
		mColorButton = (ImageButton) v.findViewById(R.id.color_imageButton);
		mColorButton.setOnClickListener(this);
		mNameEditor = (EditText) v.findViewById(R.id.name_editor);
		mSortidEditor = (EditText) v.findViewById(R.id.sortid);
		mSortidEditor.setFilters(new InputFilter[] { new PartialRegexInputFilter(mSortidEditor,
				"\\d{1,}") });
		mSortidEditor.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String h = mSortidEditor.getText().toString();
				if (h.length() == 0) {
					mSortidEditor.removeTextChangedListener(this);
					mSortidEditor.setText(String.valueOf(mOriginSortId));
					mSortidEditor.addTextChangedListener(this);
				} else if(Integer.valueOf(h) == 0) {
					mSortidEditor.removeTextChangedListener(this);
					mSortidEditor.setText(String.valueOf(mOriginSortId));
					mSortidEditor.addTextChangedListener(this);
				}
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Handler  mHandler = new Handler();;
		mHandler.post(new Runnable() {
		    public void run() {
		    	mNameEditor.clearFocus();
		    	mNameEditor.requestFocus();		    	
		        
		    }
		});
		mIntervalHoursEditor = (EditText) v
				.findViewById(R.id.interval_hours_editor);
		mIntervalHoursEditor
		.setFilters(new InputFilter[] { new PartialRegexInputFilter(mIntervalHoursEditor,
				"[0-9][0-9][0-9][0-9][0-9]") });
		mIntervalHoursEditor.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String h = mIntervalHoursEditor.getText().toString();
				if (h.length() == 0) {
					mIntervalHoursEditor.setText("0");
					mIntervalHoursEditor.setSelection(1);
					String m = mIntervalMinutesEditor.getText().toString();
					if (m.equals("0")) {
						mIntervalMinutesEditor.setText("1");
						mIntervalMinutesEditor.setSelection(1);
					}
					if (m.equals("00")) {
						mIntervalMinutesEditor.setText("01");
						mIntervalMinutesEditor.setSelection(2);
					}
				}
				if(h.length()>1 && Integer.valueOf(h)==0){
					String m = mIntervalMinutesEditor.getText().toString();
					if (m.equals("0")) {
						mIntervalMinutesEditor.setText("1");
						mIntervalMinutesEditor.setSelection(1);
					}
					if (m.equals("00")) {
						mIntervalMinutesEditor.setText("01");
						mIntervalMinutesEditor.setSelection(2);
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		mIntervalMinutesEditor = (EditText) v
				.findViewById(R.id.interval_minutes_editor);
		mIntervalMinutesEditor
				.setFilters(new InputFilter[] { new PartialRegexInputFilter(mIntervalMinutesEditor, "[0-5][0-9]|[0-9]") });
		mIntervalMinutesEditor.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				
				String m = mIntervalMinutesEditor.getText().toString();
				if (m.length() == 0) {
					mIntervalMinutesEditor.setText("0");
					mIntervalMinutesEditor.setSelection(1);
					String h = mIntervalHoursEditor.getText().toString();
					if (h.equals("0") || h.equals("00") || h.equals("000") || h.equals("0000") || h.equals("00000")) {
						if(mIntervalMinutesEditor.getText().toString().length() == 2) {
							mIntervalMinutesEditor.setText("01");
							mIntervalMinutesEditor.setSelection(2);
						}
						else {
						mIntervalMinutesEditor.setText("1");
						}
					}
				}
				if(m.length()==1 && Integer.valueOf(m)==0){
					String h = mIntervalHoursEditor.getText().toString();
					if (h.equals("0") || h.equals("00") || h.equals("000") || h.equals("0000") || h.equals("00000")) {
						if(mIntervalMinutesEditor.getText().toString().length() == 2) {
							mIntervalMinutesEditor.setText("01");
							mIntervalMinutesEditor.setSelection(2);
						}
						else {
						mIntervalMinutesEditor.setText("1");
						}
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		getDialog().getWindow().setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return v;
	}

	class PartialRegexInputFilter implements InputFilter {

		private Pattern mPattern;
		private EditText mEditor;

		public PartialRegexInputFilter(EditText editor, String pattern) {
			mPattern = Pattern.compile(pattern);
			mEditor = editor;
		}

		@Override
		public CharSequence filter(CharSequence source, int sourceStart,
				int sourceEnd, Spanned destination, int destinationStart,
				int destinationEnd) {
			String textToCheck = destination.subSequence(0, destinationStart)
					.toString()
					+ source.subSequence(sourceStart, sourceEnd)
					+ destination.subSequence(destinationEnd,
							destination.length()).toString();

			Matcher matcher = mPattern.matcher(textToCheck);

			// Entered text does not match the pattern
			if (!matcher.matches()) {

				// It does not match partially too
				if (!matcher.hitEnd()) {
					mEditor.selectAll();
					return "";
				}

			}

			return null;
		}

	}

	@Override
	public void onResume() {
		mNameEditor.setText(mName);
		mSortidEditor.setText(String.valueOf(mSortId));
		long minutes = mInterval / 60 / 1000;
		long hours = 0;
		while (minutes >= 60) {
			hours++;
			minutes -= 60;
		}
		mIntervalHoursEditor.setText(String.valueOf(hours));
		mIntervalMinutesEditor.setText(String.valueOf(minutes));
		if (mId != -1) {
			getDialog().setTitle(R.string.edit_counter_dialog);
		} else {
			getDialog().setTitle(R.string.add_counter_dialog);
		}
		if(mId != 1){
			mSortidEditor.setEnabled(true);
			mDelButton.setVisibility(View.VISIBLE);
		} else {
			mSortidEditor.setEnabled(false);
			mDelButton.setVisibility(View.INVISIBLE);
		}
		super.onResume();
	};

	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.ok) {
			mListener.onFinishDialog(mNameEditor.getText().toString(), mId,
					mId != -1 ? Status.EDIT : Status.ADD, mIsRunning, mColor,
							getInterval(), mSortidEditor.getText().toString().length() == 0 ? 1: Integer.valueOf(mSortidEditor.getText().toString()));
		} else if (id == R.id.del) {
			mListener.onFinishDialog(mNameEditor.getText().toString(), mId,
					Status.DEL, mIsRunning, mColor, getInterval(), mSortidEditor.getText().toString().length() == 1? 0: Integer.valueOf(mSortidEditor.getText().toString()));
		} else if (id == R.id.color_imageButton) {
			ColorPickerDialogFragment mColorPickerDialogFragment = new ColorPickerDialogFragment();
			mColorPickerDialogFragment.setColorCounterDialogListener(this);
			mColorPickerDialogFragment.setColor(mColor);
			mColorPickerDialogFragment.show(getActivity()
					.getFragmentManager(), "mColorPickerDialogFragment");
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
