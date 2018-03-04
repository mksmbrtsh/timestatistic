package maximsblog.blogspot.com.timestatistic;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.slots.Slot;
import ru.tinkoff.decoro.slots.SlotValidatorSet;
import ru.tinkoff.decoro.watchers.*;
import ru.tinkoff.decoro.slots.PredefinedSlots;
import ru.tinkoff.decoro.slots.SlotValidators;
/**
 * Created by mskmbrtsh on 08.12.2017.
 */

public class SplitView extends RelativeLayout implements SeekBar.OnSeekBarChangeListener {


    private TextView mStartTime;
    private TextView mEndTime;
    private EditText  mCurrentTime;
    private SeekBar mSeek;
    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(
            "dd.MM.yy HH:mm");
    private long mCurrentSplit;
    Timer t = new Timer();
    private long mStart;
    private Context mContext;
    private IdateChange mIdateChange;
    private int mId;
    private long mLenght;

    public SplitView(Context context) {
        this(context, null);
    }

    public SplitView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.split_view_attr);
    }

    public SplitView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflate(getContext(), R.layout.split_view, this);
        mStartTime = (TextView) findViewById(R.id.start_time);
        mEndTime = (TextView) findViewById(R.id.end_time);
        mCurrentTime = (EditText) findViewById(R.id.current_time);
        MaskImpl mask = MaskImpl.createTerminated(new Slot[]{
                new Slot(Slot.RULE_INPUT_REPLACE,null, SlotValidatorSet.setOf(new OnlyDigitValidator())),
                new Slot(Slot.RULE_INPUT_REPLACE ,null, SlotValidatorSet.setOf(new OnlyDigitValidator())),
                PredefinedSlots.hardcodedSlot('.').withTags(0),
                new Slot(Slot.RULE_INPUT_REPLACE ,null, SlotValidatorSet.setOf(new OnlyDigitValidator())),
                new Slot(Slot.RULE_INPUT_REPLACE ,null, SlotValidatorSet.setOf(new OnlyDigitValidator())),
                PredefinedSlots.hardcodedSlot('.').withTags(1),
                new Slot(Slot.RULE_INPUT_REPLACE ,null, SlotValidatorSet.setOf(new OnlyDigitValidator())),
                new Slot(Slot.RULE_INPUT_REPLACE ,null, SlotValidatorSet.setOf(new OnlyDigitValidator())),
                PredefinedSlots.hardcodedSlot(' ').withTags(2),
                new Slot(Slot.RULE_INPUT_REPLACE ,null, SlotValidatorSet.setOf(new OnlyDigitValidator())),
                new Slot(Slot.RULES_DEFAULT,null, SlotValidatorSet.setOf(new OnlyDigitValidator())),
                PredefinedSlots.hardcodedSlot(':').withTags(3),
                new Slot(Slot.RULE_INPUT_REPLACE  ,null, SlotValidatorSet.setOf(new OnlyDigitValidator())),
                new Slot(Slot.RULES_DEFAULT,null, SlotValidatorSet.setOf(new OnlyDigitValidator())),
        });
        mask.setShowingEmptySlots(true);

        FormatWatcher formatWatcher = new MaskFormatWatcher(mask);

        formatWatcher.installOn(mCurrentTime);
        mCurrentTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    Date c = mSimpleDateFormat.parse(mCurrentTime.getText().toString());
                    if(c.before(new Date(mStart))){
                        return;
                    }
                    if(mLenght!=0) {
                        if(c.after(new Date(mStart + mLenght))){
                            return;
                        }
                    } else {
                        if(c.after(new Date())) {
                            return;
                        }
                    }
                    mSeek.setProgress((int)(c.getTime() - mStart));
                }
                catch (ParseException e) {
                    mSeek.setOnSeekBarChangeListener(null);
                    mSeek.setProgress(mSeek.getMax());
                    mSeek.setOnSeekBarChangeListener(SplitView.this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mSeek = (SeekBar) findViewById(R.id.seek);
        mSeek.setOnSeekBarChangeListener(this);
        mContext = context;
    }

    public static class OnlyDigitValidator implements Slot.SlotValidator {

        @Override
        public boolean validate(final char value) {
            return "1234567890".contains(String.valueOf(value));
        }
    }
    public void setDateTimes(int id, final long start, final long lenght) {
        mId = id;
        mStart = start;
        mLenght = lenght;
        mCurrentSplit = 0;
        String startString = mSimpleDateFormat.format(new Date(start));
        mStartTime.setText(startString);
        if (lenght != 0) {
            String stopString = mSimpleDateFormat.format(new Date(start + lenght));
            mEndTime.setText(stopString);
            mSeek.setMax((int) lenght);
            mCurrentTime.setText(mSimpleDateFormat.format(new Date(lenght + mStart)));
        } else {
            mEndTime.setText(getContext().getString(R.string.now));
            mCurrentTime.setText(mSimpleDateFormat.format(new Date()));
            mSeek.setMax((int) (new Date().getTime() - start));
            t.scheduleAtFixedRate(new TimerTask() {
                                      public void run() {
                                          SplitView.this.post(new Runnable() {

                                              public void run() {
                                                  mSeek.setMax((int) (new Date().getTime() - start));
                                                  if (mCurrentSplit != 0) {
                                                      mCurrentTime.setText(mSimpleDateFormat.format(new Date(mCurrentSplit + mStart)));
                                                      mSeek.setProgress((int) (new Date(mCurrentSplit).getTime() - mStart));
                                                  }
                                              }

                                          });
                                      }
                                  }
                    ,
                    0,
                    60 * 1000);
        }
        mSeek.setProgress(mSeek.getMax());
    }

    public void setIdateChange(IdateChange idateChange){
        mIdateChange = idateChange;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser) {
            Date d = new Date((mCurrentSplit = progress) + mStart);
            mCurrentTime.setText(mSimpleDateFormat.format(d));
            mIdateChange.timeChange(d.getTime());
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
