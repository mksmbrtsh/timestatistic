package maximsblog.blogspot.com.timestatistic;

import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class RecordsDbHelper {

	final static int DB_VER = 1;
    final static String DB_NAME = "timestat.db";
    final static String TABLE_TIMERS = "timers";
    final static String TABLE_TIMES = "times";
    
    public final static String ID = "_id";
    public final static String NAME = "name";
    public final static String STARTTIME = "name";
    public final static String STOPTIME = "name";

    final String CREATE_TABLE_TIMERS = "CREATE TABLE "+TABLE_TIMERS +
                                "( "+ ID +" INTEGER PRIMARY KEY autoincrement, " +
                                NAME + " TEXT)";
    final String CREATE_TABLE_TIMES = "CREATE TABLE "+ TABLE_TIMES +
            "( "+ ID +" INTEGER PRIMARY KEY, " +
            STARTTIME + " INTEGER, " + STOPTIME  + "INTEGER )";
    
    
    final String DROP_TABLE_TIMERS = "DROP TABLE IF EXISTS " + TABLE_TIMERS;
    final String DROP_TABLE_TIMES = "DROP TABLE IF EXISTS " + TABLE_TIMES;
    
    public static final String INSERT_TIMER = "insert into "
    	+ TABLE_TIMERS + "( " +
    	NAME+ " ) values (?)";
    public static final String INSERT_TIMES = "insert into "
        	+ TABLE_TIMES + "( " +
        	STARTTIME +", " + STOPTIME + " ) values (?, ?)";
    
    Context mContext;
    private DatabaseHelper mDbHelper;
	private boolean isReady;
	
	private SQLiteDatabase mDb;

	public RecordsDbHelper(Context ctx) {
		this.mContext = ctx;
	}

	public RecordsDbHelper open() throws SQLException {
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		isReady = true;
		return this;
	}

	public void close() {
		isReady = false;
		mDbHelper.close();
	}
	public boolean IsReady(){
		return isReady;
	}
	public void delete(){
		mDb.delete(TABLE_TIMERS, null, null);
		mDb.delete(TABLE_TIMES, null, null);
	}
	
	public long createTimerRecord(String name) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(NAME, name);
		return mDb.insert(TABLE_TIMERS, null, initialValues);
	}
	
	public long createTimeRecord(int id, int start) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(ID, id);
		initialValues.put(STARTTIME, start);
		return mDb.insert(TABLE_TIMES, null, initialValues);
	}
	public long addEndTimeRecord(int id, int end) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(STOPTIME, end);
		return mDb.update(TABLE_TIMES, initialValues, ID + "=?",new String[]{ String.valueOf(id) });
	}
	
	synchronized public Cursor getTimersCursor() {
		Cursor ccc = mDb.query(TABLE_TIMERS, new String[]{ID, NAME}, null, null, null, null, NAME);
		return ccc;
	}
	

	public long insert(String name, long startdate) {
		ContentValues values = new ContentValues();
		values.put(RecordsDbHelper.NAME, name);
		return mDb.insert(RecordsDbHelper.TABLE_TIMERS, null, values);
		
	}
	//---deletes a particular title---
	public boolean deleteTimer(int rowId)
	{
		mDb.delete(TABLE_TIMES, ID +
				"=?", new String[]{String.valueOf(rowId)});
		
	return mDb.delete(TABLE_TIMERS, ID +
	"=?", new String[]{String.valueOf(rowId)}) > 0;
	}
	

    
private class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }
    @Override
        public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TIMERS);
        db.execSQL(CREATE_TABLE_TIMES);
        }
    
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	       db.execSQL(DROP_TABLE_TIMERS);
    	       db.execSQL(DROP_TABLE_TIMES);
    	       onCreate(db);
        }

}

}