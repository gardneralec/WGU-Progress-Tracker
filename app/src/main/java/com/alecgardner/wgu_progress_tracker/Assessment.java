package com.alecgardner.wgu_progress_tracker;

import android.databinding.ObservableField;

public class Assessment {
    public final ObservableField<Integer> assessmentType = new ObservableField<>();
    public final ObservableField<String> assessmentTitle = new ObservableField<>();
    public final ObservableField<Long> assessmentDueDate = new ObservableField<>();
    public final ObservableField<Integer> assessmentStatus = new ObservableField<>();

}
