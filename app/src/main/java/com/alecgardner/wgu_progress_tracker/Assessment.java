package com.alecgardner.wgu_progress_tracker;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ObservableLong;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Assessment {
    public final ObservableInt assessmentID = new ObservableInt();
    public final ObservableField<String> assessmentType = new ObservableField<>();
    public final ObservableField<String> assessmentTitle = new ObservableField<>();
    public final ObservableLong assessmentDueDate = new ObservableLong();
    public final ObservableField<String> assessmentStatus = new ObservableField<>();
    public final ObservableInt assessmentCourse = new ObservableInt();
    public final ObservableInt assessmentHomeColor = new ObservableInt();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");

    public String convertDueToString() {
        Date dueDate = new Date(assessmentDueDate.get());
        return dateFormat.format(dueDate);
    }

}
