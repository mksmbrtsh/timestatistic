package maximsblog.blogspot.com.timestatistic;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Log;

import java.net.IDN;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class RecordsDbHelper extends ContentProvider {

	public static final String AUTHORITY = "maximsblogspot.com.timestatistic.providers.db";
	private static final UriMatcher sUriMatcher;
	public static final int TIMERS = 1;
	public static final int TIMERS_ID = 2;
	public static final int TIMES = 3;
	public static final int TIMES_ID = 4;
	public static final int SUMTIMES = 5;
	
	
	final static String DB_NAME = "timestat.db";
	final static int DB_VER = 1;
    final static String TABLE_TIMERS = "timers";
    final static String TABLE_TIMES = "times";
    private static HashMap<String, String> timersProjectionMap;
    private static HashMap<String, String> timesProjectionMap;
    public final static String ID = "_id";
    public final static String NAME = "name";
    public final static String TIMERSID = "timerid";
    public final static String STARTTIME = "start";
    public final static String LENGHT = "lenght";

    static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, TABLE_TIMERS, TIMERS);
		sUriMatcher.addURI(AUTHORITY, TABLE_TIMERS + "/#", TIMERS_ID);
		sUriMatcher.addURI(AUTHORITY, TABLE_TIMES, TIMES);
		sUriMatcher.addURI(AUTHORITY, TABLE_TIMES + "/#", TIMES_ID);
		sUriMatcher.addURI(AUTHORITY, "sumtimes", SUMTIMES);
		timersProjectionMap = new HashMap<String, String>();
		timersProjectionMap.put(ID, ID);
		timersProjectionMap.put(NAME, NAME);
		
		timesProjectionMap = new HashMap<String, String>();
		timesProjectionMap.put(ID, ID);
		timesProjectionMap.put(TIMERSID, TIMERSID);
		
		timesProjectionMap.put(STARTTIME, STARTTIME);
		timesProjectionMap.put(LENGHT, LENGHT);
	}
    
    public static final Uri CONTENT_URI_TIMERS = Uri.parse("content://" + AUTHORITY + "/timers");
    public static final Uri CONTENT_URI_TIMES = Uri.parse("content://" + AUTHORITY + "/times");
    public static final Uri CONTENT_URI_SUMTIMES = Uri.parse("content://" + AUTHORITY + "/sumtimes");
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jwei512.notes";
  
    SQLiteDatabase mDB;
    
    @Override
    public boolean onCreate() {
            Context context = getContext();
            OpenHelper openHelper = new OpenHelper(context);
            mDB = openHelper.getWritableDatabase();
            return (mDB == null) ? false : true;
    }

    @Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case TIMERS:
			return "vnd.android.cursor.dir/vnd.jwei512.timers";
		case TIMERS_ID:
			return "vnd.android.cursor.dir/vnd.jwei512.timers";
		case TIMES:
			return "vnd.android.cursor.dir/vnd.jwei512.times";
		case TIMES_ID:
			return "vnd.android.cursor.dir/vnd.jwei512.times";
		case SUMTIMES:
			return "vnd.android.cursor.dir/vnd.jwei512.times";
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}
    
    @Override
	public Uri insert(Uri uri, ContentValues initialValues) {
    	String table;
    	Uri content;
		switch (sUriMatcher.match(uri)) {
		case TIMERS:
			table = TABLE_TIMERS;
			content = CONTENT_URI_TIMERS;
			break;
		case TIMES:
			table = TABLE_TIMES;
			content = CONTENT_URI_TIMES;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		long rowId = mDB.insert(table, ID, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(content, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}
    
    @Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		int count;
		String table;
		switch (sUriMatcher.match(uri)) {
		case TIMERS:
			table = TABLE_TIMERS;
			break;
		case TIMES:
			table = TABLE_TIMES;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		count = mDB.update(table, values, where, whereArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
    
    @Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		Cursor c;
		        
		        switch (sUriMatcher.match(uri)) {   
		            case TIMERS:
		            	qb.setTables(TABLE_TIMERS);
		            	qb.setProjectionMap(timersProjectionMap);
		                break;
		            case TIMERS_ID:
		            	qb.setTables(TABLE_TIMERS);
		            	qb.setProjectionMap(timersProjectionMap);
		                selection = selection + ID + "=" + uri.getLastPathSegment();
		                break;
		            case TIMES:
		            	qb.setTables(TABLE_TIMES);
		            	qb.setProjectionMap(timesProjectionMap);
		                break;
		            case TIMES_ID:
		            	qb.setTables(TABLE_TIMES);
		            	qb.setProjectionMap(timesProjectionMap);
		                selection = selection + ID + "=" + uri.getLastPathSegment();
		                break;
		            case SUMTIMES:
		            	String s = qb.buildQueryString(false,
		            			TABLE_TIMES,
		            			new String[] { RecordsDbHelper.TIMERSID, "SUM("+ RecordsDbHelper.LENGHT + ") AS " + RecordsDbHelper.LENGHT  },
		            			selection,
		            			RecordsDbHelper.TIMERSID,
		            			null,
		            			null,
		            			null);
		            	c = mDB.rawQuery(s, selectionArgs);
		            	c.setNotificationUri(getContext().getContentResolver(), uri);
		            	return c;
		            default:
		                throw new IllegalArgumentException("Unknown URI " + uri);
		        }
		        c = qb.query(mDB, projection, selection, selectionArgs, null, null, sortOrder);
		        c.setNotificationUri(getContext().getContentResolver(), uri);
		        return c;
	}
    
   
   
	
    @Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		String table;
		switch (sUriMatcher.match(uri)) {
		case TIMERS:
			table = TABLE_TIMERS;
			break;
		case TIMERS_ID:
			table = TABLE_TIMERS;
			where = where + ID + "=" + uri.getLastPathSegment();
			break;
		case TIMES:
			table = TABLE_TIMES;
			break;
		case TIMES_ID:
			table = TABLE_TIMES;
			where = where + ID + "=" + uri.getLastPathSegment();
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		int count = mDB.delete(table, where, whereArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

    

    
	private class OpenHelper extends SQLiteOpenHelper {

		
		final String CREATE_TABLE_TIMERS = "CREATE TABLE "+TABLE_TIMERS +
                "( "+ ID +" INTEGER PRIMARY KEY autoincrement, " +
                NAME + " TEXT)";
		
		final String CREATE_TABLE_TIMES = "CREATE TABLE "+ TABLE_TIMES +
				"( "+ ID +" INTEGER PRIMARY KEY autoincrement, " + TIMERSID + " INTEGER, " +
				STARTTIME + " INTEGER, " + LENGHT  + " INTEGER )";
		
		final String DROP_TABLE_TIMERS = "DROP TABLE IF EXISTS " + TABLE_TIMERS;
	    final String DROP_TABLE_TIMES = "DROP TABLE IF EXISTS " + TABLE_TIMES;
	    
    public OpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }
    
    @Override
        public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TIMERS);
        db.execSQL(CREATE_TABLE_TIMES);
        ContentValues cv = new ContentValues();
        cv.put(NAME, "Idle");
        long l = db.insert(TABLE_TIMERS, null, cv);
        Date now = new Date();
        cv = new ContentValues();
        cv.put(TIMERSID, l);
        cv.put(STARTTIME, now.getTime());
        db.insert(TABLE_TIMES, null, cv);
    }
    
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	       db.execSQL(DROP_TABLE_TIMERS);
    	       db.execSQL(DROP_TABLE_TIMES);
    	       onCreate(db);
        }

	}
}