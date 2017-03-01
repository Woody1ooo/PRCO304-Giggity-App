package com.liamd.giggity_app;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class VenueUserViewGigDetailsFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener
{
    // Declare visual components
    private EditText mGigNameEditText;
    private Button mUpdateStartDateButton;
    private Button mUpdateFinishDateButton;
    private TextView mStartDateSelectedTextView;
    private TextView mFinishDateSelectedTextView;
    private Button mUpdateStartTimeButton;
    private Button mUpdateFinishTimeButton;
    private TextView mStartTimeSelectedTextView;
    private TextView mFinishTimeSelectedTextView;
    private Button mUpdateGigButton;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Declare existing start date variables
    private String mExistingStartDate;
    private Date mParsedStartDate;
    private int mFormattedStartYear;
    private int mFormattedStartMonth;

    // Declare existing finish date variables to initialise
    private String mExistingFinishDate;
    private Date mParsedFinishDate;
    private int mFormattedFinishYear;
    private int mFormattedFinishMonth;

    // Declare new start date variables
    private Date mUpdatedStartDate;
    private int mUpdatedStartHour;
    private int mUpdatedStartMinute;
    private int mUpdatedStartYear;
    private int mUpdatedStartMonth;
    private int mUpdatedStartDay;

    // Declare new finish date variables
    private Date mUpdatedFinishDate;
    private int mUpdatedFinishHour;
    private int mUpdatedFinishMinute;
    private int mUpdatedFinishYear;
    private int mUpdatedFinishMonth;
    private int mUpdatedFinishDay;

    // Declare general variables
    private String mGigID;
    private String mVenueId;
    private List<Gig> mListOfVenueGigs = new ArrayList<>();
    private List<Date> mListOfGigDates = new ArrayList<>();
    private Boolean isStartDate;

    // These variables determine which elements of the gig have been edited
    private Boolean isStartDateEdited = false;
    private Boolean isFinishDateEdited = false;
    private Boolean isStartTimeEdited = false;
    private Boolean isFinishTimeEdited = false;

    Date currentlySetStartDate;
    Date currentlySetEndDate;


    public VenueUserViewGigDetailsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView =  inflater.inflate(R.layout.venue_user_fragment_view_gig_details, container, false);

        // Initialise visual components
        mGigNameEditText = (EditText) fragmentView.findViewById(R.id.gigNameEditText);

        mUpdateStartDateButton = (Button) fragmentView.findViewById(R.id.UpdateStartDatePickerButton);
        mUpdateFinishDateButton = (Button) fragmentView.findViewById(R.id.UpdateFinishDatePickerButton);

        mStartDateSelectedTextView = (TextView) fragmentView.findViewById(R.id.StartDateSelectedLabel);
        mFinishDateSelectedTextView = (TextView) fragmentView.findViewById(R.id.FinishDateSelectedLabel);

        mUpdateStartTimeButton = (Button) fragmentView.findViewById(R.id.UpdateStartTimePickerButton);
        mUpdateFinishTimeButton = (Button) fragmentView.findViewById(R.id.UpdateFinishTimePickerButton);

        mStartTimeSelectedTextView = (TextView) fragmentView.findViewById(R.id.StartTimeSelectedLabel);
        mFinishTimeSelectedTextView = (TextView) fragmentView.findViewById(R.id.FinishTimeSelectedLabel);

        mUpdateGigButton = (Button) fragmentView.findViewById(R.id.UpdateGigButton);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Call the populate fields method to pull through the data of the gig to
        // allow it to be edited
        PopulateFields();

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // This obtains the venue ID from the previous fragment
                mVenueId = getArguments().getString("GigVenueID");

                // Each gig is then iterated through and added to an
                // array list of gigs (mListOfVenueGigs)
                Iterable<DataSnapshot> children = dataSnapshot.child("Gigs/").getChildren();

                for (DataSnapshot child : children)
                {
                    Gig gig;
                    gig = child.getValue(Gig.class);
                    mListOfVenueGigs.add(gig);
                }

                mUpdateStartDateButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        // When the button to select a start date is clicked,
                        // the isStartDate variable is set to true, and the method to display
                        // the calendar is called
                        isStartDate = true;
                        StartDateShowCalendar();
                    }
                });

                mUpdateFinishDateButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        // When the button to select a start date is clicked,
                        // the isStartDate variable is set to false, and the method to display
                        // the calendar is called
                        isStartDate = false;
                        FinishDateShowCalendar();
                    }
                });

                mUpdateStartTimeButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        // When the select start time button is clicked, the method to display
                        // the time picker is called
                        StartTimeShowTimePicker();
                    }
                });

                mUpdateFinishTimeButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        // When the select finish time button is clicked, the method to display
                        // the time picker is called
                        FinishTimeShowTimePicker();
                    }
                });

                mUpdateGigButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        // Once all the other fields have been completed, this method to create
                        // the gig can be called on button click
                        UpdateGig();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        return fragmentView;
    }

    // This method populates the fields with the data from the selected gig for reference
    // when editing
    private void PopulateFields()
    {
        // Populate components and variables with the details of the chosen gig
        mGigID = getArguments().getString("GigID");
        mExistingStartDate = getArguments().getString("GigStartDate");
        mExistingFinishDate = getArguments().getString("GigEndDate");

        // This parses the dates from a String back into a Date object
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy");
        try
        {
            mParsedStartDate = format.parse(mExistingStartDate);
            mParsedFinishDate = format.parse(mExistingFinishDate);

            mFormattedStartYear = mParsedStartDate.getYear() + 1900;
            mFormattedStartMonth = mParsedStartDate.getMonth() + 1;

            mFormattedFinishYear = mParsedFinishDate.getYear() + 1900;
            mFormattedFinishMonth = mParsedFinishDate.getMonth() + 1;
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        mGigNameEditText.setText(getArguments().getString("GigTitle"));

        mStartDateSelectedTextView.setText(mParsedStartDate.getDate() + "/" + mFormattedStartMonth + "/" + mFormattedStartYear);
        mFinishDateSelectedTextView.setText(mParsedFinishDate.getDate() + "/" + mFormattedFinishMonth + "/" + mFormattedFinishYear);

        mStartTimeSelectedTextView.setText(mParsedStartDate.getHours() + ":" + mParsedStartDate.getMinutes());
        mFinishTimeSelectedTextView.setText(mParsedFinishDate.getHours() + ":" + mParsedFinishDate.getMinutes());
    }



    private void StartDateShowCalendar()
    {
        // When the calender is clicked, we iterate through the list of gigObjects populated above
        // to find any gigs for the current venue. If there are any, these days are hidden from the
        // calendar to prevent double bookings
        for(int i = 0; i < mListOfVenueGigs.size(); i++)
        {
            if(mListOfVenueGigs.get(i).getVenueID().equals(mVenueId))
            {
                // These are then added to a separate list of just dates
                mListOfGigDates.add(mListOfVenueGigs.get(i).getStartDate());
            }
        }

        // An array of calendar objects is then created
        Calendar[] mAlreadyBookedDates = new Calendar[mListOfGigDates.size()];

        // This then loops through the gigs in this list and adds their dates
        // to a separate list.
        // The 1900 needs to be added here as the dates are stored differently
        // e.g. 117 rather than 2017
        // This calendar object list then contains all the dates that are
        // already taken and thus should be disabled
        for(int j = 0; j < mListOfGigDates.size(); j++)
        {
            Calendar cal = Calendar.getInstance();
            cal.set(mListOfGigDates.get(j).getYear() + 1900, mListOfGigDates.get(j).getMonth(), mListOfGigDates.get(j).getDate());
            mAlreadyBookedDates[j] = cal;
        }

        // The calendar is then initialised with today's date
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                VenueUserViewGigDetailsFragment.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        // The disabled days are then set against the calendar
        dpd.setDisabledDays(mAlreadyBookedDates);
        dpd.show(getActivity().getFragmentManager(), "DatePickerDialog");

        // By setting the minimum date to today, it prevents gigs being
        // created in the past
        dpd.setMinDate(Calendar.getInstance());
        mListOfGigDates.clear();
    }

    // This simpler version of the method above simply displays the calendar
    // as there is no restriction on the end dates as they will likely only be
    // in the early morning, therefore not interfering with the next day
    private void FinishDateShowCalendar()
    {
        Calendar mNow = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                VenueUserViewGigDetailsFragment.this,
                mNow.get(Calendar.YEAR),
                mNow.get(Calendar.MONTH),
                mNow.get(Calendar.DAY_OF_MONTH)
        );

        dpd.show(getActivity().getFragmentManager(), "DatePickerDialog");
        dpd.setMinDate(Calendar.getInstance());
        mListOfGigDates.clear();
    }

    // This method calls and displays the time picker to allow
    // the gig start time to be selected
    private void StartTimeShowTimePicker()
    {
        Calendar mNow = Calendar.getInstance();
        int mHour = mNow.get(Calendar.HOUR_OF_DAY);
        int mMinute = mNow.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
            {
                // The values of the time attributes is then stored in
                // these global variables so they can be accessed by the CreateGig() method
                mUpdatedStartHour = selectedHour;
                mUpdatedStartMinute = selectedMinute;
                mStartTimeSelectedTextView.setText(selectedHour + ":" + selectedMinute);

                isStartTimeEdited = true;
            }
            // The 'false' value here determines whether the clock is 12 or 24 hours.
            // Currently this is 12 hour only, but this isn't final
        }, mHour, mMinute, false);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    // This method is the same as the above but with the gig finish times
    private void FinishTimeShowTimePicker()
    {
        Calendar mCurrentTime = Calendar.getInstance();
        int mHour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        int mMinute = mCurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
            {
                mUpdatedFinishHour = selectedHour;
                mUpdatedFinishMinute = selectedMinute;
                mFinishTimeSelectedTextView.setText(selectedHour + ":" + selectedMinute);

                isFinishTimeEdited = true;
            }
        }, mHour, mMinute, false);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    // I was unable to implement the onDateSet method within the
    // StartDateShowCalender/FinishDateShowCalendar methods above.
    // I therefore had to have a way to differentiate between the
    // two types as they both use the same date picker dialog.
    // This is the purpose of the isStartDate boolean.

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onDateSet(DatePickerDialog dpd, int year, int monthOfYear, int dayOfMonth)
    {
        // These variables store the dates selected on the picker
        String mYearSelected = Integer.toString(year);
        String mMonthSelected = Integer.toString(monthOfYear + 1);
        String mDaySelected = Integer.toString(dayOfMonth);

        // if the isStartDate boolean is true, this means the
        // the start date button was selected, therefore the
        // relevant variables are populated
        if(isStartDate)
        {
            mUpdatedStartYear = year;
            mUpdatedStartMonth = monthOfYear + 1;
            mUpdatedStartDay = dayOfMonth;

            mStartDateSelectedTextView.setText(mDaySelected + "/" + mMonthSelected + "/" + mYearSelected);

            isStartDateEdited = true;
        }

        // Otherwise it means the finish date button was selected
        else
        {
            mUpdatedFinishYear = year;
            mUpdatedFinishMonth = monthOfYear + 1;
            mUpdatedFinishDay = dayOfMonth;

            mFinishDateSelectedTextView.setText(mDaySelected + "/" + mMonthSelected + "/" + mYearSelected);

            isFinishDateEdited = true;
        }
    }

    // This method takes all the data and creates a gig in the database
    private void UpdateGig()
    {
        try
        {
            // The start date currently held by the selected gig
            currentlySetStartDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                    .parse(mParsedFinishDate.getDate() + "/" + mParsedFinishDate.getMonth()
                            + "/" + mParsedFinishDate.getYear() + " " + mParsedFinishDate.getHours()
                            + ":" + mParsedFinishDate.getMinutes() + ":" + 00.000);

            // The end date currently held by the selected gig
            currentlySetEndDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                    .parse(mParsedStartDate.getDate() + "/" + mParsedStartDate.getMonth()
                            + "/" + mParsedStartDate.getYear() + " " + mParsedStartDate.getHours()
                    + ":" + mParsedStartDate.getMinutes() + ":" + 00.000);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        // If all elements are edited
        if(isStartDateEdited && isFinishDateEdited && isStartTimeEdited && isFinishTimeEdited)
        {
            Date newStartDate;
            Date newEndDate;

            try
            {
                newStartDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                        .parse(mUpdatedStartDay + "/" + mUpdatedStartMonth
                                + "/" + mUpdatedStartYear + " " + mUpdatedStartHour
                                + ":" + mUpdatedStartMinute + ":" + 00.000);

                newEndDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                        .parse(mUpdatedFinishDay + "/" + mUpdatedFinishMonth
                                + "/" + mUpdatedFinishYear + " " + mUpdatedFinishHour
                                + ":" + mUpdatedFinishMinute + ":" + 00.000);

                Gig gig = new Gig();
                gig.setGigId(mGigID);
                gig.setStartDate(newStartDate);
                gig.setEndDate(newEndDate);

                mDatabase.child("Gigs/" + mGigID + "/" + "title").setValue(mGigNameEditText.getText().toString());
                mDatabase.child("Gigs/" + mGigID).setValue(gig);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }

        // In this instance, only the start date and time has been edited
        // Therefore a new start date is created using the variables, and inserted into a gig object
        // This updates the existing gig with a new start date and title, but doesn't touch the finish
        // date or time.
        else if(isStartDateEdited && !isFinishDateEdited && isStartTimeEdited && !isFinishTimeEdited)
        {
            Date newStartDate;

            try
            {
                newStartDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                        .parse(mUpdatedStartDay + "/" + mUpdatedStartMonth
                                + "/" + mUpdatedStartYear + " " + mUpdatedStartHour
                                + ":" + mUpdatedStartMinute + ":" + 00.000);

                Gig gig = new Gig();
                gig.setGigId(mGigID);
                gig.setStartDate(newStartDate);

                mDatabase.child("Gigs/" + mGigID + "/" + "title").setValue(mGigNameEditText.getText().toString());
                mDatabase.child("Gigs/" + mGigID + "/" + "startDate").setValue(newStartDate);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }

        // If the finish date and finish time are edited
        else if(!isStartDateEdited && isFinishDateEdited && !isStartTimeEdited && isFinishTimeEdited)
        {
            Date newEndDate;

            try
            {
                newEndDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                        .parse(mUpdatedFinishDay + "/" + mUpdatedFinishMonth
                                + "/" + mUpdatedFinishYear + " " + mUpdatedFinishHour
                                + ":" + mUpdatedFinishMinute + ":" + 00.000);

                mDatabase.child("Gigs/" + mGigID + "/" + "title").setValue(mGigNameEditText.getText().toString());
                mDatabase.child("Gigs/" + mGigID + "/" + "endDate").setValue(newEndDate);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }

        // If both the dates are edited, but neither of the times
        else if(isStartDateEdited && isFinishDateEdited && !isStartTimeEdited && !isFinishTimeEdited)
        {
            Date newStartDateWithExistingTime;
            Date newFinishDateWithExistingTime;

            try
            {
                newStartDateWithExistingTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                        .parse(mUpdatedStartDay + "/" + mUpdatedStartMonth
                                + "/" + mUpdatedStartYear + " " + currentlySetStartDate.getHours()
                                + ":" + currentlySetStartDate.getMinutes() + ":" + 00.000);

                newFinishDateWithExistingTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                        .parse(mUpdatedFinishDay + "/" + mUpdatedFinishMonth
                                + "/" + mUpdatedFinishYear + " " + currentlySetEndDate.getHours()
                                + ":" + currentlySetEndDate.getMinutes() + ":" + 00.000);

                mDatabase.child("Gigs/" + mGigID + "/" + "title").setValue(mGigNameEditText.getText().toString());
                mDatabase.child("Gigs/" + mGigID + "/" + "startDate").setValue(newStartDateWithExistingTime);
                mDatabase.child("Gigs/" + mGigID + "/" + "endDate").setValue(newFinishDateWithExistingTime);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }

        else if(!isStartDateEdited && !isFinishDateEdited && isStartTimeEdited && isFinishTimeEdited)
        {
            Date newStartTimeWithExistingDate;
            Date newFinishTimeWithExistingDate;

            try
            {
                newStartTimeWithExistingDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                        .parse(currentlySetStartDate.getDate() + "/" + currentlySetStartDate.getMonth()
                                + "/" + currentlySetStartDate.getYear() + " " + mUpdatedStartHour
                                + ":" + mUpdatedStartMinute + ":" + 00.000);

                newFinishTimeWithExistingDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                        .parse(currentlySetEndDate.getDate() + "/" + currentlySetEndDate.getMonth()
                                + "/" + currentlySetEndDate.getYear() + " " + mUpdatedFinishHour
                                + ":" + mUpdatedFinishMinute + ":" + 00.000);

                mDatabase.child("Gigs/" + mGigID + "/" + "title").setValue(mGigNameEditText.getText().toString());
                mDatabase.child("Gigs/" + mGigID + "/" + "startDate").setValue(newStartTimeWithExistingDate);
                mDatabase.child("Gigs/" + mGigID + "/" + "endDate").setValue(newFinishTimeWithExistingDate);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }


    }

    // Android seems to think this method is required even
    // though it's implemented elsewhere. If it's removed it
    // throws an error so it's been left in for that purpose.
    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1)
    {


    }

    private void ReturnToMyGigs()
    {
        // The user is then taken to the home fragment
        getActivity().setTitle("My Gigs");
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.frame, new VenueUserViewGigsFragment(), "VenueUserViewGigsFragment");
        ft.commit();
    }
}
