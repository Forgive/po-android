package com.pokebros.android.pokemononline;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

	private static final String DBNAME = "Move_Message";
	private DataBaseHelper myDBHelper;
	private SQLiteDatabase myDB;

	private final Context myContext;

	public DataBaseHelper(Context context) {
		super(context, DBNAME, null, 2);
		this.myContext = context;
	}
	
	public void createDatabase() throws IOException {
		if (databaseExists())
			return;
		
		InputStream assetsDB = myContext.getAssets().open("Move_Message");
		
		// This makes an empty file, otherwise it won't be able to write
		this.getReadableDatabase();
		
		OutputStream dbOut = new FileOutputStream("/data/data/com.pokebros.android.pokemononline/databases/Move_Message");
		
		byte[] buffer = new byte[1024];
		int length;
		while ((length = assetsDB.read(buffer))>0) {
			dbOut.write(buffer, 0, length);
		}
		
		dbOut.flush();
		dbOut.close();
		assetsDB.close();
	}
	
	private boolean databaseExists() {
		SQLiteDatabase checkDB = null;

		try {
			checkDB = SQLiteDatabase.openDatabase("/data/data/com.pokebros.android.pokemononline/databases/Move_Message", null, SQLiteDatabase.OPEN_READONLY);
		} catch(SQLiteException e){
			//database does't exist yet.
		}

		if(checkDB != null){
			checkDB.close();
		}

		return checkDB != null;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public DataBaseHelper open() throws SQLException {
		myDBHelper = new DataBaseHelper(myContext);
		myDB = myDBHelper.getWritableDatabase();
		return this;
	}

}