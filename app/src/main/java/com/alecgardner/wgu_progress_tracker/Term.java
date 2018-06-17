package com.alecgardner.wgu_progress_tracker;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;

public class Term {
    public final ObservableField<Integer> termNumber = new ObservableField<>();
    public final ObservableField<Long> termStart = new ObservableField<>();
    public final ObservableField<Long> termEnd = new ObservableField<>();
    public final ObservableArrayList<Course> associatedCourses = new ObservableArrayList();
    public final ObservableField<String> termStatus = new ObservableField<>();
}
