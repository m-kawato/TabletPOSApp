package net.m_kawato.tabletpos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PosDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "pos_db";
    private static final int DB_VERSION = 5;

    private static final String CREATE_TABLE_LOADING =
            "CREATE TABLE loading ( "
            + "product_id INTEGER, "
            + "loaded INTEGER, "
            + "stock INTEGER )";

    public PosDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LOADING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS loading");
        onCreate(db);
    }
}
