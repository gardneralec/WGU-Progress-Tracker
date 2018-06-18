package com.alecgardner.wgu_progress_tracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBSQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "WGU_Progress_Tracker_DB";

    public DBSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DBContract.TermTable.CREATE_TABLE);
        sqLiteDatabase.execSQL(DBContract.CourseTable.CREATE_TABLE);
        sqLiteDatabase.execSQL(DBContract.TermTable.SAMPLE_DATA_1);
        sqLiteDatabase.execSQL(DBContract.TermTable.SAMPLE_DATA_2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DBContract.TermTable.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
