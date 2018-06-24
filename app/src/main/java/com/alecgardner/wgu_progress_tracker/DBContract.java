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
                COLUMN_END + " INTEGER, " +
                COLUMN_STATUS + " TEXT" + ")";

        public static final String SAMPLE_TERM_1 = "INSERT INTO Term (Term_Number, Start, End, Status) " +
                                                   "VALUES(1, 1517464800000, 1519884000000, 'IP')";

        public static final String SAMPLE_TERM_2 = "INSERT INTO Term (Term_Number, Start, End, Status) " +
                                                   "VALUES(2, 1519884000000, 1522558800000, 'IP')";

        public static final String SAMPLE_COURSE_1 = "INSERT INTO Course (" +
                                                    CourseTable.COLUMN_TITLE + ", " +
                                                    CourseTable.COLUMN_START + ", " +
                                                    CourseTable.COLUMN_END + ", " +
                                                    CourseTable.COLUMN_TERM + ", " +
                                                    CourseTable.COLUMN_STATUS + ", " +
                                                    CourseTable.COLUMN_NOTES +
                                                    ") VALUES('Course1', 1517464800000, 1518674400000, " +
                                                    "1, 'CO', 'Lots of Notes')";

        public static final String SAMPLE_COURSE_2 = "INSERT INTO Course (" +
                                                    CourseTable.COLUMN_TITLE + ", " +
                                                    CourseTable.COLUMN_START + ", " +
                                                    CourseTable.COLUMN_END + ", " +
                                                    CourseTable.COLUMN_TERM + ", " +
                                                    CourseTable.COLUMN_STATUS + ", " +
                                                    CourseTable.COLUMN_NOTES +
                                                    ") VALUES('Course2', 1519884000000, 1521090000000, " +
                                                    "2, 'IP', 'Lots of Notes')";

        public static final String SAMPLE_COURSE_3 = "INSERT INTO Course (" +
                                                    CourseTable.COLUMN_TITLE + ", " +
                                                    CourseTable.COLUMN_START + ", " +
                                                    CourseTable.COLUMN_END + ", " +
                                                    CourseTable.COLUMN_TERM + ", " +
                                                    CourseTable.COLUMN_STATUS + ", " +
                                                    CourseTable.COLUMN_NOTES +
                                                    ") VALUES('Splendid Tidbits', 1518674400000, 1519884000000, " +
                                                    "1, 'DR', 'Lots of Notes')";

        public static final String SAMPLE_COURSE_4 = "INSERT INTO Course (" +
                                                    CourseTable.COLUMN_TITLE + ", " +
                                                    CourseTable.COLUMN_START + ", " +
                                                    CourseTable.COLUMN_END + ", " +
                                                    CourseTable.COLUMN_TERM + ", " +
                                                    CourseTable.COLUMN_STATUS + ", " +
                                                    CourseTable.COLUMN_NOTES +
                                                    ") VALUES('LoveLess', 1521090000000, 1522558800000, " +
                                                    "2, 'PT', 'Lots of Notes')";

        public static final String SAMPLE_ASSESSMENT_1 = "INSERT INTO Assessment (" +
                                                        AssessmentTable.COLUMN_TITLE + ", " +
                                                        AssessmentTable.COLUMN_TYPE + ", " +
                                                        AssessmentTable.COLUMN_DUE + ", " +
                                                        AssessmentTable.COLUMN_STATUS + ", " +
                                                        AssessmentTable.COLUMN_COURSE +
                                                        ") VALUES('Perf1', 'P', 1518674400000, " +
                                                        "'CO', 1)";

        public static final String SAMPLE_ASSESSMENT_2 = "INSERT INTO Assessment (" +
                                                        AssessmentTable.COLUMN_TITLE + ", " +
                                                        AssessmentTable.COLUMN_TYPE + ", " +
                                                        AssessmentTable.COLUMN_DUE + ", " +
                                                        AssessmentTable.COLUMN_STATUS + ", " +
                                                        AssessmentTable.COLUMN_COURSE +
                                                        ") VALUES('Obj1', 'O', 1519884000000, " +
                                                        "'PT', 2)";

        public static final String SAMPLE_ASSESSMENT_3 = "INSERT INTO Assessment (" +
                                                        AssessmentTable.COLUMN_TITLE + ", " +
                                                        AssessmentTable.COLUMN_TYPE + ", " +
                                                        AssessmentTable.COLUMN_DUE + ", " +
                                                        AssessmentTable.COLUMN_STATUS + ", " +
                                                        AssessmentTable.COLUMN_COURSE +
                                                        ") VALUES('Perf2', 'P', 1521090000000, " +
                                                        "'PT', 3)";

        public static final String SAMPLE_ASSESSMENT_4 = "INSERT INTO Assessment (" +
                                                        AssessmentTable.COLUMN_TITLE + ", " +
                                                        AssessmentTable.COLUMN_TYPE + ", " +
                                                        AssessmentTable.COLUMN_DUE + ", " +
                                                        AssessmentTable.COLUMN_STATUS + ", " +
                                                        AssessmentTable.COLUMN_COURSE +
                                                        ") VALUES('Obj2', 'O', 1522558800000, " +
                                                        "'PT', 4)";

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
                                                COLUMN_STATUS + " TEXT, " +
                                                COLUMN_NOTES + " TEXT, " +
                                                "FOREIGN KEY(" + COLUMN_TERM + ") REFERENCES " +
                                                TermTable.TABLE_NAME + " (" + TermTable._ID + ") )";
    }

    public static class AssessmentTable implements BaseColumns {
        public static final String TABLE_NAME = "Assessment";
        public static final String COLUMN_TITLE = "Title";
        public static final String COLUMN_TYPE = "Type";
        public static final String COLUMN_DUE = "Due";
        public static final String COLUMN_STATUS = "Status";
        public static final String COLUMN_COURSE = "Course";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                                                TABLE_NAME + "(" +
                                                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                COLUMN_TITLE + " TEXT, " +
                                                COLUMN_TYPE + " TEXT, " +
                                                COLUMN_DUE + " INTEGER, " +
                                                COLUMN_STATUS + " TEXT, " +
                                                COLUMN_COURSE + " INTEGER, " +
                                                "FOREIGN KEY(" + COLUMN_COURSE + ") REFERENCES " +
                                                CourseTable.TABLE_NAME + " (" + CourseTable._ID + ") )";
    }


}
