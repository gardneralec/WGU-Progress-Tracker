package com.alecgardner.wgu_progress_tracker;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddCourseActivity extends AppCompatActivity {

    private EditText courseTitle;
    private EditText courseStart;
    private EditText courseEnd;
    private Spinner courseStatus;
    private Spinner courseTerm;
    private EditText courseNotes;
    private Date selectedStartDate;
    private Date selectedEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        Intent intent = getIntent();
        final int termID = intent.getIntExtra("TERM_ID", 0);

        populateCourseSpinners(termID);

        Button submitButton = findViewById(R.id.addCourseSubmit);

        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                submitSaveChanges();
            }
        });

    }

    private void submitSaveChanges() {
        if(checkFields(pullCoursesFromDB())) {

            Term selectedTerm = (Term) courseTerm.getSelectedItem();

            addCourseToDB(courseTitle.getText().toString(), selectedTerm.termID.get(),
                           selectedStartDate.getTime(), selectedEndDate.getTime(), courseNotes.getText().toString(), courseStatus.getSelectedItem().toString());

            finish();
        }
    }

    private void addCourseToDB(String courseTitle, int termID, long startDate, long endDate, String notes, String status) {

        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DBContract.CourseTable.COLUMN_TITLE, courseTitle);
        cv.put(DBContract.CourseTable.COLUMN_TERM, termID);
        cv.put(DBContract.CourseTable.COLUMN_START, startDate);
        cv.put(DBContract.CourseTable.COLUMN_END, endDate);
        cv.put(DBContract.CourseTable.COLUMN_NOTES, notes);
        cv.put(DBContract.TermTable.COLUMN_STATUS, status);

        database.insert("Course", null, cv);

        database.close();
    }

    private void populateCourseSpinners(int termID) {

        ArrayAdapter<Term> termAdapter = new ArrayAdapter<Term>(this, R.layout.spinner_item, new ArrayList<Term>()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                String text = "Term " + getItem(position).termNumber.get();
                view.setText(text);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                String text = "Term " + getItem(position).termNumber.get();
                view.setText(text);
                return view;
            }
        };
        termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        String[] termProjection = {
                DBContract.TermTable._ID,
                DBContract.TermTable.COLUMN_NUMBER,
                DBContract.TermTable.COLUMN_START,
                DBContract.TermTable.COLUMN_END,
                DBContract.TermTable.COLUMN_STATUS
        };

        Cursor termCursor = database.query(
                DBContract.TermTable.TABLE_NAME,
                termProjection,
                null,
                null,
                null,
                null,
                null,
                null
        );

        ArrayList<Term> termArrayList = new ArrayList<>();

        if(termCursor.getCount() > 0) {

            termCursor.moveToFirst();

            do {

                Term term = new Term();
                term.termID.set(termCursor.getInt(0));
                term.termNumber.set(termCursor.getInt(1));
                term.termStart.set(termCursor.getLong(2));
                term.termEnd.set(termCursor.getLong(3));
                term.termStatus.set(termCursor.getString(4));
                termArrayList.add(term);
            } while(termCursor.moveToNext());

        }

        termCursor.close();

        database.close();

        termAdapter.addAll(termArrayList);

        courseTerm = findViewById(R.id.addCourseTerm);

        courseTerm.setAdapter(termAdapter);

        int termCount = 0;

        if(!termAdapter.isEmpty()) {

            while (termCount < termAdapter.getCount()) {
                if (termAdapter.getItem(termCount).termID.get() == termID) {
                    courseTerm.setSelection(termCount);
                    break;
                } else{
                    termCount++;
                }
            }
        }

        courseStatus = findViewById(R.id.addCourseStatus);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this, R.array.courseStatus, R.layout.spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseStatus.setAdapter(statusAdapter);

        courseTitle = findViewById(R.id.addCourseTitle);

        courseStart = findViewById(R.id.addCourseStart);
        courseStart.setKeyListener(null);

        final Calendar startCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                startCalendar.set(Calendar.YEAR, year);
                startCalendar.set(Calendar.MONTH, monthOfYear);
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                startCalendar.set(Calendar.MINUTE, 0);
                startCalendar.set(Calendar.SECOND, 0);
                startCalendar.set(Calendar.MILLISECOND, 0);
                selectedStartDate = startCalendar.getTime();
                updateLabel(courseStart, startCalendar);
            }

        };

        courseStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(AddCourseActivity.this, startDate, startCalendar
                        .get(Calendar.YEAR), startCalendar.get(Calendar.MONTH),
                        startCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        courseEnd = findViewById(R.id.addCourseEnd);
        courseEnd.setKeyListener(null);

        final Calendar endCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener endDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                endCalendar.set(Calendar.YEAR, year);
                endCalendar.set(Calendar.MONTH, monthOfYear);
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                endCalendar.set(Calendar.HOUR_OF_DAY, 0);
                endCalendar.set(Calendar.MINUTE, 0);
                endCalendar.set(Calendar.SECOND, 0);
                endCalendar.set(Calendar.MILLISECOND, 0);
                selectedEndDate = endCalendar.getTime();
                updateLabel(courseEnd, endCalendar);
            }

        };

        courseEnd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(AddCourseActivity.this, endDate, endCalendar
                        .get(Calendar.YEAR), endCalendar.get(Calendar.MONTH),
                        endCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        courseNotes = findViewById(R.id.addCourseNotes);
    }

    private void updateLabel(EditText view, Calendar calendar) {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        view.setText(sdf.format(calendar.getTime()));
    }

    private boolean checkRequiredFieldsForNull() {
        boolean allFieldsFilled = false;

        if((courseTitle.getText().toString().length() > 0) && (selectedStartDate != null) && (selectedEndDate != null)) {
            allFieldsFilled = true;
        }

        return allFieldsFilled;
    }

    private boolean checkDateMismatch() {

        return(!(selectedStartDate.getTime() >= selectedEndDate.getTime()));
    }

    private boolean checkDateOverlap(ArrayList<Course> courseList) {

        for(Course course:courseList) {
            if((selectedStartDate.getTime() <= course.courseStart.get()) & (selectedEndDate.getTime() >= course.courseEnd.get())) {
                return false;
            }

            if((selectedStartDate.getTime() >= course.courseStart.get()) && (selectedStartDate.getTime() <= course.courseEnd.get())) {
                return false;
            }

            if((selectedEndDate.getTime() >= course.courseStart.get()) & (selectedEndDate.getTime() <= course.courseEnd.get())) {
                return false;
            }
        }

        return true;

    }

    private boolean checkFields(ArrayList<Course> courseList) {
        if(checkRequiredFieldsForNull()) {
                if(checkDateMismatch()) {
                    if (checkDateOverlap(courseList)) {
                        return true;

                    } else {
                        Context context = getApplicationContext();
                        CharSequence text = "Course dates cannot overlap with an existing course";
                        int duration = Toast.LENGTH_SHORT;
                        Toast missingFields = Toast.makeText(context, text, duration);
                        missingFields.show();
                    }
                } else {
                    Context context = getApplicationContext();
                    CharSequence text = "Start Date cannot be after End Date";
                    int duration = Toast.LENGTH_SHORT;
                    Toast missingFields = Toast.makeText(context, text, duration);
                    missingFields.show();
                }

        } else {
            Context context = getApplicationContext();
            CharSequence text = "Missing Fields";
            int duration = Toast.LENGTH_SHORT;
            Toast missingFields = Toast.makeText(context, text, duration);
            missingFields.show();
        }

        return false;
    }

    private ArrayList<Course> pullCoursesFromDB() {
        ArrayList<Course> courseList = new ArrayList<>();

        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        String[] courseProjection = {
                DBContract.CourseTable._ID,
                DBContract.CourseTable.COLUMN_START,
                DBContract.CourseTable.COLUMN_END
        };

        Cursor courseCursor = database.query(
                DBContract.CourseTable.TABLE_NAME,     // The table to query
                courseProjection,                          // The columns to return
                null,                       // The columns for the WHERE clause
                null,                    // The values for the WHERE clause
                null,                        // don't group the rows
                null,                         // don't filter by row groups
                null                         // don't sort
        );

        if(courseCursor.getCount() != 0) {

            courseCursor.moveToFirst();

            do {
                Course course = new Course();
                course.courseID.set(courseCursor.getInt(0));
                course.courseStart.set(courseCursor.getLong(1));
                course.courseEnd.set(courseCursor.getLong(2));
                courseList.add(course);
            } while (courseCursor.moveToNext());
        }

        courseCursor.close();

        database.close();

        return courseList;
    }

}