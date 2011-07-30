package com.pokebros.android.pokemononline;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

	private static final String DBNAME = "po_database";
	private final Context myContext;

	public DataBaseHelper(Context context) {
		super(context, DBNAME, null, 1);
		myContext = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			InputStream assetsDB;
			assetsDB = myContext.getAssets().open(DBNAME);

			OutputStream dbOut = new FileOutputStream("/data/data/com.pokebros.android.pokemononline/databases/" + DBNAME);

			byte[] buffer = new byte[1024];
			int length;
			while ((length = assetsDB.read(buffer))>0) {
				dbOut.write(buffer, 0, length);
			}

			dbOut.flush();
			dbOut.close();
			assetsDB.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

	public String query(String query) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor curs = db.rawQuery(query, null);
		curs.moveToFirst();
		String ret = curs.getString(0);
		curs.close();
		close();
		return ret;
	}

}