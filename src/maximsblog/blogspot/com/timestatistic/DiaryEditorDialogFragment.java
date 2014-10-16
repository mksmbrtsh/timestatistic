package maximsblog.blogspot.com.timestatistic;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.DataFormatException;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.Spinner;

public class DiaryEditorDialogFragment extends DialogFragment implements OnClickListener{
	private IRecordDialog mListener;

	private EditText mCurrentNoteEdit;
	private TextView mTime;
	// original values
	private int mOriginalPosition;
	private long mOriginalStart;
	private long mOriginalLenght;
	private String mName;
	private int mIDrecord;

	// edit values
	private String mCurrentNote;

	private Calendar mCalendar = Calendar.getInstance();
	private SimpleCursorAdapter mCurrentCounterAdapter;


	DateFormat mFormatterDateTime = DateFormat.getDateTimeInstance(
			DateFormat.SHORT, DateFormat.SHORT);

	public void setCounterDialogListener(IRecordDialog listener) {
		mListener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mOriginalPosition = savedInstanceState.getInt("mOriginalPosition");
			mOriginalStart = savedInstanceState.getLong("mOriginalStart");
			mOriginalLenght = savedInstanceState.getLong("mOriginalLenght");
			mName = savedInstanceState.getString("mName");
			mIDrecord = savedInstanceState.getInt("mIDrecord");
			mCurrentNote = savedInstanceState.getString("mCurrentNote");
		} 
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// original values
		outState.putInt("mOriginalPosition", mOriginalPosition);
		outState.putLong("mOriginalStart", mOriginalStart);
		outState.putLong("mOriginalLenght", mOriginalLenght);
		outState.putString("mName", mName);
		outState.putInt("mIDrecord", mIDrecord);
		// edit values
		outState.putString("mCurrentNote", mCurrentNote = mCurrentNoteEdit.getText().toString());
		super.onSaveInstanceState(outState);
	};

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.fragment_diary_editor_dialog, null);
		v.findViewById(R.id.ok).setOnClickListener(this);
		v.findViewById(R.id.cancel).setOnClickListener(this);
		mCurrentNoteEdit = (EditText)v.findViewById(R.id.current_note);
		mTime = (TextView)v.findViewById(R.id.diary_time);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		mCurrentNoteEdit.setText(mCurrentNote);
		mCalendar.setTimeInMillis(mOriginalStart);
		String startString = mFormatterDateTime.format(mCalendar.getTime());
		mCalendar.setTimeInMillis(mOriginalStart + mOriginalLenght);
		String stopString = mFormatterDateTime.format(mCalendar.getTime());
		getDialog().setTitle(mName);
		mTime.setText(startString + " - " + stopString);
	};


	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.ok) {
			editRecord();
			mListener.onDiaryFragmentsRefresh();
			dismiss();
		} else if (id == R.id.cancel) {
			dismiss();
		}
	}

	private void editRecord() {
		editNote(mIDrecord, mCurrentNoteEdit.getText().toString().trim());
	}

	private void editNote(int id, String note) {
		if(note.length() == 0){
			getActivity().getContentResolver().delete(RecordsDbHelper.CONTENT_URI_NOTES, RecordsDbHelper.ID3 + "=?", new String[]{String.valueOf(id)});
			return;
		}
		ContentValues cv = new ContentValues();
		cv.put(RecordsDbHelper.ID3, id);
		cv.put(RecordsDbHelper.NOTE, note);
		getActivity().getContentResolver().insert(
				RecordsDbHelper.CONTENT_URI_NOTES, cv);
	}

	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}

	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
	}

	public void setValues(String name, int idRecord, long start, long lenght, String note) {
		mName = name;
		mIDrecord = idRecord;
		mOriginalStart = start;
		mOriginalLenght = lenght;
		mCurrentNote = note;
	}

}