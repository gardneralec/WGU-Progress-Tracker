package com.alecgardner.wgu_progress_tracker;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
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

public class AssessmentDetailsActivity extends AppCompatActivity {

    private EditText assessmentTitle;
    private Spinner assessmentCourse;
    private Spinner assessmentType;
    private EditText assessmentGoal;
    private Spinner assessmentStatus;
    private Assessment selectedAssessment;
    private Date selectedDueDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_detail);

        Intent intent = getIntent();
        int assessmentID = intent.getIntExtra("ASSESSMENT_ID", 0);

        selectedAssessment = getAssessmentFromDB(assessmentID);

        populateAssessmentFields(selectedAssessment);

        Button deleteButton = findViewById(R.id.assessmentDelete);

        Button submitButton = findViewById(R.id.modifyAssessmentSubmit);

        Button setAlert = findViewById(R.id.assessmentSetAlarm);

        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                submitSaveChanges();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                deleteAssessment();
            }
        });

        setAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedAssessment.assessmentDueDate.get() > System.currentTimeMillis()) {
                    scheduleNotification(getNotification("Goal date for Assessment " + selectedAssessment.assessmentTitle.get() + " today."), selectedAssessment.assessmentDueDate.get());
                    Context context = getApplicationContext();
                    CharSequence text = "Goal date notification scheduled.";
                    int duration = Toast.LENGTH_SHORT;
                    Toast missingFields = Toast.makeText(context, text, duration);
                    missingFields.show();
                } else {
                    Context context = getApplicationContext();
                    CharSequence text = "Goal date is already past.";
                    int duration = Toast.LENGTH_SHORT;
                    Toast missingFields = Toast.makeText(context, text, duration);
                    missingFields.show();
                }
            }
        });

    }

    private void deleteAssessment() {
        final AlertDialog alertDialog = new AlertDialog.Builder(AssessmentDetailsActivity.this).create();
        alertDialog.setTitle("Delete Assessment");
        alertDialog.setMessage("Are you sure you want to delete this assessment?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase database = new DBSQLiteHelper(getApplicationContext()).getReadableDatabase();

                        database.delete("Assessment", "_ID = " + selectedAssessment.assessmentID.get(), null);

                        finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                    }
                });
        alertDialog.show();

    }

    private void submitSaveChanges() {
        if(checkFieldsForNull()) {
            Course selectedCourse = (Course) assessmentCourse.getSelectedItem();

            Editable myString = assessmentTitle.getText();

            modifyAssessmentInDB(selectedAssessment.assessmentID.get(), myString.toString(), selectedCourse.courseID.get(),
                                assessmentStatus.getSelectedItem().toString(), selectedDueDate.getTime(), assessmentType.getSelectedItem().toString());

            finish();
        } else {
            Context context = getApplicationContext();
            CharSequence text = "Information error, please review fields";
            int duration = Toast.LENGTH_SHORT;
            Toast missingFields = Toast.makeText(context, text, duration);
            missingFields.show();
        }
    }

    private void modifyAssessmentInDB(int assessmentID, String title, int courseID, String complete, long goalDate, String type) {

        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DBContract.AssessmentTable.COLUMN_TITLE, title);
        cv.put(DBContract.AssessmentTable.COLUMN_COURSE, courseID);
        cv.put(DBContract.AssessmentTable.COLUMN_STATUS, complete);
        cv.put(DBContract.AssessmentTable.COLUMN_DUE, goalDate);
        cv.put(DBContract.AssessmentTable.COLUMN_TYPE, type);

        database.update("Assessment", cv, "_id = " + assessmentID, null);

        database.close();
    }

    private boolean checkFieldsForNull() {
        boolean allFieldsFilled = false;

        if(assessmentTitle.getText() != null && assessmentGoal.getText() != null) {
            allFieldsFilled = true;
        }

        return allFieldsFilled;
    }

    private void populateAssessmentFields(Assessment foundAssessment) {

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

        database.close();

        courseAdapter.addAll(courseArrayList);

        assessmentCourse = findViewById(R.id.assessmentCourseTitle);

        assessmentCourse.setAdapter(courseAdapter);

        int courseCount = 0;

        if(!courseAdapter.isEmpty()) {

            while (courseCount < courseAdapter.getCount()) {
                if (courseAdapter.getItem(courseCount).courseID.get() == foundAssessment.assessmentCourse.get()) {
                    assessmentCourse.setSelection(courseCount);
                    break;
                } else{
                    courseCount++;
                }
            }

        }

        assessmentType = findViewById(R.id.assessmentType);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this, R.array.assessmentType, R.layout.spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assessmentType.setAdapter(typeAdapter);

        if(foundAssessment.assessmentType.get() != null) {
            int spinnerLocation = typeAdapter.getPosition(foundAssessment.assessmentType.get());
            assessmentType.setSelection(spinnerLocation);
        }

        assessmentStatus = findViewById(R.id.assessmentStatus);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this, R.array.termStatus, R.layout.spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assessmentStatus.setAdapter(statusAdapter);

        if(foundAssessment.assessmentStatus.get() != null) {

            int spinnerLocation = statusAdapter.getPosition(foundAssessment.assessmentStatus.get());
            assessmentStatus.setSelection(spinnerLocation);
        }

        assessmentTitle = findViewById(R.id.assessmentTitle);
        assessmentTitle.setText(foundAssessment.assessmentTitle.get());

        assessmentGoal = findViewById(R.id.assessmentGoal);
        assessmentGoal.setKeyListener(null);

        Date currentGoal = new Date(foundAssessment.assessmentDueDate.get());

        final Calendar goalCalendar = Calendar.getInstance();
        goalCalendar.setTime(currentGoal);
        selectedDueDate = goalCalendar.getTime();
        updateLabel(assessmentGoal, goalCalendar);

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
                selectedDueDate = goalCalendar.getTime();
                updateLabel(assessmentGoal, goalCalendar);
            }

        };

        assessmentGoal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(AssessmentDetailsActivity.this, goalDate, goalCalendar
                        .get(Calendar.YEAR), goalCalendar.get(Calendar.MONTH),
                        goalCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private Assessment getAssessmentFromDB(int ID) {
        Assessment clickedAssessment = new Assessment();

        String[] selectionArgs = {
                Integer.toString(ID)
        };

        String selection = "_ID = ?";

        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        String[] assessmentProjection = {
                DBContract.AssessmentTable._ID,
                DBContract.AssessmentTable.COLUMN_TITLE,
                DBContract.AssessmentTable.COLUMN_TYPE,
                DBContract.AssessmentTable.COLUMN_DUE,
                DBContract.AssessmentTable.COLUMN_STATUS,
                DBContract.AssessmentTable.COLUMN_COURSE
        };

        Cursor assessmentCursor = database.query(
                DBContract.AssessmentTable.TABLE_NAME,
                assessmentProjection,
                selection,
                selectionArgs,
                null,
                null,
                null,
                null
        );

        if(assessmentCursor.getCount() != 0) {

            assessmentCursor.moveToFirst();

            clickedAssessment.assessmentID.set(assessmentCursor.getInt(0));
            clickedAssessment.assessmentTitle.set(assessmentCursor.getString(1));
            clickedAssessment.assessmentType.set(assessmentCursor.getString(2));
            clickedAssessment.assessmentDueDate.set(assessmentCursor.getLong(3));
            clickedAssessment.assessmentStatus.set(assessmentCursor.getString(4));
            clickedAssessment.assessmentCourse.set(assessmentCursor.getInt(5));
        }

        assessmentCursor.close();

        database.close();



        return clickedAssessment;
    }

    private void updateLabel(EditText view, Calendar calendar) {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        view.setText(sdf.format(calendar.getTime()));
    }

    private void scheduleNotification(Notification notification, long date) {

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 3);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 2, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager2 = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager2.set(AlarmManager.RTC_WAKEUP, date, pendingIntent);
    }

    private Notification getNotification(String content) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Assessment Reminder");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.calendar_icon_72);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("Reminders");
        }
        return builder.build();
    }

}