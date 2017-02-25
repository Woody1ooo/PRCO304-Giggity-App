package com.liamd.giggity_app;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class VenueUserCreateGigFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener
{
    private Boolean isStartDate;

    // Declare visual components
    private EditText mGigNameTextView;
    private EditText mVenueNameTextView;
    private Button mSelectStartDateButton;
    private Button mSelectFinishDateButton;
    private TextView mStartDateSelectedTextView;
    private TextView mFinishDateSelectedTextView;
    private Button mSelectStartTimeButton;
    private Button mSelectFinishTimeButton;
    private TextView mStartTimeSelectedTextView;
    private TextView mFinishTimeSelectedTextView;
    private Button mCreateGigButton;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Declare general variables required
    private List<Gig> mListOfVenueGigs = new ArrayList<>();
    private List<Date> mListOfGigDates = new ArrayList<>();
    private String mVenueId;
    private Date mStartDate;
    private Date mFinishDate;

    private int mStartYear;
    private int mStartMonth;
    private int mStartDay;
    private int mStartHour;
    private int mStartMinute;

    private int mFinishYear;
    private int mFinishMonth;
    private int mFinishDay;
    private int mFinishHour;
    private int mFinishMinute;

    public VenueUserCreateGigFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.venue_user_fragment_gig_creator, container, false);

        // initialise visual components
        mVenueNameTextView = (EditText) fragmentView.findViewById(R.id.VenueNameTextView);
        mGigNameTextView = (EditText) fragmentView.findViewById(R.id.gigNameTextView);

        mSelectStartDateButton = (Button) fragmentView.findViewById(R.id.LaunchStartDatePickerButton);
        mSelectFinishDateButton = (Button) fragmentView.findViewById(R.id.LaunchFinishDatePickerButton);

        mStartDateSelectedTextView = (TextView) fragmentView.findViewById(R.id.StartDateSelectedLabel);
        mFinishDateSelectedTextView = (TextView) fragmentView.findViewById(R.id.FinishDateSelectedLabel);

        mSelectStartTimeButton = (Button) fragmentView.findViewById(R.id.LaunchStartTimePickerButton);
        mSelectFinishTimeButton = (Button) fragmentView.findViewById(R.id.LaunchFinishTimePickerButton);

        mStartTimeSelectedTextView = (TextView) fragmentView.findViewById(R.id.StartTimeSelectedLabel);
        mFinishTimeSelectedTextView = (TextView) fragmentView.findViewById(R.id.FinishTimeSelectedLabel);

        mCreateGigButton = (Button) fragmentView.findViewById(R.id.createGigButton);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mVenueId = dataSnapshot.child("Users/" +
                        mAuth.getCurrentUser().getUid() + "/venueID").getValue().toString();

                String venueName;
                venueName = dataSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString();
                mVenueNameTextView.setText(venueName);
                mVenueNameTextView.setEnabled(false);

                Iterable<DataSnapshot> children = dataSnapshot.child("Gigs/").getChildren();

                for (DataSnapshot child : children)
                {
                    Gig gig;
                    gig = child.getValue(Gig.class);
                    mListOfVenueGigs.add(gig);
                }

                mSelectStartDateButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        isStartDate = true;
                        StartDateShowCalendar();
                    }
                });

                mSelectFinishDateButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        isStartDate = false;
                        FinishDateShowCalendar();
                    }
                });

                mSelectStartTimeButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        StartTimeShowTimePicker();
                    }
                });

                mSelectFinishTimeButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        FinishTimeShowTimePicker();
                    }
                });

                mCreateGigButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        CreateGig();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        mSelectStartDateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                StartDateShowCalendar();
            }
        });
        return fragmentView;
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

        Calendar[] mAlreadyBookedDates = new Calendar[mListOfGigDates.size()];

        for(int j = 0; j < mListOfGigDates.size(); j++)
        {
            Calendar cal = Calendar.getInstance();
            cal.set(mListOfGigDates.get(j).getYear() + 1900, mListOfGigDates.get(j).getMonth(), mListOfGigDates.get(j).getDate());
            mAlreadyBookedDates[j] = cal;
        }

        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                VenueUserCreateGigFragment.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        dpd.setDisabledDays(mAlreadyBookedDates);
        dpd.show(getActivity().getFragmentManager(), "DatePickerDialog");
        dpd.setMinDate(Calendar.getInstance());
        mListOfGigDates.clear();
    }

    private void FinishDateShowCalendar()
    {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                VenueUserCreateGigFragment.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        dpd.show(getActivity().getFragmentManager(), "DatePickerDialog");
        dpd.setMinDate(Calendar.getInstance());
        mListOfGigDates.clear();
    }

    private void StartTimeShowTimePicker()
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
                mStartHour = selectedHour;
                mStartMinute = selectedMinute;
                mStartTimeSelectedTextView.setText( selectedHour + ":" + selectedMinute);
            }
        }, mHour, mMinute, false);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

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
                mFinishHour = selectedHour;
                mFinishMinute = selectedMinute;
                mFinishTimeSelectedTextView.setText( selectedHour + ":" + selectedMinute);
            }
        }, mHour, mMinute, false);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }


    @SuppressLint("SimpleDateFormat")
    @Override
    public void onDateSet(DatePickerDialog dpd, int year, int monthOfYear, int dayOfMonth)
    {
        String yearSelected = Integer.toString(year);
        String monthSelected = Integer.toString(monthOfYear + 1);
        String daySelected = Integer.toString(dayOfMonth);

        if(isStartDate)
        {
            mStartYear = year;
            mStartMonth = monthOfYear + 1;
            mStartDay = dayOfMonth;

            mStartDateSelectedTextView.setText(daySelected + "/" + monthSelected + "/" + yearSelected);
        }

        else
        {
            mFinishYear = year;
            mFinishMonth = monthOfYear + 1;
            mFinishDay = dayOfMonth;

            mFinishDateSelectedTextView.setText(daySelected + "/" + monthSelected + "/" + yearSelected);
        }
    }

    private void CreateGig()
    {
        try
        {
            mStartDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").parse(mStartDay + "/" +
                    mStartMonth + "/" + mStartYear + " " +
                    mStartHour + ":" + mStartMinute + ":" + "00" + "." + "000");
        }

        catch (ParseException e)
        {
            e.printStackTrace();
        }

        try
        {
            mFinishDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").parse(mFinishDay + "/" +
                    mFinishMonth + "/" + mFinishYear + " " +
                    mFinishHour + ":" + mFinishMinute + ":" + "00" + "." + "000");
        }

        catch (ParseException e)
        {
            e.printStackTrace();
        }

        Gig gigToInsert = new Gig();
        gigToInsert.setStartDate(mStartDate);
        gigToInsert.setEndDate(mFinishDate);
        gigToInsert.setTitle(mGigNameTextView.getText().toString());
        gigToInsert.setVenueID(mVenueId);
        mDatabase.child("Gigs/").push().setValue(gigToInsert);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1)
    {

    }
}
