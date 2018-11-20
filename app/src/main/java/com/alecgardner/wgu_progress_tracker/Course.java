package com.alecgardner.wgu_progress_tracker;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ObservableLong;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Course {
    public final ObservableInt courseID = new ObservableInt();
    public final ObservableField<String> courseTitle = new ObservableField<>();
    public final ObservableLong courseStart = new ObservableLong();
    public final ObservableLong courseEnd = new ObservableLong();
    public final ObservableInt courseTermID = new ObservableInt();
    public final ObservableField<String> courseStatus = new ObservableField<>();
    public final ArrayList<Assessment> associatedAssessments = new ArrayList<>();
    public final ObservableField<String> courseNotes = new ObservableField<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
    public final ObservableInt courseHomeColor = new ObservableInt();

    public String convertStartToString() {
        Date startDate = new Date(courseStart.get());
        return dateFormat.format(startDate);
    }

    public String convertEndToString() {
        Date endDate = new Date(courseEnd.get());
        return dateFormat.format(endDate);
    }

}
