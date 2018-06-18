package com.alecgardner.wgu_progress_tracker;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Term {
    public final ObservableField<Integer> termID = new ObservableField<>();
    public final ObservableField<Integer> termNumber = new ObservableField<>();
    public final ObservableField<Long> termStart = new ObservableField<>();
    public final ObservableField<Long> termEnd = new ObservableField<>();
    public final ObservableArrayList<Course> associatedCourses = new ObservableArrayList();
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
}
