package com.alecgardner.wgu_progress_tracker;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    private ArrayList<Term> terms;
    private ArrayList<Course> courses;
    private ArrayList<Assessment> assessments;
    private float scale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        scale = getResources().getDisplayMetrics().density;
        terms = new ArrayList<>();
        courses = new ArrayList<>();
        assessments = new ArrayList<>();
        this.generateTermsFromDB();
        LinearLayout rootView = findViewById(R.id.rootView);
        rootView.addView(generateRows());

    }

    private int convertToDP(int pixels) {
        return (int) (pixels * scale + 0.5f);
    }

    private void generateTermsFromDB() {

        SQLiteDatabase database = new DBSQLiteHelper(this).getReadableDatabase();

        String[] termProjection = {
                DBContract.TermTable._ID,
                DBContract.TermTable.COLUMN_NUMBER,
                DBContract.TermTable.COLUMN_START,
                DBContract.TermTable.COLUMN_END,
                DBContract.TermTable.COLUMN_STATUS
        };

        Cursor termCursor = database.query(
                DBContract.TermTable.TABLE_NAME,     // The table to query
                termProjection,                          // The columns to return
                null,                       // The columns for the WHERE clause
                null,                    // The values for the WHERE clause
                null,                        // don't group the rows
                null,                         // don't filter by row groups
                null                         // don't sort
        );

        if(termCursor.getCount() != 0) {

            termCursor.moveToFirst();

            do {
                Term term = new Term();
                term.termID.set(termCursor.getInt(0));
                term.termNumber.set(termCursor.getInt(1));
                term.termStart.set(termCursor.getLong(2));
                term.termEnd.set(termCursor.getLong(3));
                term.termStatus.set(termCursor.getString(4));
                terms.add(term);
            } while (termCursor.moveToNext());
        }

        termCursor.close();

        String[] courseProjection = {
                DBContract.CourseTable._ID,
                DBContract.CourseTable.COLUMN_TITLE,
                DBContract.CourseTable.COLUMN_START,
                DBContract.CourseTable.COLUMN_END,
                DBContract.CourseTable.COLUMN_TERM,
                DBContract.CourseTable.COLUMN_STATUS,
                DBContract.CourseTable.COLUMN_NOTES
        };

        Cursor courseCursor = database.query(
            DBContract.CourseTable.TABLE_NAME,
            courseProjection,
            null,
            null,
            null,
            null,
            null,
            null
        );

        if(courseCursor.getCount() != 0) {

            courseCursor.moveToFirst();

            do {
                Course course = new Course();
                course.courseID.set(courseCursor.getInt(0));
                course.courseTitle.set(courseCursor.getString(1));
                course.courseStart.set(courseCursor.getLong(2));
                course.courseEnd.set(courseCursor.getLong(3));
                course.courseTermID.set(courseCursor.getInt(4));
                course.courseStatus.set(courseCursor.getString(5));
                course.courseNotes.set(courseCursor.getString(6));


                switch (course.courseStatus.get()) {
                    case "CO":
                        course.courseHomeColor.set(R.color.lightGreen);
                        break;

                    case "DR":
                        course.courseHomeColor.set(R.color.lightRed);
                        break;

                    case "IP":
                        course.courseHomeColor.set(R.color.white);
                        break;

                    default:
                        course.courseHomeColor.set(R.color.gray);
                        break;
                }


                courses.add(course);
            } while (courseCursor.moveToNext());
        }

        courseCursor.close();

        for(Course course:courses) {
            for(Term term:terms) {
                if(course.courseTermID.get() == term.termID.get()) {
                    term.associatedCourses.add(course);
                    break;
                }
            }
        }

        String[] assessmentProjection = {
                DBContract.AssessmentTable._ID,
                DBContract.AssessmentTable.COLUMN_TITLE,
                DBContract.AssessmentTable.COLUMN_TYPE,
                DBContract.AssessmentTable.COLUMN_DUE,
                DBContract.AssessmentTable.COLUMN_STATUS,
                DBContract.AssessmentTable.COLUMN_COURSE
        };

        Cursor assessmentCursor = database.query(
                DBContract.AssessmentTable.TABLE_NAME,
                assessmentProjection,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if(assessmentCursor.getCount() != 0) {

            assessmentCursor.moveToFirst();

            do {
                Assessment assessment = new Assessment();
                assessment.assessmentID.set(assessmentCursor.getInt(0));
                assessment.assessmentTitle.set(assessmentCursor.getString(1));
                assessment.assessmentType.set(assessmentCursor.getString(2));
                assessment.assessmentDueDate.set(assessmentCursor.getLong(3));
                assessment.assessmentStatus.set(assessmentCursor.getString(4));
                assessment.assessmentCourse.set(assessmentCursor.getInt(5));


                switch (assessment.assessmentStatus.get()) {
                    case "CO":
                        assessment.assessmentHomeColor.set(R.color.lightGreen);
                        break;
                        
                    default:
                        assessment.assessmentHomeColor.set(R.color.gray);
                        break;
                }

                assessments.add(assessment);
            } while (assessmentCursor.moveToNext());
        }

        assessmentCursor.close();

        for(Assessment assessment:assessments) {
            for(Course course:courses) {
                if(assessment.assessmentCourse.get() == course.courseID.get()) {
                    course.associatedAssessments.add(assessment);
                    break;
                }
            }
        }

    }

    private LinearLayout generateRows() {
        LinearLayout termRoot = new LinearLayout(this); // root view for all rows
        termRoot.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        termRoot.setOrientation(LinearLayout.VERTICAL);

        for(Term term:this.terms) {
            LinearLayout termRowRoot = new LinearLayout(this); // root view for individual term and children
            termRowRoot.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            termRowRoot.setOrientation(LinearLayout.VERTICAL);

            final LinearLayout termChildren = new LinearLayout(this); // root view for child courses of term
            LinearLayout.LayoutParams termChildrenLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            termChildren.setLayoutParams(termChildrenLP);
            termChildren.setOrientation(LinearLayout.VERTICAL);
            termChildren.setVisibility(View.GONE);

            LinearLayout termRow = new LinearLayout(this); // view for items within term row
            termRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams termRowLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            termRowLP.setMargins(0, 0, 0, convertToDP(1));
            termRow.setLayoutParams(termRowLP);
            termRow.setPadding(convertToDP(16), convertToDP(8), 0, convertToDP(8));
            termRow.setBackgroundColor(getResources().getColor(R.color.blue));

            LinearLayout.LayoutParams wrapContentLP2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            wrapContentLP2.weight = 2;
            wrapContentLP2.gravity = Gravity.CENTER_VERTICAL;

            LinearLayout.LayoutParams wrapContentLP1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            wrapContentLP1.weight = 1;
            wrapContentLP1.gravity = Gravity.CENTER_VERTICAL;

            LinearLayout.LayoutParams buttonLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            buttonLP.weight = 1;

            TextView termNumber = new TextView(this);
            termNumber.setLayoutParams(wrapContentLP2);
            termNumber.setTextColor(getResources().getColor(R.color.white));
            String termNumberText = "Term " + term.termNumber.get();
            termNumber.setText(termNumberText);
            termNumber.setGravity(Gravity.START);
            termNumber.setTextSize(18);
            termRow.addView(termNumber);

            TextView termStart = new TextView(this);
            termStart.setLayoutParams(wrapContentLP1);
            termStart.setTextColor(getResources().getColor(R.color.white));
            termStart.setGravity(Gravity.END);
            termStart.setText(term.convertStartToString());
            termStart.setTextSize(18);
            termRow.addView(termStart);

            TextView termEnd = new TextView(this);
            termEnd.setLayoutParams(wrapContentLP1);
            termEnd.setTextColor(getResources().getColor(R.color.white));
            termEnd.setGravity(Gravity.END);
            termEnd.setText(term.convertEndToString());
            termEnd.setTextSize(18);
            termRow.addView(termEnd);

            final Button termExpand = new Button(this);
            termExpand.setLayoutParams(buttonLP);
            termExpand.setGravity(Gravity.CENTER);
            termExpand.setTextColor(getResources().getColor(R.color.white));
            termExpand.setBackgroundColor(Color.TRANSPARENT);
            termExpand.setText(getResources().getText(R.string.maximize));
            termExpand.setTextSize(24);
            termExpand.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(termExpand.getText().equals("+")) {
                        termExpand.setText("-");
                        termChildren.setVisibility(View.VISIBLE);
                    } else {
                        termExpand.setText("+");
                        termChildren.setVisibility(View.GONE);
                    }
                }
            });
            termRow.addView(termExpand);

            termRowRoot.addView(termRow);

            // Descriptor row for courses associated with each term
            LinearLayout courseDescriptorRow = new LinearLayout(this); // view for course descriptor of this term
            courseDescriptorRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams courseDescriptorRowLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            courseDescriptorRowLP.setMargins(0, 0, 0, convertToDP(1));
            courseDescriptorRow.setLayoutParams(courseDescriptorRowLP);
            courseDescriptorRow.setPadding(convertToDP(16), convertToDP(8), 0, convertToDP(8));
            courseDescriptorRow.setBackgroundColor(getResources().getColor(R.color.tan));

            TextView courseDescriptorText = new TextView(this);
            courseDescriptorText.setLayoutParams(wrapContentLP1);
            courseDescriptorText.setTextColor(getResources().getColor(R.color.textDark));
            courseDescriptorText.setGravity(Gravity.START);
            String courseDescriptorTextContent = "Term " + term.termNumber.get() + " Courses";
            courseDescriptorText.setText(courseDescriptorTextContent);
            courseDescriptorText.setTextSize(18);
            courseDescriptorRow.addView(courseDescriptorText);

            termChildren.addView(courseDescriptorRow);

            // Add courses underneath term row created above
            for(Course course:term.associatedCourses) { // Create course rows
                LinearLayout courseRowRoot = new LinearLayout(this); // root view for course row and associated assessments
                courseRowRoot.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                courseRowRoot.setOrientation(LinearLayout.VERTICAL);

                final LinearLayout courseChildren = new LinearLayout(this); // root view for child assessments of course
                LinearLayout.LayoutParams courseChildrenLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                courseChildren.setLayoutParams(courseChildrenLP);
                courseChildren.setOrientation(LinearLayout.VERTICAL);
                courseChildren.setVisibility(View.GONE);

                LinearLayout courseRow = new LinearLayout(this); // root view for information about course
                courseRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams courseRowLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                courseRowLP.setMargins(0, 0, 0, convertToDP(1));
                courseRow.setLayoutParams(courseRowLP);
                courseRow.setPadding(convertToDP(16), convertToDP(8), 0, convertToDP(8));
                courseRow.setBackgroundColor(getResources().getColor(course.courseHomeColor.get()));

                int textColor;

                if(course.courseHomeColor.get() == R.color.white) {
                    textColor = getResources().getColor(R.color.textDark);
                } else if(course.courseHomeColor.get() == R.color.gray) {
                    textColor = getResources().getColor(R.color.textDark);
                } else {
                    textColor = getResources().getColor(R.color.white);
                }

                TextView courseTitle = new TextView(this);
                courseTitle.setLayoutParams(wrapContentLP2);
                courseTitle.setTextColor(textColor);
                courseTitle.setText(course.courseTitle.get());
                courseTitle.setGravity(Gravity.START);
                courseTitle.setTextSize(18);
                courseRow.addView(courseTitle);

                TextView courseStart = new TextView(this);
                courseStart.setLayoutParams(wrapContentLP1);
                courseStart.setTextColor(textColor);
                courseStart.setGravity(Gravity.END);
                courseStart.setText(course.convertStartToString());
                courseStart.setTextSize(18);
                courseRow.addView(courseStart);

                TextView courseEnd = new TextView(this);
                courseEnd.setLayoutParams(wrapContentLP1);
                courseEnd.setTextColor(textColor);
                courseEnd.setGravity(Gravity.END);
                courseEnd.setText(course.convertEndToString());
                courseEnd.setTextSize(18);
                courseRow.addView(courseEnd);

                final Button courseExpand = new Button(this);
                courseExpand.setLayoutParams(buttonLP);
                courseExpand.setGravity(Gravity.CENTER);
                courseExpand.setTextColor(textColor);
                courseExpand.setBackgroundColor(Color.TRANSPARENT);
                courseExpand.setText(getResources().getText(R.string.maximize));
                courseExpand.setTextSize(24);
                courseExpand.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(courseExpand.getText().equals("+")) {
                            courseExpand.setText("-");
                            courseChildren.setVisibility(View.VISIBLE);
                        } else {
                            courseExpand.setText("+");
                            courseChildren.setVisibility(View.GONE);
                        }
                    }
                });
                courseRow.addView(courseExpand);

                courseRowRoot.addView(courseRow);

                // Descriptor row for assessments associated with each course
                LinearLayout assessmentDescriptorRow = new LinearLayout(this); // view for assessment descriptor row
                assessmentDescriptorRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams assessmentDescriptorRowLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                assessmentDescriptorRowLP.setMargins(0, 0, 0, convertToDP(1));
                assessmentDescriptorRow.setLayoutParams(assessmentDescriptorRowLP);
                assessmentDescriptorRow.setPadding(convertToDP(16), convertToDP(8), 0, convertToDP(8));
                assessmentDescriptorRow.setBackgroundColor(getResources().getColor(R.color.tan));

                TextView assessmentDescriptorText = new TextView(this);
                assessmentDescriptorText.setLayoutParams(wrapContentLP1);
                assessmentDescriptorText.setTextColor(getResources().getColor(R.color.textDark));
                assessmentDescriptorText.setGravity(Gravity.START);
                String assessmentDescriptorTextContent = course.courseTitle.get() + " Assessments";
                assessmentDescriptorText.setText(assessmentDescriptorTextContent);
                assessmentDescriptorText.setTextSize(18);
                assessmentDescriptorRow.addView(assessmentDescriptorText);

                courseChildren.addView(assessmentDescriptorRow); // Add assessment descriptor row to top of assessment holder

                // Add assessments underneath each course created above
                for(Assessment assessment:course.associatedAssessments) { // Create assessment rows
                    LinearLayout assessmentRowRoot = new LinearLayout(this); // root view for assessment row and associated assessments
                    assessmentRowRoot.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    assessmentRowRoot.setOrientation(LinearLayout.VERTICAL);

                    LinearLayout assessmentRow = new LinearLayout(this); // root view for information about assessment
                    assessmentRow.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams assessmentRowLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    assessmentRowLP.setMargins(0, 0, 0, convertToDP(1));
                    assessmentRow.setLayoutParams(assessmentRowLP);
                    assessmentRow.setPadding(convertToDP(16), convertToDP(8), 0, convertToDP(8));
                    assessmentRow.setBackgroundColor(getResources().getColor(assessment.assessmentHomeColor.get()));

                    int assessmentTextColor;

                    if (assessment.assessmentHomeColor.get() == R.color.lightGreen) {
                        assessmentTextColor = getResources().getColor(R.color.white);
                    } else {
                        assessmentTextColor = getResources().getColor(R.color.textDark);
                    }

                    TextView assessmentTitle = new TextView(this);
                    assessmentTitle.setLayoutParams(wrapContentLP2);
                    assessmentTitle.setTextColor(assessmentTextColor);
                    assessmentTitle.setText(assessment.assessmentTitle.get());
                    assessmentTitle.setGravity(Gravity.START);
                    assessmentTitle.setTextSize(18);
                    assessmentRow.addView(assessmentTitle);

                    TextView assessmentType = new TextView(this);
                    assessmentType.setLayoutParams(wrapContentLP1);
                    assessmentType.setTextColor(assessmentTextColor);
                    assessmentType.setGravity(Gravity.END);
                    assessmentType.setText(assessment.assessmentType.get());
                    assessmentType.setTextSize(18);
                    assessmentRow.addView(assessmentType);

                    TextView assessmentDue = new TextView(this);
                    assessmentDue.setLayoutParams(wrapContentLP1);
                    assessmentDue.setTextColor(assessmentTextColor);
                    assessmentDue.setGravity(Gravity.END);
                    assessmentDue.setText(assessment.convertDueToString());
                    assessmentDue.setTextSize(18);
                    assessmentRow.addView(assessmentDue);

                    Button assessmentExpand = new Button(this);
                    assessmentExpand.setLayoutParams(buttonLP);
                    assessmentExpand.setGravity(Gravity.CENTER);
                    assessmentExpand.setTextColor(assessmentTextColor);
                    assessmentExpand.setBackgroundColor(Color.TRANSPARENT);
                    assessmentExpand.setText("");
                    assessmentExpand.setTextSize(24);
                    assessmentRow.addView(assessmentExpand);

                    assessmentRowRoot.addView(assessmentRow); // Adds assessment information row to main assessment root view

                    courseChildren.addView(assessmentRowRoot); //  Adds assessment root view to course's assessment holder
                } // End create assessment rows

                final LinearLayout addAssessmentRow = new LinearLayout(this); // view to display add assessment row for each course
                addAssessmentRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams addAssessmentRowLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                addAssessmentRowLP.setMargins(0, 0, 0, convertToDP(1));
                addAssessmentRow.setLayoutParams(addAssessmentRowLP);
                addAssessmentRow.setPadding(convertToDP(16), convertToDP(16), convertToDP(16), convertToDP(16));
                addAssessmentRow.setBackgroundColor(getResources().getColor(R.color.tan));

                TextView addAssessmentRowText = new TextView(this);
                addAssessmentRowText.setLayoutParams(wrapContentLP1);
                addAssessmentRowText.setTextColor(getResources().getColor(R.color.textDark));
                addAssessmentRowText.setGravity(Gravity.CENTER);
                String addAssessmentRowTextContent = "+  Add New Assessment For " + course.courseTitle.get() + "  +";
                addAssessmentRowText.setText(addAssessmentRowTextContent);
                addAssessmentRowText.setTextSize(18);
                addAssessmentRow.addView(addAssessmentRowText);

                courseChildren.addView(addAssessmentRow); // Attach add assessment row to the end of this course's assessment list

                courseRowRoot.addView(courseChildren); // Adds assessment list to main course root view

                termChildren.addView(courseRowRoot); // Add course information and assessment list to term's course holder


            } // End create course rows

            final LinearLayout addCourseRow = new LinearLayout(this); // view to display add course row for each term
            addCourseRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams addCourseRowLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            addCourseRowLP.setMargins(0, 0, 0, convertToDP(1));
            addCourseRow.setLayoutParams(addCourseRowLP);
            addCourseRow.setPadding(convertToDP(16), convertToDP(16), convertToDP(16), convertToDP(16));
            addCourseRow.setBackgroundColor(getResources().getColor(R.color.tan));

            TextView addCourseRowText = new TextView(this);
            addCourseRowText.setLayoutParams(wrapContentLP1);
            addCourseRowText.setTextColor(getResources().getColor(R.color.textDark));
            addCourseRowText.setGravity(Gravity.CENTER);
            String addCourseRowTextContent = "+  Add New Course For Term " + term.termNumber.get() + "  +";
            addCourseRowText.setText(addCourseRowTextContent);
            addCourseRowText.setTextSize(18);
            addCourseRow.addView(addCourseRowText);

            termChildren.addView(addCourseRow); // Attach add course row to the end of this term's course list

            termRowRoot.addView(termChildren); // Adds course list to main term root view

            termRoot.addView(termRowRoot); // Add term information and course list to main term holder

        } // End create term rows

        final LinearLayout addTermRow = new LinearLayout(this); // view to display add term to bottom of term list
        addTermRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams addTermRowLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        addTermRowLP.setMargins(0, 0, 0, convertToDP(1));
        addTermRow.setLayoutParams(addTermRowLP);
        addTermRow.setPadding(convertToDP(16), convertToDP(16), convertToDP(16), convertToDP(16));
        addTermRow.setBackgroundColor(getResources().getColor(R.color.tan));
        addTermRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddTermActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout.LayoutParams wrapContentLPAddTerm = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        wrapContentLPAddTerm.weight = 1;
        wrapContentLPAddTerm.gravity = Gravity.CENTER_VERTICAL;

        TextView addTermRowText = new TextView(this);
        addTermRowText.setLayoutParams(wrapContentLPAddTerm);
        addTermRowText.setTextColor(getResources().getColor(R.color.textDark));
        addTermRowText.setGravity(Gravity.CENTER);
        String addTermRowTextContent = "+  Add New Term  +";
        addTermRowText.setText(addTermRowTextContent);
        addTermRowText.setTextSize(18);
        addTermRow.addView(addTermRowText);

        termRoot.addView(addTermRow); // Attach add term row to bottom of term list

        return termRoot; // Return root view containing all rows of terms, courses, and assessments
    }
}
