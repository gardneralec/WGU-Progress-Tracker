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

public class AddCMActivity extends AppCompatActivity {

    private EditText cmName;
    private EditText cmPhone;
    private EditText cmEmail;
    private int selectedCourseID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cm);

        Intent intent = getIntent();
        selectedCourseID = intent.getIntExtra("COURSE_ID", 0);

        cmName = findViewById(R.id.addCMName);

        cmPhone = findViewById(R.id.addCMPhone);

        cmEmail = findViewById(R.id.addCMEmail);

        Button submitTerm = findViewById(R.id.addCMSubmit);

        submitTerm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onSubmitClickHandler();
            }
        });
    }

    public void onSubmitClickHandler() {

        if(checkRequiredFieldsForNull()) {
            addCMToDB(selectedCourseID, cmName.getText().toString(), cmPhone.getText().toString(), cmEmail.getText().toString());
            finish();
        } else {
            Context context = getApplicationContext();
            CharSequence text = "Please enter all contact information";
            int duration = Toast.LENGTH_SHORT;
            Toast missingFields = Toast.makeText(context, text, duration);
            missingFields.show();
        }
    }

    private boolean checkRequiredFieldsForNull() {
        boolean allFieldsFilled = false;

        if((cmName.getText().toString().length() > 0) && (cmPhone.getText().toString().length() > 0) && (cmEmail.getText().toString().length() > 0)) {
            allFieldsFilled = true;
        }

        return allFieldsFilled;
    }

    private void addCMToDB(int courseID, String name, String phone, String email) {

        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DBContract.CMTable.COLUMN_COURSE, courseID);
        cv.put(DBContract.CMTable.COLUMN_NAME, name);
        cv.put(DBContract.CMTable.COLUMN_PHONE, phone);
        cv.put(DBContract.CMTable.COLUMN_EMAIL, email);

        database.insert("CM", null, cv);

        database.close();
    }

}
