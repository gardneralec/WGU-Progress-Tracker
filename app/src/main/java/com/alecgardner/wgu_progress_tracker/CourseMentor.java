package com.alecgardner.wgu_progress_tracker;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;

public class CourseMentor {

    public final ObservableInt cmID= new ObservableInt();
    public final ObservableInt courseID = new ObservableInt();
    public final ObservableField<String> cmName = new ObservableField<>();
    public final ObservableField<String> cmPhone = new ObservableField<>();
    public final ObservableField<String> cmEmail = new ObservableField<>();

}
