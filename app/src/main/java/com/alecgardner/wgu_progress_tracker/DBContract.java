package com.alecgardner.wgu_progress_tracker;

import android.provider.BaseColumns;

public class DBContract {
    private DBContract() {
        // Prevent accidental constructor call | static methods and variables used
    }

    public static class TermTable implements BaseColumns {
        public static final String TABLE_NAME = "Term";
        public static final String COLUMN_NUMBER = "Term_Number";
        public static final String COLUMN_START = "Start";
        public static final String COLUMN_END = "End";
        public static final String COLUMN_STATUS = "Status";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NUMBER + " INTEGER, " +
                COLUMN_START + " INTEGER, " +
                COLUMN_END + " INTEGER" +
                COLUMN_STATUS + "TEXT" + ")";

        public static final String SAMPLE_DATA = "INSERT INTO Term (Term_Number, Start, End, Status) " +
                                                 "VALUES(1, 1514764800, 1517443200, 'IP');" +
                                                 "INSERT INTO Term (Term_Number, Start, End, Status) " +
                                                 "VALUES(2, 1517443200, 1519862400, 'IP');";
    }

    public static class CourseTable implements BaseColumns {
        public static final String TABLE_NAME = "Course";
        public static final String COLUMN_TITLE = "Title";
        public static final String COLUMN_START = "Start";
        public static final String COLUMN_END = "End";
        public static final String COLUMN_TERM = "TermID";
        public static final String COLUMN_STATUS = "Status";
        public static final String COLUMN_NOTES = "Notes";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_START + " INTEGER, " +
                COLUMN_END + " INTEGER, " +
                COLUMN_TERM + " INTEGER, " +
                COLUMN_STATUS + "TEXT, " +
                COLUMN_NOTES + "TEXT" + ")";
    }


}
