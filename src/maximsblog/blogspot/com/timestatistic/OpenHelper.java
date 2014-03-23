package maximsblog.blogspot.com.timestatistic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class OpenHelper extends SQLiteOpenHelper {

	final static String DB_NAME = "timestat.db";
	final static int DB_VER = 3;
	final static String TABLE_TIMERS = "timers";
	final static String TABLE_TIMES = "times";
	public final static String ID = "_id";
	public final static String ID2 = "_idt";
	public final static String NAME = "name";
	public final static String COLOR = "color";
	public final static String INTERVAL = "interval";
	public final static String ISRUNNING = "isrunning";
	public final static String TIMERSID = "timerid";
	public final static String STARTTIME = "start";
	public final static String LENGHT = "lenght";
	public final static String ENDTIME = "endtime";

	// public static String DB_FILEPATH =
	// "/data/data/maximsblog.blogspot.com.timestatistic/databases/database.db";

	final String CREATE_TABLE_TIMERS = "CREATE TABLE " + TABLE_TIMERS + "( "
			+ ID + " INTEGER PRIMARY KEY autoincrement, " + NAME + " TEXT, "
			+ COLOR + " INTEGER, " + ISRUNNING + " INTEGER DEFAULT 0, "
			+ INTERVAL + " INTEGER DEFAULT 900000 )";

	final String CREATE_TABLE_TIMES = "CREATE TABLE " + TABLE_TIMES + "( "
			+ ID2 + " INTEGER PRIMARY KEY autoincrement, " + TIMERSID
			+ " INTEGER, " + STARTTIME + " INTEGER, " + LENGHT
			+ " INTEGER DEFAULT 0, " + ENDTIME + " INTEGER )";

	final String DROP_TABLE_TIMERS = "DROP TABLE IF EXISTS " + TABLE_TIMERS;
	final String DROP_TABLE_TIMES = "DROP TABLE IF EXISTS " + TABLE_TIMES;

	public OpenHelper(Context context) {
		super(context, DB_NAME, null, DB_VER);
	}

	/**
	 * Copies the database file at the specified location over the current
	 * internal application database.
	 * 
	 * @param file
	 * */
	public boolean importDatabase(File file, String dbPath) throws IOException {

		// Close the SQLiteOpenHelper so it will commit the created empty
		// database to internal storage.
		close();
		String destPath = file.getPath();
		destPath = destPath.substring(0, destPath.lastIndexOf("/"))
				+ "/databases";
		String DB_FILEPATH = destPath + File.separator + OpenHelper.DB_NAME;
		File newDb = new File(dbPath);
		File oldDb = new File(DB_FILEPATH);
		if (!newDb.exists()) {
			newDb.createNewFile();
		}
		if (newDb.exists()) {
			copyFile(new FileInputStream(newDb), new FileOutputStream(oldDb));
			// Access the copied database so SQLiteHelper will cache it and mark
			// it as created.
			getWritableDatabase();
			return true;
		}
		return false;
	}

	/**
	 * Copies the database file at the specified location over the current
	 * internal application database.
	 * 
	 * @param file
	 * */
	public boolean exportDatabase(File file, String dbPath) throws IOException {

		// Close the SQLiteOpenHelper so it will commit the created empty
		// database to internal storage.
		close();
		String destPath = file.getPath();
		destPath = destPath.substring(0, destPath.lastIndexOf("/"))
				+ "/databases";
		String DB_FILEPATH = destPath + File.separator + OpenHelper.DB_NAME;
		File newDb = new File(DB_FILEPATH);
		File oldDb = new File(dbPath);
		if (newDb.exists()) {
			copyFile(new FileInputStream(newDb), new FileOutputStream(oldDb));
			// Access the copied database so SQLiteHelper will cache it and mark
			// it as created.
			getWritableDatabase();
			return true;
		}
		return false;
	}

	private void copyFile(FileInputStream fromFile, FileOutputStream toFile)
			throws IOException {
		FileChannel fromChannel = null;
		FileChannel toChannel = null;
		try {
			fromChannel = fromFile.getChannel();
			toChannel = toFile.getChannel();
			fromChannel.transferTo(0, fromChannel.size(), toChannel);
		} finally {
			try {
				if (fromChannel != null) {
					fromChannel.close();
				}
			} finally {
				if (toChannel != null) {
					toChannel.close();
				}
			}
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_TIMERS);
		db.execSQL(CREATE_TABLE_TIMES);
		ContentValues cv = new ContentValues();
		cv.put(NAME, "Idle");
		cv.put(ISRUNNING, 1);
		cv.put(COLOR, DiagramFragment.getRandomColor());
		long l = db.insert(TABLE_TIMERS, null, cv);
		Date now = new Date();
		cv = new ContentValues();
		cv.put(TIMERSID, l);
		cv.put(STARTTIME, now.getTime());
		cv.put(ENDTIME, 0);
		db.insert(TABLE_TIMES, null, cv);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion == 2) {
			db.execSQL("ALTER TABLE " + TABLE_TIMERS + " ADD COLUMN "
					+ INTERVAL + " INTEGER DEFAULT 900000");
		} else if (oldVersion == 2 && newVersion == 3) {
			db.execSQL("ALTER TABLE " + TABLE_TIMES + " ADD COLUMN " + ENDTIME
					+ " INTEGER");
			Cursor c = db.query(TABLE_TIMES, new String[] {
					RecordsDbHelper.ID2, RecordsDbHelper.STARTTIME,
					RecordsDbHelper.LENGHT }, null, null, null, null, null);
			if (c.moveToFirst()) {
				ContentValues values = new ContentValues();
				do {
					int id = c.getInt(0);
					long start = c.getLong(1);
					long l = c.getLong(2);
					long end = l != 0 ? start + l : 0;
					values.put(ENDTIME, end);
					db.update(TABLE_TIMES, values, ID2 + "=?", new String[]{ String.valueOf(id) });
				} while (c.moveToNext());
			}
		} else if (oldVersion == 1 && newVersion == 3) {
			db.execSQL("ALTER TABLE " + TABLE_TIMERS + " ADD COLUMN "
					+ INTERVAL + " INTEGER DEFAULT 900000");
			db.execSQL("ALTER TABLE " + TABLE_TIMES + " ADD COLUMN " + ENDTIME
					+ " INTEGER");
		} else {
			db.execSQL(DROP_TABLE_TIMERS);
			db.execSQL(DROP_TABLE_TIMES);
			onCreate(db);
		}

	}

}
