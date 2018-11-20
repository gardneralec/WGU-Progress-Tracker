package com.alecgardner.wgu_progress_tracker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddAssessmentActivity extends AppCompatActivity {

    private EditText assessmentTitle;
    private Spinner statusSpinner;
    private Spinner assessmentTypeSpinner;
    private Spinner assessmentCourseSpinner;
    private EditText goalText;
    private Date selectedGoalDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_assessment);

        Intent intent = getIntent();
        int courseID = intent.getIntExtra("COURSE_ID", 0);

        // Assessment title EditText assignment
        assessmentTitle = findViewById(R.id.addAssessmentTitle);

        // Assessment status spinner assignment and adapter
        statusSpinner = findViewById(R.id.addAssessmentStatus);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                                             R.array.termStatus, R.layout.spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        // Assessment type spinner assignment and adapter
        assessmentTypeSpinner = findViewById(R.id.addAssessmentType);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this, R.array.assessmentType, R.layout.spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assessmentTypeSpinner.setAdapter(typeAdapter);

        // Assessment parent course Spinner assignment and adapter - converts course id to titles for user view
        // uses SQLite database query to pull existing Courses into Spinner dropdown items
        ArrayAdapter<Course> courseAdapter = new ArrayAdapter<Course>(this, R.layout.spinner_item, new ArrayList<Course>()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setText(getItem(position).courseTitle.get());
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setText(getItem(position).courseTitle.get());
                return view;
            }
        };
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        String[] courseProjection = {
                DBContract.CourseTable._ID,
                DBContract.CourseTable.COLUMN_TITLE,
                DBContract.CourseTable.COLUMN_START,
                DBContract.CourseTable.COLUMN_END,
                DBContract.CourseTable.COLUMN_TERM,
                DBContract.CourseTable.COLUMN_STATUS,
                DBContract.CourseTable.COLUMN_NOTES
        };

        Cursor courseCursor = database.query(
                DBContract.CourseTable.TABLE_NAME,
                courseProjection,
                null,
                null,
                null,
                null,
                null,
                null
        );

        ArrayList<Course> courseArrayList = new ArrayList<>();

        if(courseCursor.getCount() != 0) {

            courseCursor.moveToFirst();

            do {

                Course course = new Course();
                course.courseID.set(courseCursor.getInt(0));
                course.courseTitle.set(courseCursor.getString(1));
                course.courseStart.set(courseCursor.getLong(2));
                course.courseEnd.set(courseCursor.getLong(3));
                course.courseTermID.set(courseCursor.getInt(4));
                course.courseStatus.set(courseCursor.getString(5));
                course.courseNotes.set(courseCursor.getString(6));
                courseArrayList.add(course);
            } while(courseCursor.moveToNext());

        }

        courseCursor.close();

        courseAdapter.addAll(courseArrayList);

        assessmentCourseSpinner = findViewById(R.id.addAssessmentCourseTitle);

        assessmentCourseSpinner.setAdapter(courseAdapter);

        int courseCount = 0;

        if(!courseAdapter.isEmpty()) {

            while (courseCount < courseAdapter.getCount()) {
                if (courseAdapter.getItem(courseCount).courseID.get() == courseID) {
                    assessmentCourseSpinner.setSelection(courseCount);
                    break;
                } else{
                    courseCount++;
                }
            }

        }


        // Assessment goal EditText assignment, Calendar creation based on current date, and date dialog
        // for user assignment of date. Dialog updates Calendar and which translates to Date (selectedGoalDate) for use in saving function.
        final Calendar goalCalendar = Calendar.getInstance();

        goalText = findViewById(R.id.addAssessmentGoal);
        goalText.setKeyListener(null);
        final DatePickerDialog.OnDateSetListener goalDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                goalCalendar.set(Calendar.YEAR, year);
                goalCalendar.set(Calendar.MONTH, monthOfYear);
                goalCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                goalCalendar.set(Calendar.HOUR_OF_DAY, 0);
                goalCalendar.set(Calendar.MINUTE, 0);
                goalCalendar.set(Calendar.SECOND, 0);
                goalCalendar.set(Calendar.MILLISECOND, 0);
                updateLabel(goalText, goalCalendar);
                selectedGoalDate = goalCalendar.getTime();
            }

        };

        goalText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(AddAssessmentActivity.this, goalDate, goalCalendar
                        .get(Calendar.YEAR), goalCalendar.get(Calendar.MONTH),
                        goalCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // Submit button assignment and onClick override to utilize onSubmitClickHandler method
        Button submitAssessment = findViewById(R.id.addAssessmentSubmit);

        submitAssessment.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onSubmitClickHandler(v);
            }

        });

    }

    private void updateLabel(EditText view, Calendar calendar) {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        view.setText(sdf.format(calendar.getTime()));
    }

    public void onSubmitClickHandler(View view) {

        if(checkRequiredFieldsForNull()) {
            Course selectedCourse = (Course) assessmentCourseSpinner.getSelectedItem();

            Editable myString = assessmentTitle.getText();

            addAssessmentToDB(myString.toString(), selectedCourse.courseID.get(),
                    statusSpinner.getSelectedItem().toString(), selectedGoalDate.getTime(), assessmentTypeSpinner.getSelectedItem().toString());

            finish();
        } else {
            Context context = getApplicationContext();
            CharSequence text = "Information error, please review fields";
            int duration = Toast.LENGTH_SHORT;
            Toast missingFields = Toast.makeText(context, text, duration);
            missingFields.show();
        }
    }


    private boolean checkRequiredFieldsForNull() {
        boolean allFieldsFilled = false;

        if((assessmentTitle.getText() != null) && (selectedGoalDate != null)) {
            allFieldsFilled = true;
        }

        return allFieldsFilled;
    }


    private void addAssessmentToDB(String title, int courseID, String complete, long goalDate, String type) {

        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DBContract.AssessmentTable.COLUMN_TITLE, title);
        cv.put(DBContract.AssessmentTable.COLUMN_COURSE, courseID);
        cv.put(DBContract.AssessmentTable.COLUMN_STATUS, complete);
        cv.put(DBContract.AssessmentTable.COLUMN_DUE, goalDate);
        cv.put(DBContract.AssessmentTable.COLUMN_TYPE, type);

        database.insert("Assessment", null, cv);
    }

}
