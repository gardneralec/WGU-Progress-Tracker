package com.alecgardner.wgu_progress_tracker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.ObservableBoolean;
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

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTermActivity extends AppCompatActivity {

    private EditText termTitle;
    private Spinner statusSpinner;
    private EditText startText;
    private EditText endText;
    private int selectedTermNumber;
    private String selectedTermStatus;
    private Date selectedStartDate;
    private Date selectedEndDate;
    private Button submitTerm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_term);

        selectedTermNumber = -1;
        selectedTermStatus = "In-Progress";
        selectedStartDate = null;
        selectedEndDate = null;

        termTitle = findViewById(R.id.addTermTitle);

        statusSpinner = findViewById(R.id.addTermStatus);
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                                             R.array.termStatus, R.layout.spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        final Calendar startCalendar = Calendar.getInstance();
        final Calendar endCalendar = Calendar.getInstance();



        startText = findViewById(R.id.addTermStart);
        startText.setKeyListener(null);
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
                updateLabel(startText, startCalendar);
                selectedStartDate = startCalendar.getTime();
            }

        };

        startText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(AddTermActivity.this, startDate, startCalendar
                        .get(Calendar.YEAR), startCalendar.get(Calendar.MONTH),
                        startCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });



        endText = findViewById(R.id.addTermEnd);
        endText.setKeyListener(null);
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
                updateLabel(endText, endCalendar);
                selectedEndDate = endCalendar.getTime();
            }

        };

        endText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(AddTermActivity.this, endDate, endCalendar
                        .get(Calendar.YEAR), endCalendar.get(Calendar.MONTH),
                        endCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        submitTerm = findViewById(R.id.addTermSubmit);

        submitTerm.setOnClickListener(new View.OnClickListener() {

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
        ArrayList<Term> termList = pullTermsFromDB();

        if(checkFields(termList)) {
            addTermToDB(selectedTermNumber, selectedStartDate.getTime(), selectedEndDate.getTime(), (String) statusSpinner.getSelectedItem());
            finish();
        }
    }

    public void termNumberPickerShow(View view) {

        final Dialog d = new Dialog(AddTermActivity.this);
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
                termTitle.setText("Term " + String.valueOf(np.getValue()));
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

    private ArrayList<Term> pullTermsFromDB() {
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
                null,                       // The columns for the WHERE clause
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

        return termList;
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

        if(selectedStartDate.getTime() >= selectedEndDate.getTime()) {
            return false;
        }

        return true;
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

    private void addTermToDB(int termNumber, long start, long end, String status) {

        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DBContract.TermTable.COLUMN_NUMBER, termNumber);
        cv.put(DBContract.TermTable.COLUMN_STATUS, status);
        cv.put(DBContract.TermTable.COLUMN_START, start);
        cv.put(DBContract.TermTable.COLUMN_END, end);

        database.insert("Term", null, cv);
    }

}
