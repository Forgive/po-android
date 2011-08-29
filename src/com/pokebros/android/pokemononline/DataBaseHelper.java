package com.pokebros.android.pokemononline;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 4; // Increment when database should be updated on users' devices
	private static final String DBNAME = "po_database";
	private static final String DBPATH = "/data/data/com.pokebros.android.pokemononline/databases/";
	private final Context myContext;

	public DataBaseHelper(Context context) {
		super(context, DBNAME, null, DATABASE_VERSION);
		myContext = context;
		if (!currentVersionExists())
			create();
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {}
	
    private boolean currentVersionExists() {
    	SQLiteDatabase checkDB = null;
    	try{
    		checkDB = SQLiteDatabase.openDatabase(DBPATH + DBNAME, null, SQLiteDatabase.OPEN_READONLY);
    	} catch(SQLiteException e) {
    		// database does't exist yet.
    	}
    	if(checkDB != null && checkDB.getVersion() == DATABASE_VERSION) {
    		checkDB.close();
    		return true;
    	}
    	return false;
    }
	
	public void create() {
		try {
			InputStream assetsDB;
			assetsDB = myContext.getAssets().open(DBNAME);
			SQLiteDatabase db = getWritableDatabase(); // Create file to overwrite
			OutputStream dbOut = new FileOutputStream(DBPATH + DBNAME);

			byte[] buffer = new byte[1024];
			int length;
			while ((length = assetsDB.read(buffer))>0) {
				dbOut.write(buffer, 0, length);
			}

			dbOut.flush();
			dbOut.close();
			db.setVersion(DATABASE_VERSION);
			assetsDB.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

	public String query(String query) {
		synchronized(this) {
			SQLiteDatabase db = getReadableDatabase();
			Cursor curs = db.rawQuery(query, null);
			curs.moveToFirst();
			String ret = curs.getString(0);
			curs.close();
			close();
			return ret;
		}
	}

}