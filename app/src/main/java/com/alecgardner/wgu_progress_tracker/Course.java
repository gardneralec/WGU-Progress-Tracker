package com.alecgardner.wgu_progress_tracker;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;

public class Course {
    public final ObservableField<String> coursetitle = new ObservableField<>();
    public final ObservableField<Long> courseStart = new ObservableField<>();
    public final ObservableField<Long> courseEnd = new ObservableField<>();
    public final ObservableField<String> courseStatus = new ObservableField<>();
    public final ObservableArrayList<CourseMentor> associatedMentors = new ObservableArrayList();
    public final ObservableArrayList<Assessment> associatedAssessments = new ObservableArrayList();
    public final ObservableField<String> courseNotes = new ObservableField<>();

}
