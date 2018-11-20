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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CourseDetailsActivity extends AppCompatActivity {

    private EditText courseTitle;
    private EditText courseStart;
    private EditText courseEnd;
    private Spinner courseStatus;
    private Spinner courseTerm;
    private LinearLayout cmParentView;
    private EditText courseNotes;
    private Course foundCourse;
    private Date selectedStartDate;
    private Date selectedEndDate;
    private float scale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        Intent intent = getIntent();
        final int courseID = intent.getIntExtra("COURSE_ID", 0);

        scale = getResources().getDisplayMetrics().density;

        foundCourse = getCourseFromDB(courseID);

        populateCourseFields(foundCourse);

        cmParentView = findViewById(R.id.courseDetailsCMParent);

        cmParentView.addView(generateCourseMentors(pullCourseMentorsFromDB(courseID)));

        Button deleteButton = findViewById(R.id.courseDetailsDelete);

        Button submitButton = findViewById(R.id.courseDetailsSave);

        Button addCMButton = findViewById(R.id.courseDetailsAddCM);

        Button shareNotesButton = findViewById(R.id.courseDetailsShareNotes);

        Button setAlert = findViewById(R.id.courseDetailsSetAlarm);

        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                submitSaveChanges(courseID);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                deleteCourse();
            }
        });

        addCMButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCMClickHandler(foundCourse.courseID.get());
            }
        });

        setAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(foundCourse.courseStart.get() > System.currentTimeMillis() || foundCourse.courseEnd.get() > System.currentTimeMillis()) {
                    scheduleStartNotification(getNotification("Test to show notification functionality."), System.currentTimeMillis() + 5000);
                    scheduleStartNotification(getNotification("Start Course " + foundCourse.courseTitle.get() + " today."), foundCourse.courseStart.get());
                    scheduleEndNotification(getNotification("End Course " + foundCourse.courseTitle.get() + " today."), foundCourse.courseEnd.get());
                    Context context = getApplicationContext();
                    CharSequence text = "Start/End Date reminders scheduled";
                    int duration = Toast.LENGTH_SHORT;
                    Toast missingFields = Toast.makeText(context, text, duration);
                    missingFields.show();
                } else {
                    Context context = getApplicationContext();
                    CharSequence text = "Start/End dates have already passed";
                    int duration = Toast.LENGTH_SHORT;
                    Toast missingFields = Toast.makeText(context, text, duration);
                    missingFields.show();
                }
            }
        });

        shareNotesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareNotes("My Course Notes for " + foundCourse.courseTitle.get(), foundCourse.courseNotes.get());
            }
        });

    }

    private void deleteCourse() {
        final AlertDialog alertDialog = new AlertDialog.Builder(CourseDetailsActivity.this).create();
        alertDialog.setTitle("Delete Course");
        alertDialog.setMessage("Are you sure you want to delete this course?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        if(checkCourseAssessments(foundCourse)) {
                            SQLiteDatabase database = new DBSQLiteHelper(getApplicationContext()).getReadableDatabase();

                            database.delete("Course", "_ID = " + foundCourse.courseID.get(), null);

                            database.close();

                            finish();
                        } else {
                            Context context = getApplicationContext();
                            CharSequence text = "Please delete or relocate Assessments currently associated with this Course";
                            int duration = Toast.LENGTH_SHORT;
                            Toast existingCourses = Toast.makeText(context, text, duration);
                            existingCourses.show();
                        }
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

    private void shareNotes(String subject, String message) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        Intent mailer = Intent.createChooser(intent, null);
        startActivity(mailer);
    }

    private void submitSaveChanges(int courseID) {
        if(checkFields(pullCoursesFromDB(courseID))) {

            Term selectedTerm = (Term) courseTerm.getSelectedItem();

            modifyCourseInDB(foundCourse.courseID.get(), courseTitle.getText().toString(), selectedTerm.termID.get(),
                           selectedStartDate.getTime(), selectedEndDate.getTime(), courseNotes.getText().toString(), courseStatus.getSelectedItem().toString());

            finish();
        }
    }

    private void modifyCourseInDB(int courseID, String courseTitle, int termID, long startDate, long endDate, String notes, String status) {

        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DBContract.CourseTable.COLUMN_TITLE, courseTitle);
        cv.put(DBContract.CourseTable.COLUMN_TERM, termID);
        cv.put(DBContract.CourseTable.COLUMN_START, startDate);
        cv.put(DBContract.CourseTable.COLUMN_END, endDate);
        cv.put(DBContract.CourseTable.COLUMN_NOTES, notes);
        cv.put(DBContract.TermTable.COLUMN_STATUS, status);

        database.update("Course", cv, "_ID = " + courseID, null);

        database.close();
    }

    private boolean checkCourseAssessments(Course course) {
        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        String[] assessmentProjection = {
                DBContract.AssessmentTable._ID
        };

        Cursor assessmentCursor = database.query(
                DBContract.AssessmentTable.TABLE_NAME,
                assessmentProjection,
                "Course = " + course.courseID.get(),
                null,
                null,
                null,
                null,
                null
        );

        assessmentCursor.close();

        database.close();

        return(assessmentCursor.getCount() != 0);

    }

    private void populateCourseFields(Course foundCourse) {

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

        if(termCursor.getCount() != 0) {

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

        courseTerm = findViewById(R.id.courseDetailsTerm);

        courseTerm.setAdapter(termAdapter);

        int termCount = 0;

        if(!termAdapter.isEmpty()) {

            while (termCount < termAdapter.getCount()) {
                if (termAdapter.getItem(termCount).termID.get() == foundCourse.courseTermID.get()) {
                    courseTerm.setSelection(termCount);
                    break;
                } else{
                    termCount++;
                }
            }
        }

        courseStatus = findViewById(R.id.courseDetailsStatus);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this, R.array.courseStatus, R.layout.spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseStatus.setAdapter(statusAdapter);

        if(foundCourse.courseStatus.get() != null) {

            int spinnerLocation = statusAdapter.getPosition(foundCourse.courseStatus.get());
            courseStatus.setSelection(spinnerLocation);
        }

        courseTitle = findViewById(R.id.courseDetailsTitle);
        courseTitle.setText(foundCourse.courseTitle.get());

        courseStart = findViewById(R.id.courseDetailsStart);
        courseStart.setKeyListener(null);

        Date currentStart = new Date(foundCourse.courseStart.get());

        final Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(currentStart);
        selectedStartDate = startCalendar.getTime();
        updateLabel(courseStart, startCalendar);

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
                new DatePickerDialog(CourseDetailsActivity.this, startDate, startCalendar
                        .get(Calendar.YEAR), startCalendar.get(Calendar.MONTH),
                        startCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        courseEnd = findViewById(R.id.courseDetailsEnd);
        courseEnd.setKeyListener(null);

        Date currentEnd = new Date(foundCourse.courseEnd.get());

        final Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(currentEnd);
        selectedEndDate = endCalendar.getTime();
        updateLabel(courseEnd, endCalendar);

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
                new DatePickerDialog(CourseDetailsActivity.this, endDate, endCalendar
                        .get(Calendar.YEAR), endCalendar.get(Calendar.MONTH),
                        endCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        courseNotes = findViewById(R.id.courseDetailsNotes);
        courseNotes.setText(foundCourse.courseNotes.get());
    }

    private Course getCourseFromDB(int courseID) {
        Course clickedCourse = new Course();

        String[] selectionArgs = {
                Integer.toString(courseID)
        };

        String selection = "_ID = ?";

        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        String[] courseProjection = {
                DBContract.CourseTable._ID,
                DBContract.CourseTable.COLUMN_TITLE,
                DBContract.CourseTable.COLUMN_TERM,
                DBContract.CourseTable.COLUMN_START,
                DBContract.CourseTable.COLUMN_END,
                DBContract.CourseTable.COLUMN_STATUS,
                DBContract.CourseTable.COLUMN_NOTES
        };

        Cursor courseCursor = database.query(
                DBContract.CourseTable.TABLE_NAME,
                courseProjection,
                selection,
                selectionArgs,
                null,
                null,
                null,
                null
        );

        if(courseCursor.getCount() != 0) {

            courseCursor.moveToFirst();

            clickedCourse.courseID.set(courseCursor.getInt(0));
            clickedCourse.courseTitle.set(courseCursor.getString(1));
            clickedCourse.courseTermID.set(courseCursor.getInt(2));
            clickedCourse.courseStart.set(courseCursor.getLong(3));
            clickedCourse.courseEnd.set(courseCursor.getLong(4));
            clickedCourse.courseStatus.set(courseCursor.getString(5));
            clickedCourse.courseNotes.set(courseCursor.getString(6));
        }

        courseCursor.close();

        database.close();

        return clickedCourse;
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

    private ArrayList<Course> pullCoursesFromDB(int courseID) {
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
                "_ID != " + courseID,                       // The columns for the WHERE clause
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

    private LinearLayout generateCourseMentors(ArrayList<CourseMentor> cmList) {
        LinearLayout cmRoot = new LinearLayout(this);
        cmRoot.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        cmRoot.setOrientation(LinearLayout.VERTICAL);

        for(final CourseMentor cm:cmList) {
            LinearLayout cmRow = new LinearLayout(this); // root view for information about course
            cmRow.setOrientation(LinearLayout.VERTICAL);
            cmRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            cmRow.setPadding(0, convertToDP(8), 0, convertToDP(8));
            cmRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), EditCMActivity.class);
                    intent.putExtra("CM_ID", cm.cmID.get());
                    startActivity(intent);
                }
            });

            TextView nameView = new TextView(this);
            nameView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            nameView.setTextColor(getResources().getColor(R.color.textDark));
            nameView.setTextSize(18);
            nameView.setPadding(0, convertToDP(4), 0, convertToDP(4));
            nameView.setText(cm.cmName.get());
            cmRow.addView(nameView);

            TextView phoneView = new TextView(this);
            phoneView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            phoneView.setTextColor(getResources().getColor(R.color.textDark));
            phoneView.setTextSize(18);
            phoneView.setPadding(0, convertToDP(4), 0, convertToDP(4));
            phoneView.setText(cm.cmPhone.get());
            cmRow.addView(phoneView);

            TextView emailView = new TextView(this);
            emailView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            emailView.setTextColor(getResources().getColor(R.color.textDark));
            emailView.setTextSize(18);
            emailView.setPadding(0, convertToDP(4), 0, 0);
            emailView.setText(cm.cmEmail.get());
            cmRow.addView(emailView);

            cmRoot.addView(cmRow);
        }

        return cmRoot;
    }

    private void addCMClickHandler(int courseID) {
        Intent intent = new Intent(getApplicationContext(), AddCMActivity.class);
        intent.putExtra("COURSE_ID", courseID);
        startActivity(intent);
    }

    private int convertToDP(int pixels) {
        return (int) (pixels * scale + 0.5f);
    }

    public void onResume() {
        super.onResume();
        cmParentView.removeAllViews();
        cmParentView.addView(generateCourseMentors(pullCourseMentorsFromDB(foundCourse.courseID.get())));
    }

    public ArrayList<CourseMentor> pullCourseMentorsFromDB(int courseID) {
        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        String[] cmProjection = {
                DBContract.CMTable._ID,
                DBContract.CMTable.COLUMN_COURSE,
                DBContract.CMTable.COLUMN_NAME,
                DBContract.CMTable.COLUMN_EMAIL,
                DBContract.CMTable.COLUMN_PHONE
        };

        Cursor cmCursor = database.query(
                DBContract.CMTable.TABLE_NAME,
                cmProjection,
                "Course = " + foundCourse.courseID.get(),
                null,
                null,
                null,
                null,
                null
        );

        ArrayList<CourseMentor> courseMentorArrayList = new ArrayList<>();

        if(cmCursor.getCount() > 0) {
            cmCursor.moveToFirst();

            do {
                CourseMentor cm = new CourseMentor();
                cm.cmID.set(cmCursor.getInt(0));
                cm.courseID.set(cmCursor.getInt(1));
                cm.cmName.set(cmCursor.getString(2));
                cm.cmEmail.set(cmCursor.getString(3));
                cm.cmPhone.set(cmCursor.getString(4));
                courseMentorArrayList.add(cm);
            } while(cmCursor.moveToNext());
        }

        cmCursor.close();

        return courseMentorArrayList;
    }

    private void scheduleStartNotification(Notification notificationStart, long start) {

        Intent notificationIntentStart = new Intent(this, NotificationPublisher.class);
        notificationIntentStart.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntentStart.putExtra(NotificationPublisher.NOTIFICATION, notificationStart);
        PendingIntent pendingIntentStart = PendingIntent.getBroadcast(this, 0, notificationIntentStart, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager1 = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager1.set(AlarmManager.RTC_WAKEUP, start, pendingIntentStart);
    }

    private void scheduleEndNotification(Notification notificationEnd, long end) {

        Intent notificationIntentEnd = new Intent(this, NotificationPublisher.class);
        notificationIntentEnd.putExtra(NotificationPublisher.NOTIFICATION_ID, 2);
        notificationIntentEnd.putExtra(NotificationPublisher.NOTIFICATION, notificationEnd);
        PendingIntent pendingIntentEnd = PendingIntent.getBroadcast(this, 1, notificationIntentEnd, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager2 = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager2.set(AlarmManager.RTC_WAKEUP, end, pendingIntentEnd);
    }

    private Notification getNotification(String content) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Course Reminder");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.calendar_icon_72);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("Reminders");
        }
        return builder.build();
    }

}