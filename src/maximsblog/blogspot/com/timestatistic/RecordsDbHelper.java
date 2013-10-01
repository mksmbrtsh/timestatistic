package maximsblog.blogspot.com.timestatistic;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.preference.PreferenceManager;
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
	public static final int RESETCOUNTERS = 6;
	public static final int RENAMECOUNTER = 7;
	
	
	final static String DB_NAME = "timestat.db";
	final static int DB_VER = 1;
    final static String TABLE_TIMERS = "timers";
    final static String TABLE_TIMES = "times";
    private static HashMap<String, String> timersProjectionMap;
    private static HashMap<String, String> timesProjectionMap;
    public final static String ID = "_id";
    public final static String ID2 = "_idt";
    public final static String NAME = "name";
    public final static String ISRUNNING="isrunning";
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
		sUriMatcher.addURI(AUTHORITY, "resetcounters", RESETCOUNTERS);
		sUriMatcher.addURI(AUTHORITY, "renamecounter", RENAMECOUNTER);
		
		timersProjectionMap = new HashMap<String, String>();
		timersProjectionMap.put(ID, ID);
		timersProjectionMap.put(NAME, NAME);
		timersProjectionMap.put(ISRUNNING, ISRUNNING);
		
		timesProjectionMap = new HashMap<String, String>();
		timesProjectionMap.put(ID2, ID2);
		timesProjectionMap.put(TIMERSID, TIMERSID);
		
		timesProjectionMap.put(STARTTIME, STARTTIME);
		timesProjectionMap.put(LENGHT, LENGHT);
	}
    
    public static final Uri CONTENT_URI_TIMERS = Uri.parse("content://" + AUTHORITY + "/timers");
    public static final Uri CONTENT_URI_TIMES = Uri.parse("content://" + AUTHORITY + "/times");
    public static final Uri CONTENT_URI_SUMTIMES = Uri.parse("content://" + AUTHORITY + "/sumtimes");
    public static final Uri CONTENT_URI_RESETCOUNTERS = Uri.parse("content://" + AUTHORITY + "/resetcounters");
    public static final Uri CONTENT_URI_RENAMECOUNTER= Uri.parse("content://" + AUTHORITY + "/renamecounter");
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
		case RENAMECOUNTER:
			table = TABLE_TIMERS;
			break;
		case TIMERS:
			table = TABLE_TIMERS;
			ContentValues cv = new ContentValues();
			cv.put(RecordsDbHelper.ISRUNNING, 0);
			mDB.update(table, cv, RecordsDbHelper.ISRUNNING +"=?", new String[] { String.valueOf(1) } );
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
		            	String e = qb.buildQueryString(false,
		            			TABLE_TIMERS + " LEFT OUTER JOIN " + TABLE_TIMES + " ON " + ID +  " = " + TIMERSID,
		            			new String[] { RecordsDbHelper.ID2 + " AS " + RecordsDbHelper.ID,
		            			RecordsDbHelper.TIMERSID, 
		            			"SUM("+ RecordsDbHelper.LENGHT + ") AS " + RecordsDbHelper.LENGHT,
		            			"MAX(" + RecordsDbHelper.STARTTIME +") AS" + RecordsDbHelper.STARTTIME,
		            			RecordsDbHelper.ID,
		            			RecordsDbHelper.NAME,
		            			RecordsDbHelper.ISRUNNING  },
		            			selection,
		            			RecordsDbHelper.TIMERSID,
		            			null,
		            			null,
		            			null);
		            	c = mDB.rawQuery(e, selectionArgs);
		            	c.setNotificationUri(getContext().getContentResolver(), uri);
		            	return c;
		            case TIMES_ID:
		            	qb.setTables(TABLE_TIMES);
		            	qb.setProjectionMap(timesProjectionMap);
		                selection = selection + ID2 + "=" + uri.getLastPathSegment();
		                break;
		            case SUMTIMES:
		            	String s = qb.buildQueryString(false,
		            			TABLE_TIMERS + " LEFT OUTER JOIN " + TABLE_TIMES + " ON " + ID +  " = " +TIMERSID ,
		            			new String[] { RecordsDbHelper.TIMERSID,
		            			"SUM("+ RecordsDbHelper.LENGHT + ") AS " + RecordsDbHelper.LENGHT,
		            			"MAX(" + RecordsDbHelper.STARTTIME +") AS" + RecordsDbHelper.STARTTIME,
		            			RecordsDbHelper.NAME,
		            			RecordsDbHelper.ISRUNNING },
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
		int count;
		switch (sUriMatcher.match(uri)) {
		case TIMERS:
			table = TABLE_TIMERS;
			where = ID + "=?";
			count = mDB.delete(table, where, whereArgs);
			where = TIMERSID + "=?";
			table = TABLE_TIMES;
			mDB.delete(table, where, whereArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			return count;
		case TIMES:
			table = TABLE_TIMES;
			break;
		case TIMES_ID:
			table = TABLE_TIMES;
			where = where + ID2 + "=" + uri.getLastPathSegment();
			break;
		case RESETCOUNTERS:
			table = TABLE_TIMES;
			count = mDB.delete(table, where, whereArgs);
			Cursor c = mDB.query(TABLE_TIMERS, new String[] { RecordsDbHelper.ID }, null, null, null, null, null);
			ContentValues cv = new ContentValues();
			while(c.moveToNext())
			{
				int id = c.getInt(0);
				cv.clear();
				cv.put(RecordsDbHelper.TIMERSID, id);
				if(id == 1)
					cv.put(STARTTIME, (new Date()).getTime());
				mDB.insert(TABLE_TIMES, null, cv);
			}
			cv.clear();
			cv.put(RecordsDbHelper.ISRUNNING, 0);
			mDB.update(TABLE_TIMERS, cv, RecordsDbHelper.ISRUNNING +"=?", new String[] { String.valueOf(1) } );
			cv.clear();
			cv.put(RecordsDbHelper.ISRUNNING, 1);
			mDB.update(TABLE_TIMERS, cv, RecordsDbHelper.ID +"=?", new String[] { String.valueOf(1) } );
			getContext().getContentResolver().notifyChange(uri, null);
			return count;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		count = mDB.delete(table, where, whereArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

    

    
	private class OpenHelper extends SQLiteOpenHelper {

		
		final String CREATE_TABLE_TIMERS = "CREATE TABLE "+TABLE_TIMERS +
                "( "+ ID +" INTEGER PRIMARY KEY autoincrement, " +
                NAME + " TEXT, " + ISRUNNING + " INTEGER DEFAULT 0 )";
		
		final String CREATE_TABLE_TIMES = "CREATE TABLE "+ TABLE_TIMES +
				"( "+ ID2 +" INTEGER PRIMARY KEY autoincrement, " + TIMERSID + " INTEGER, " +
				STARTTIME + " INTEGER, " + LENGHT  + " INTEGER DEFAULT 0 )";
		
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
        cv.put(ISRUNNING, 1);
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