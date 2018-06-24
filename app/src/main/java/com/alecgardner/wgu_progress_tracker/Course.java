package com.alecgardner.wgu_progress_tracker;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ObservableLong;
import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Course {
    public final ObservableInt courseID = new ObservableInt();
    public final ObservableField<String> courseTitle = new ObservableField<>();
    public final ObservableLong courseStart = new ObservableLong();
    public final ObservableLong courseEnd = new ObservableLong();
    public final ObservableInt courseTermID = new ObservableInt();
    public final ObservableField<String> courseStatus = new ObservableField<>();
    public final ObservableArrayList<CourseMentor> associatedMentors = new ObservableArrayList();
    public final ObservableArrayList<Assessment> associatedAssessments = new ObservableArrayList();
    public final ObservableField<String> courseNotes = new ObservableField<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
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
