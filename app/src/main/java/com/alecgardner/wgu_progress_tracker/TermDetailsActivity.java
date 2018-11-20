package com.alecgardner.wgu_progress_tracker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TermDetailsActivity extends AppCompatActivity {

    private EditText termTitle;
    private EditText termStart;
    private EditText termEnd;
    private Spinner termStatus;
    private Term foundTerm;
    private int selectedTermNumber;
    private Date selectedStartDate;
    private Date selectedEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_details);

        Intent intent = getIntent();
        final int termID = intent.getIntExtra("TERM_ID", 0);

        foundTerm = getTermFromDB(termID);

        populateTermFields(foundTerm);

        Button deleteButton = findViewById(R.id.termDetailsDelete);

        Button submitButton = findViewById(R.id.termDetailsSave);

        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                submitSaveChanges(termID);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                deleteTerm(termID);
            }
        });

    }

    private void deleteTerm(final int termID) {
        final AlertDialog alertDialog = new AlertDialog.Builder(TermDetailsActivity.this).create();
        alertDialog.setTitle("Delete Term");
        alertDialog.setMessage("Are you sure you want to delete this term?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        if(checkTermCourses(foundTerm)) {
                            SQLiteDatabase database = new DBSQLiteHelper(getApplicationContext()).getReadableDatabase();

                            database.delete("Term", "_ID = " + termID, null);

                            database.close();

                            finish();
                        } else {
                            Context context = getApplicationContext();
                            CharSequence text = "Please delete or relocate Courses currently associated with this Term";
                            int duration = Toast.LENGTH_LONG;
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

    private void submitSaveChanges(int termID) {
        if(checkFields(pullTermsFromDB(termID))) {

            modifyTermInDB(foundTerm.termID.get(), selectedTermNumber, termStatus.getSelectedItem().toString(),
                           selectedStartDate.getTime(), selectedEndDate.getTime());

            finish();
        }
    }

    private void modifyTermInDB(int termID, int termNumber, String status, long startDate, long endDate) {

        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DBContract.TermTable.COLUMN_NUMBER, termNumber);
        cv.put(DBContract.TermTable.COLUMN_STATUS, status);
        cv.put(DBContract.TermTable.COLUMN_START, startDate);
        cv.put(DBContract.TermTable.COLUMN_END, endDate);

        database.update("Term", cv, "_ID = " + termID, null);

        database.close();
    }

    private boolean checkTermCourses(Term term) {
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
                "TermID = " + term.termID.get(),
                null,
                null,
                null,
                null,
                null
        );

        boolean coursesNotFound = true;

        if(courseCursor.getCount() != 0) {
            coursesNotFound = false;
        }

        courseCursor.close();

        database.close();

        return(coursesNotFound);

    }

    private void populateTermFields(Term foundTerm) {

        termStatus = findViewById(R.id.termDetailsStatus);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this, R.array.termStatus, R.layout.spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        termStatus.setAdapter(statusAdapter);

        if(foundTerm.termStatus.get() != null) {

            int spinnerLocation = statusAdapter.getPosition(foundTerm.termStatus.get());
            termStatus.setSelection(spinnerLocation);
        }

        String termText = "Term " + foundTerm.termNumber.get();
        termTitle = findViewById(R.id.termDetailsTitle);
        termTitle.setText(termText);
        termTitle.setKeyListener(null);
        termTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                termNumberPickerShow(view);
            }
        });

        termStart = findViewById(R.id.termDetailsStart);
        termStart.setKeyListener(null);

        Date currentStart = new Date(foundTerm.termStart.get());

        final Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(currentStart);
        selectedStartDate = startCalendar.getTime();
        updateLabel(termStart, startCalendar);

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
                updateLabel(termStart, startCalendar);
            }

        };

        termStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(TermDetailsActivity.this, startDate, startCalendar
                        .get(Calendar.YEAR), startCalendar.get(Calendar.MONTH),
                        startCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        termEnd = findViewById(R.id.termDetailsEnd);
        termEnd.setKeyListener(null);

        Date currentEnd = new Date(foundTerm.termEnd.get());

        final Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(currentEnd);
        selectedEndDate = endCalendar.getTime();
        updateLabel(termEnd, endCalendar);

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
                updateLabel(termEnd, endCalendar);
            }

        };

        termEnd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(TermDetailsActivity.this, endDate, endCalendar
                        .get(Calendar.YEAR), endCalendar.get(Calendar.MONTH),
                        endCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private Term getTermFromDB(int termID) {
        Term clickedTerm = new Term();

        String[] selectionArgs = {
                Integer.toString(termID)
        };

        String selection = "_ID = ?";

        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        String[] termProjection = {
                DBContract.TermTable._ID,
                DBContract.TermTable.COLUMN_NUMBER,
                DBContract.TermTable.COLUMN_STATUS,
                DBContract.TermTable.COLUMN_START,
                DBContract.TermTable.COLUMN_END
        };

        Cursor termCursor = database.query(
                DBContract.TermTable.TABLE_NAME,
                termProjection,
                selection,
                selectionArgs,
                null,
                null,
                null,
                null
        );

        if(termCursor.getCount() != 0) {

            termCursor.moveToFirst();

            clickedTerm.termID.set(termCursor.getInt(0));
            clickedTerm.termNumber.set(termCursor.getInt(1));
            clickedTerm.termStatus.set(termCursor.getString(2));
            clickedTerm.termStart.set(termCursor.getLong(3));
            clickedTerm.termEnd.set(termCursor.getLong(4));
        }

        termCursor.close();

        database.close();

        return clickedTerm;
    }

    private void updateLabel(EditText view, Calendar calendar) {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        view.setText(sdf.format(calendar.getTime()));
    }

    private boolean checkRequiredFieldsForNull() {
        boolean allFieldsFilled = false;

        if((selectedTermNumber != -1) && (selectedStartDate != null) && (selectedEndDate != null)) {
            allFieldsFilled = true;
        }

        return allFieldsFilled;
    }

    private boolean checkTermNumber(ArrayList<Term> termList) {
        boolean termNumberNotRepeated = true;

        for(Term term:termList) {
            if(term.termNumber.get() == selectedTermNumber) {
                termNumberNotRepeated = false;
            }
        }

        return termNumberNotRepeated;
    }

    private boolean checkDateMismatch() {

        return(!(selectedStartDate.getTime() >= selectedEndDate.getTime()));
    }

    private boolean checkDateOverlap(ArrayList<Term> termList) {

        for(Term term:termList) {
            if((selectedStartDate.getTime() <= term.termStart.get()) & (selectedEndDate.getTime() >= term.termEnd.get())) {
                return false;
            }

            if((selectedStartDate.getTime() >= term.termStart.get()) && (selectedStartDate.getTime() <= term.termEnd.get())) {
                return false;
            }

            if((selectedEndDate.getTime() >= term.termStart.get()) & (selectedEndDate.getTime() <= term.termEnd.get())) {
                return false;
            }
        }

        return true;

    }

    private boolean checkFields(ArrayList<Term> termList) {
        if(checkRequiredFieldsForNull()) {
            if(checkTermNumber(termList)) {
                if(checkDateMismatch()) {
                    if (checkDateOverlap(termList)) {
                        return true;

                    } else {
                        Context context = getApplicationContext();
                        CharSequence text = "Term dates cannot overlap with an existing term";
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
                CharSequence text = "Term number already used";
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

    private ArrayList<Term> pullTermsFromDB(int termID) {
        ArrayList<Term> termList = new ArrayList<>();

        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        String[] termProjection = {
                DBContract.TermTable._ID,
                DBContract.TermTable.COLUMN_NUMBER,
                DBContract.TermTable.COLUMN_START,
                DBContract.TermTable.COLUMN_END
        };

        Cursor termCursor = database.query(
                DBContract.TermTable.TABLE_NAME,     // The table to query
                termProjection,                          // The columns to return
                "_ID != " + termID,                       // The columns for the WHERE clause
                null,                    // The values for the WHERE clause
                null,                        // don't group the rows
                null,                         // don't filter by row groups
                null                         // don't sort
        );

        if(termCursor.getCount() != 0) {

            termCursor.moveToFirst();

            do {
                Term term = new Term();
                term.termID.set(termCursor.getInt(0));
                term.termNumber.set(termCursor.getInt(1));
                term.termStart.set(termCursor.getLong(2));
                term.termEnd.set(termCursor.getLong(3));
                termList.add(term);
            } while (termCursor.moveToNext());
        }

        termCursor.close();

        database.close();

        return termList;
    }

    public void termNumberPickerShow(View view) {

        final Dialog d = new Dialog(TermDetailsActivity.this);
        d.setTitle("Select Term Number");
        d.setContentView(R.layout.add_term_number_picker);
        Button b1 = d.findViewById(R.id.button1);
        Button b2 = d.findViewById(R.id.button2);
        final NumberPicker np = d.findViewById(R.id.numberPicker1);
        np.setMinValue(1);
        np.setMaxValue(10000);
        np.setWrapSelectorWheel(false);
        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                String termText = "Term " + String.valueOf(np.getValue());
                termTitle.setText(termText);
                selectedTermNumber = np.getValue();
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();

    }

}