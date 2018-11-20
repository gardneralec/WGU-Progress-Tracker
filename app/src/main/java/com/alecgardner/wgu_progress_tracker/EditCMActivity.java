package com.alecgardner.wgu_progress_tracker;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditCMActivity extends AppCompatActivity {

    private EditText cmName;
    private EditText cmPhone;
    private EditText cmEmail;
    private int selectedCMID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cm);

        Intent intent = getIntent();
        selectedCMID = intent.getIntExtra("CM_ID", 0);

        cmName = findViewById(R.id.editCMName);

        cmPhone = findViewById(R.id.editCMPhone);

        cmEmail = findViewById(R.id.editCMEmail);

        populateFields(findCMInDB(selectedCMID));

        Button deleteCM = findViewById(R.id.editCMDelete);

        Button submitCM = findViewById(R.id.editCMSubmit);

        deleteCM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCMinDB(selectedCMID);
            }
        });

        submitCM.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onSubmitClickHandler(selectedCMID);
            }
        });
    }

    public void onSubmitClickHandler(int cmID) {

        if(checkRequiredFieldsForNull()) {
            editCMInDB(cmID, cmName.getText().toString(), cmPhone.getText().toString(), cmEmail.getText().toString());
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

    private void editCMInDB(int cmID, String name, String phone, String email) {

        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DBContract.CMTable.COLUMN_NAME, name);
        cv.put(DBContract.CMTable.COLUMN_PHONE, phone);
        cv.put(DBContract.CMTable.COLUMN_EMAIL, email);

        database.update("CM", cv, "_ID = " + cmID, null);

        database.close();
    }

    private void deleteCMinDB(final int cmID) {
        final AlertDialog alertDialog = new AlertDialog.Builder(EditCMActivity.this).create();
        alertDialog.setTitle("Delete Course Mentor");
        alertDialog.setMessage("Are you sure you want to delete this course mentor?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase database = new DBSQLiteHelper(getApplicationContext()).getReadableDatabase();

                        database.delete("CM", "_ID = " + cmID, null);

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

    private void populateFields(CourseMentor cm) {
        cmName.setText(cm.cmName.get());
        cmEmail.setText(cm.cmEmail.get());
        cmPhone.setText(cm.cmPhone.get());
    }

    private CourseMentor findCMInDB(int cmID) {
        String[] selectionArgs = {
                Integer.toString(cmID)
        };

        String selection = "_ID = ?";

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
                selection,
                selectionArgs,
                null,
                null,
                null,
                null
        );

        CourseMentor cm = new CourseMentor();

        if(cmCursor.getCount() > 0) {
            cmCursor.moveToFirst();

            cm.cmID.set(cmCursor.getInt(0));
            cm.courseID.set(cmCursor.getInt(1));
            cm.cmName.set(cmCursor.getString(2));
            cm.cmEmail.set(cmCursor.getString(3));
            cm.cmPhone.set(cmCursor.getString(4));
        }

        cmCursor.close();

        database.close();

        return cm;
    }

}
