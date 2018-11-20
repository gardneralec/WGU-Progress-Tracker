package com.alecgardner.wgu_progress_tracker;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ObservableLong;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Term {
    public final ObservableInt termID = new ObservableInt();
    public final ObservableInt termNumber = new ObservableInt();
    public final ObservableLong termStart = new ObservableLong();
    public final ObservableLong termEnd = new ObservableLong();
    public final ArrayList<Course> associatedCourses = new ArrayList<>();
    public final ObservableField<String> termStatus = new ObservableField<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");

    public String convertStartToString() {
        Date startDate = new Date(termStart.get());
        return dateFormat.format(startDate);
    }

    public String convertEndToString() {
        Date endDate = new Date(termEnd.get());
        return dateFormat.format(endDate);
    }

    public String getStringTermNumber() {
        return "Term " + termNumber.get();
    }
}
