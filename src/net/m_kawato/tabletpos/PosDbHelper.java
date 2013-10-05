package net.m_kawato.tabletpos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PosDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "pos_db";
    private static final int DB_VERSION = 3;

    private static final String CREATE_TABLE_ORDERS = "CREATE TABLE orders ( "
    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
    + "transaction_id INTEGER, "
    + "loading_sheet_number TEXT, "
    + "route TEXT, "
    + "place TEXT, "
    + "product_id INTEGER, "
    + "quantity INTEGER, "
    + "credit INTEGER )";
 
    public PosDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ORDERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS orders");
        onCreate(db);
    }    }
