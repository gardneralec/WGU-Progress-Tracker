package com.alecgardner.wgu_progress_tracker;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.databinding.DataBindingUtil;
import android.view.View;

import com.alecgardner.wgu_progress_tracker.databinding.ActivityHomeBinding;
import com.alecgardner.wgu_progress_tracker.databinding.TermRowBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private ArrayList<Term> terms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        terms = new ArrayList<>();
        this.generateTermsFromDB();
        for(Term term:terms) {
            View child = getLayoutInflater().inflate(R.layout.term_row, null);
            final TermRowBinding termBinding = DataBindingUtil.bind(child);
            termBinding.setTerm(term);
            binding.dataRootView.addView(child);
        }
    }

    private void generateTermsFromDB() {

        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        String[] projection = {
                DBContract.TermTable._ID,
                DBContract.TermTable.COLUMN_NUMBER,
                DBContract.TermTable.COLUMN_START,
                DBContract.TermTable.COLUMN_END,
                DBContract.TermTable.COLUMN_STATUS
        };

        Cursor cursor = database.query(
                DBContract.TermTable.TABLE_NAME,     // The table to query
                projection,                          // The columns to return
                null,                       // The columns for the WHERE clause
                null,                    // The values for the WHERE clause
                null,                        // don't group the rows
                null,                         // don't filter by row groups
                null                         // don't sort
        );

        if(cursor.getCount() != 0) {

            cursor.moveToFirst();

            do {
                Term term = new Term();
                term.termID.set(cursor.getInt(0));
                System.out.println(term.termID.get());
                term.termNumber.set(cursor.getInt(1));
                term.termStart.set(cursor.getLong(2));
                term.termEnd.set(cursor.getLong(3));
                System.out.println(term.termStart.get());
                term.termStatus.set(cursor.getString(4));
                terms.add(term);
            } while (cursor.moveToNext());
        }
    }
}
