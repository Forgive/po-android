package com.pokebros.android.pokemononline;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
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
		InputStream assetsDB = myContext.getAssets().open("Move_Message");
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