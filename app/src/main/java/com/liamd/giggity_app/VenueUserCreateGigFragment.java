package com.liamd.giggity_app;


import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.CalendarContract;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import me.everything.providers.android.calendar.CalendarProvider;


/**
 * A simple {@link Fragment} subclass.
 */
public class VenueUserCreateGigFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener
{
    // Declare visual components
    private EditText mGigNameEditText;
    private EditText mVenueNameEditText;
    private MultiSelectSpinner mGenresSpinner;
    private Button mSelectStartDateButton;
    private Button mSelectFinishDateButton;
    private TextView mStartDateSelectedTextView;
    private TextView mFinishDateSelectedTextView;
    private Button mSelectStartTimeButton;
    private Button mSelectFinishTimeButton;
    private TextView mStartTimeSelectedTextView;
    private TextView mFinishTimeSelectedTextView;
    private NumberPicker mTicketCostNumberPicker;
    private TextView mTicketCostSelectedTextView;
    private TextView mTicketQuantitySelectedTextView;
    private CheckBox mMatchVenueCapacityCheckBox;
    private NumberPicker mTicketQuantityNumberPicker;
    private CheckBox mFeaturedItemCheckBox;
    private TextView mEntryAgeSelectedTextView;
    private NumberPicker mEntryAgeSelectedNumberPicker;
    private Button mCreateGigButton;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Declare general variables required
    private List<Gig> mListOfVenueGigs = new ArrayList<>();
    private List<Date> mListOfGigDates = new ArrayList<>();
    private List<String> mGenreList;
    private String mVenueId;
    private String mVenueName;
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

    private int mTicketCost;
    private int mVenueCapacity;
    private int mTicketQuantity;

    private Boolean isStartDate;

    private final static int PERMISSION_ALL = 1;
    private boolean mIsEventWithoutEndDate;
    private String mGigId;

    private boolean mIsFeatured;
    private int mFeaturedWeekQuantity;

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

        // Initialise visual components
        mVenueNameEditText = (EditText) fragmentView.findViewById(R.id.VenueNameEditText);
        mGigNameEditText = (EditText) fragmentView.findViewById(R.id.gigNameEditText);

        mGenresSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.genreSpinner);

        mSelectStartDateButton = (Button) fragmentView.findViewById(R.id.LaunchStartDatePickerButton);
        mSelectFinishDateButton = (Button) fragmentView.findViewById(R.id.LaunchFinishDatePickerButton);

        mStartDateSelectedTextView = (TextView) fragmentView.findViewById(R.id.StartDateSelectedLabel);
        mFinishDateSelectedTextView = (TextView) fragmentView.findViewById(R.id.FinishDateSelectedLabel);

        mSelectStartTimeButton = (Button) fragmentView.findViewById(R.id.LaunchStartTimePickerButton);
        mSelectFinishTimeButton = (Button) fragmentView.findViewById(R.id.LaunchFinishTimePickerButton);

        mStartTimeSelectedTextView = (TextView) fragmentView.findViewById(R.id.StartTimeSelectedLabel);
        mFinishTimeSelectedTextView = (TextView) fragmentView.findViewById(R.id.FinishTimeSelectedLabel);

        mTicketCostSelectedTextView = (TextView) fragmentView.findViewById(R.id.TicketCostSelectedTextView);
        mTicketCostNumberPicker = (NumberPicker) fragmentView.findViewById(R.id.ticketCostNumberPicker);
        mTicketCostNumberPicker.setMinValue(0);
        mTicketCostNumberPicker.setMaxValue(100);

        mTicketQuantitySelectedTextView = (TextView) fragmentView.findViewById(R.id.TicketQuantitySelectedTextView);
        mMatchVenueCapacityCheckBox = (CheckBox) fragmentView.findViewById(R.id.matchVenueCapacityItemCheckBox);
        mTicketQuantityNumberPicker = (NumberPicker) fragmentView.findViewById(R.id.ticketQuantityNumberPicker);
        mTicketQuantityNumberPicker.setMinValue(1);

        mFeaturedItemCheckBox = (CheckBox) fragmentView.findViewById(R.id.featuredItemCheckBox);

        mEntryAgeSelectedTextView = (TextView) fragmentView.findViewById(R.id.entryAgeSelectedTextView);
        mEntryAgeSelectedNumberPicker = (NumberPicker) fragmentView.findViewById(R.id.entryAgeNumberPicker);
        mEntryAgeSelectedNumberPicker.setMinValue(0);
        mEntryAgeSelectedNumberPicker.setMaxValue(100);

        mCreateGigButton = (Button) fragmentView.findViewById(R.id.createGigButton);

        // Add items to the genre list, and set the spinner to use these
        mGenreList = new ArrayList<>();
        mGenreList.add("Acoustic");
        mGenreList.add("Alternative Rock");
        mGenreList.add("Blues");
        mGenreList.add("Classic Rock");
        mGenreList.add("Classical");
        mGenreList.add("Country");
        mGenreList.add("Death Metal");
        mGenreList.add("Disco");
        mGenreList.add("Electronic");
        mGenreList.add("Folk");
        mGenreList.add("Funk");
        mGenreList.add("Garage");
        mGenreList.add("Grunge");
        mGenreList.add("Hip-Hop");
        mGenreList.add("House");
        mGenreList.add("Indie");
        mGenreList.add("Jazz");
        mGenreList.add("Metal");
        mGenreList.add("Pop");
        mGenreList.add("Psychedelic Rock");
        mGenreList.add("Punk");
        mGenreList.add("Rap");
        mGenreList.add("Reggae");
        mGenreList.add("R&B");
        mGenreList.add("Ska");
        mGenreList.add("Techno");
        mGenreList.add("Thrash Metal");

        mGenresSpinner.setItems(mGenreList);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // This checks the database to find the user's venueID
                mVenueId = dataSnapshot.child("Users/" +
                        mAuth.getCurrentUser().getUid() + "/venueID").getValue().toString();

                // This then retrieves the venue name associated with the ID
                // from the Venues parent node
                mVenueName = dataSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString();

                // This is then used as the text for a non-editable edit text field
                mVenueNameEditText.setText(mVenueName);
                mVenueNameEditText.setEnabled(false);

                // This populates the ticket quantity picker's maximum value with the capacity set when the venue was created
                mVenueCapacity = Integer.parseInt(dataSnapshot.child("Venues/" + mVenueId + "/capacity").getValue().toString());
                mTicketQuantityNumberPicker.setMaxValue(mVenueCapacity);

                // As the ticket quantities are changed the display is updated
                mTicketQuantityNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
                {
                    @Override
                    public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue)
                    {
                        mTicketQuantitySelectedTextView.setText(newValue + " ticket(s)");
                        mTicketQuantity = newValue;
                    }
                });

                // This method populates the genre spinner with the genres the user
                // selected when setting up their account
                mGenresSpinner.setSelection(PopulateUserGenreData(dataSnapshot));

                // Each gig is then iterated through and added to an
                // array list of gigs (mListOfVenueGigs)
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
                        // When the button to select a start date is clicked,
                        // the isStartDate variable is set to true, and the method to display
                        // the calendar is called
                        isStartDate = true;
                        StartDateShowCalendar();
                    }
                });

                mSelectFinishDateButton.setOnClickListener(new View.OnClickListener()
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

                mSelectStartTimeButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        // When the select start time button is clicked, the method to display
                        // the time picker is called
                        StartTimeShowTimePicker();
                    }
                });

                mSelectFinishTimeButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        // When the select finish time button is clicked, the method to display
                        // the time picker is called
                        FinishTimeShowTimePicker();
                    }
                });

                mTicketCostNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
                {
                    @Override
                    public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue)
                    {
                        mTicketCostSelectedTextView.setText("£" + newValue + ".00");
                        mTicketCost = newValue;
                    }
                });

                mMatchVenueCapacityCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
                    {
                        if(isChecked)
                        {
                            mTicketQuantityNumberPicker.setValue(mVenueCapacity);
                            mTicketQuantityNumberPicker.setEnabled(false);
                            mTicketQuantitySelectedTextView.setText(mVenueCapacity + " ticket(s)");
                            mTicketQuantity = mVenueCapacity;
                        }

                        else
                        {
                            mTicketQuantityNumberPicker.setEnabled(true);
                        }
                    }
                });

                mFeaturedItemCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
                    {
                        if (isChecked)
                        {
                            OpenFeaturedPaymentDialog();
                        }
                    }
                });


                mCreateGigButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        // Once all the other fields have been completed, this method to create
                        // the gig can be called on button click
                        CreateGig();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        mEntryAgeSelectedNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue)
            {
                mEntryAgeSelectedTextView.setText(newValue + " years");
            }
        });

        getActivity().setTitle("Create a Gig");

        return fragmentView;
    }

    private void StartDateShowCalendar()
    {
        // When the calender is clicked, we iterate through the list of gigObjects populated above
        // to find any gigs for the current venue. If there are any, these days are hidden from the
        // calendar to prevent double bookings
        for (int i = 0; i < mListOfVenueGigs.size(); i++)
        {
            if (mListOfVenueGigs.get(i).getVenueID().equals(mVenueId))
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
        for (int j = 0; j < mListOfGigDates.size(); j++)
        {
            Calendar cal = Calendar.getInstance();
            cal.set(mListOfGigDates.get(j).getYear() + 1900, mListOfGigDates.get(j).getMonth(), mListOfGigDates.get(j).getDate());
            mAlreadyBookedDates[j] = cal;
        }

        // The calendar is then initialised with today's date
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                VenueUserCreateGigFragment.this,
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
                VenueUserCreateGigFragment.this,
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
                mStartHour = selectedHour;
                mStartMinute = selectedMinute;
                mStartTimeSelectedTextView.setText(selectedHour + ":" + selectedMinute);
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
                mFinishHour = selectedHour;
                mFinishMinute = selectedMinute;
                mFinishTimeSelectedTextView.setText(selectedHour + ":" + selectedMinute);
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
        if (isStartDate)
        {
            mStartYear = year;
            mStartMonth = monthOfYear + 1;
            mStartDay = dayOfMonth;

            mStartDateSelectedTextView.setText(mDaySelected + "/" + mMonthSelected + "/" + mYearSelected);
        }

        // Otherwise it means the finish date button was selected
        else
        {
            mFinishYear = year;
            mFinishMonth = monthOfYear + 1;
            mFinishDay = dayOfMonth;

            mFinishDateSelectedTextView.setText(mDaySelected + "/" + mMonthSelected + "/" + mYearSelected);
        }
    }

    // This method takes all the data and creates a gig in the database
    private void CreateGig()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_info_outline_black_24px);
        builder.setTitle("Create Gig");
        builder.setMessage("Are you sure you want to create this gig?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                // This ensures that a value has been set for
                // name, start date, start time, finish time, ticket cost, and ticket quantity
                if (mGigNameEditText.getText().toString().matches("")
                        || mStartDateSelectedTextView.getText().equals("No date selected!")
                        || mStartTimeSelectedTextView.getText().equals("No time selected!")
                        || mFinishTimeSelectedTextView.getText().equals("No time selected")
                        || mTicketCostSelectedTextView.getText().equals("No cost selected!")
                        || mTicketQuantitySelectedTextView.getText().equals("No tickets selected!"))
                {
                    Toast.makeText(getActivity(),
                            "Please ensure you have given a value for all the required fields!",
                            Toast.LENGTH_SHORT).show();
                }

                else
                {
                    if (mGenresSpinner.getSelectedItem() == "")
                    {
                        Toast.makeText(getActivity(),
                                "Please ensure you have given a value for all the required fields!",
                                Toast.LENGTH_SHORT).show();
                    }

                    else
                    {
                        try
                        {
                            // This creates a date object and populates it with the start date data.
                            // The seconds and milliseconds are hardcoded as I believe this level of
                            // specificity is not required.
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
                            // If the finish date has been left blank, then we can assume that
                            // the gig finishes on the same day as the start. We therefore just
                            // take the date information from the start date, but append the finish
                            // times as defined by the user.
                            if (mFinishDateSelectedTextView.getText().equals
                                    ("No date selected! (ignore if this is the same as the start date)"))
                            {
                                mGigId = mDatabase.push().getKey();

                                mFinishDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").parse(mStartDay + "/" +
                                        mStartMonth + "/" + mStartYear + " " +
                                        mFinishHour + ":" + mFinishMinute + ":" + "00" + "." + "000");

                                // A gig object is then created and populated with
                                // the data generated across this page
                                Gig gigToInsert = new Gig(
                                        mFinishDate,
                                        mStartDate,
                                        mGigNameEditText.getText().toString(),
                                        mVenueId,
                                        mGigId);

                                // Set the age restriction against the gig if one has been set
                                if(mEntryAgeSelectedNumberPicker.getValue() != 0)
                                {
                                    gigToInsert.setAgeRestriction(mEntryAgeSelectedNumberPicker.getValue());
                                }

                                // This is then inserted into the database using a push
                                // command to generate a new random identifier
                                mDatabase.child("Gigs/" + mGigId).setValue(gigToInsert);

                                mDatabase.child("Gigs/" + mGigId).child("genres").setValue(mGenresSpinner.getSelectedItemsAsString());
                                mDatabase.child("Gigs/" + mGigId).child("bookedAct").setValue("Vacant");

                                // Insert the ticket information to the database
                                mDatabase.child("Gigs/" + mGigId).child("ticketCost").setValue(mTicketCost);
                                mDatabase.child("Gigs/" + mGigId).child("ticketQuantity").setValue(mTicketQuantity);

                                // Get the current date time for the news items
                                Calendar calendar = Calendar.getInstance();
                                Date date = calendar.getTime();

                                if(!mIsFeatured)
                                {
                                    // This creates a ticket object and posts it to the database under the generated push key
                                    String newsItemId = mDatabase.child("NewsFeedItems").push().getKey();
                                    NewsFeedItem newsFeedItem = new NewsFeedItem(newsItemId, mVenueName, "has just posted a gig at their venue! Musicians wanted!", mGigId, mIsFeatured, mFeaturedWeekQuantity, date);
                                    mDatabase.child("NewsFeedItems/" + newsItemId + "/").setValue(newsFeedItem);
                                }

                                // A dialog is then shown to alert the user that the changes have been made
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Confirmation");
                                builder.setIcon(R.drawable.ic_event_available_black_24px);
                                builder.setMessage("Gig Created! Would you like to add this to your device calendar?");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        mIsEventWithoutEndDate = true;

                                        // First check that the user has granted permission to write to the calendar
                                        String[] PERMISSIONS = {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR};

                                        if (!hasPermissions(getActivity(), PERMISSIONS))
                                        {
                                            requestPermissions(PERMISSIONS, PERMISSION_ALL);
                                        }

                                        else if (hasPermissions(getActivity(), PERMISSIONS))
                                        {
                                            CreateCalendarEvent();
                                        }

                                        else
                                        {
                                            // A dialog is then shown to alert the user that the changes have been made
                                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setTitle("Error");
                                            builder.setIcon(R.drawable.ic_event_available_black_24px);
                                            builder.setMessage("Calendar Event Could Not Be Added!");
                                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i)
                                                {
                                                    ReturnToHome();
                                                }
                                            });
                                            builder.setCancelable(false);
                                            builder.show();
                                        }
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        ReturnToHome();
                                    }
                                });
                                builder.setCancelable(false);
                                builder.show();
                            }

                            else
                            {
                                mGigId = mDatabase.push().getKey();

                                // This code is called when the user selects a specific finish date.
                                mFinishDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").parse(mFinishDay + "/" +
                                        mFinishMonth + "/" + mFinishYear + " " +
                                        mFinishHour + ":" + mFinishMinute + ":" + "00" + "." + "000");

                                if (mStartDate.after(mFinishDate))
                                {
                                    Toast.makeText(getActivity(),
                                            "Please ensure that the start date and " +
                                                    "times are before the finish date and times!",
                                            Toast.LENGTH_SHORT).show();
                                }

                                else
                                {
                                    // A gig object is then created and populated with
                                    // the data generated across this page
                                    Gig gigToInsert = new Gig(
                                            mFinishDate,
                                            mStartDate,
                                            mGigNameEditText.getText().toString(),
                                            mVenueId,
                                            mGigId);

                                    // Set the age restriction against the gig if one has been set
                                    if(mEntryAgeSelectedNumberPicker.getValue() != 0)
                                    {
                                        gigToInsert.setAgeRestriction(mEntryAgeSelectedNumberPicker.getValue());
                                    }

                                    // This is then inserted into the database using a push
                                    // command to generate a new random identifier
                                    mDatabase.child("Gigs/" + mGigId).setValue(gigToInsert);

                                    mDatabase.child("Gigs/" + mGigId).child("genres").setValue(mGenresSpinner.getSelectedItemsAsString());
                                    mDatabase.child("Gigs/" + mGigId).child("bookedAct").setValue("Vacant");

                                    // Insert the ticket information to the database
                                    mDatabase.child("Gigs/" + mGigId).child("ticketCost").setValue(mTicketCost);
                                    mDatabase.child("Gigs/" + mGigId).child("ticketQuantity").setValue(mTicketQuantity);

                                    // Get the current date time for the news items
                                    Calendar calendar = Calendar.getInstance();
                                    Date date = calendar.getTime();

                                    if(!mIsFeatured)
                                    {
                                        // This creates a news feed object and posts it to the database under the generated push key
                                        String newsItemId = mDatabase.child("NewsFeedItems").push().getKey();
                                        NewsFeedItem newsFeedItem = new NewsFeedItem(newsItemId, mVenueName, "has just posted a gig at their venue! Musicians wanted!", mGigId, mIsFeatured, mFeaturedWeekQuantity, date);
                                        mDatabase.child("NewsFeedItems/" + newsItemId + "/").setValue(newsFeedItem);
                                    }

                                    // A dialog is then shown to alert the user that the changes have been made
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setTitle("Confirmation");
                                    builder.setMessage("Gig Created! Would you like to add this to your device calendar?");
                                    builder.setIcon(R.drawable.ic_event_available_black_24px);
                                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i)
                                        {
                                            mIsEventWithoutEndDate = false;

                                            // First check that the user has granted permission to write to the calendar
                                            String[] PERMISSIONS = {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR};

                                            if (!hasPermissions(getActivity(), PERMISSIONS))
                                            {
                                                requestPermissions(PERMISSIONS, PERMISSION_ALL);
                                            }

                                            else if (hasPermissions(getActivity(), PERMISSIONS))
                                            {
                                                CreateCalendarEvent();
                                            }
                                        }
                                    });
                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i)
                                        {
                                            ReturnToHome();
                                        }
                                    });
                                    builder.setCancelable(false);
                                    builder.show();
                                }
                            }
                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {

            }
        });
        builder.show();
    }

    // This method takes a snapshot of the database as a parameter and returns a
    // list of trimmed strings to populate the list of genres that the user selected
    // when they created their account
    private ArrayList<String> PopulateUserGenreData(DataSnapshot dataSnapshot)
    {
        // This takes the list of genres from the database that the user has selected
        // and adds them to a string
        String userPulledGenres = dataSnapshot.child("Venues/" + mVenueId + "/genre").getValue().toString();

        // This then splits this string into an array of strings, each separated by
        // a comma
        List<String> splitUserPulledGenres = Arrays.asList(userPulledGenres.split(","));

        // For the select list to understand this, they need any leading or trailing
        // spaces to be removed
        ArrayList<String> splitUserPulledGenresFormatted = new ArrayList<>();

        // The string array is then iterated through and added to a separate string
        // array and passed to the spinner.
        for (int i = 0; i < splitUserPulledGenres.size(); i++)
        {
            String formattedGenreStringToAdd;

            formattedGenreStringToAdd = splitUserPulledGenres.get(i).trim();

            splitUserPulledGenresFormatted.add(formattedGenreStringToAdd);
        }

        return splitUserPulledGenresFormatted;
    }

    // Android seems to think this method is required even
    // though it's implemented elsewhere. If it's removed it
    // throws an error so it's been left in for that purpose.
    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1)
    {

    }

    private void OpenFeaturedPaymentDialog()
    {
        final Dialog ticketPickerDialog = new Dialog(getActivity());
        ticketPickerDialog.setContentView(R.layout.featured_item_purchase_dialog_layout);

        ticketPickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialogInterface)
            {
                mFeaturedItemCheckBox.setChecked(false);
            }
        });

        final int cost = 15;

        // Initialise dialog visual components
        final Spinner mWeeksQuantitySpinner = (Spinner) ticketPickerDialog.findViewById(R.id.weeksQuantitySpinner);
        final TextView mCostTotalTextView = (TextView) ticketPickerDialog.findViewById(R.id.costTotalTextView);
        Button mPurchaseButton = (Button) ticketPickerDialog.findViewById(R.id.purchaseButton);

        mWeeksQuantitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
            {
                if(position == 0)
                {
                    mCostTotalTextView.setText("£" + cost + ".00");
                    mFeaturedWeekQuantity = 1;
                }

                else if(position == 1)
                {
                    mCostTotalTextView.setText("£" + cost * 2 + ".00");
                    mFeaturedWeekQuantity = 2;
                }

                else if(position == 2)
                {
                    mCostTotalTextView.setText("£" + cost * 3 + ".00");
                    mFeaturedWeekQuantity = 3;
                }

                else if(position == 3)
                {
                    mCostTotalTextView.setText("£" + cost * 4 + ".00");
                    mFeaturedWeekQuantity = 4;
                }

                else if(position == 4)
                {
                    mCostTotalTextView.setText("£" + cost * 5 + ".00");
                    mFeaturedWeekQuantity = 5;
                }

                else if(position == 5)
                {
                    mCostTotalTextView.setText("£" + cost * 6 + ".00");
                    mFeaturedWeekQuantity = 6;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        });

        mPurchaseButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // This dialog is created to confirm that the users want to purchase the tickets
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Confirm Purchase");
                builder.setMessage("Are you sure you wish to make this purchase?");
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        dialogInterface.dismiss();

                        mIsFeatured = true;

                        // Get the current date time for the news items
                        Calendar calendar = Calendar.getInstance();
                        Date date = calendar.getTime();

                        // This creates a ticket object and posts it to the database under the generated push key
                        String newsItemId = mDatabase.child("NewsFeedItems").push().getKey();
                        NewsFeedItem newsFeedItem = new NewsFeedItem(newsItemId, mVenueName, "has just posted a gig at their venue! Musicians wanted!", mGigId, mIsFeatured, mFeaturedWeekQuantity, date);
                        mDatabase.child("NewsFeedItems/" + newsItemId + "/").setValue(newsFeedItem);

                        // This dialog is created to confirm that the users want to purchase the tickets
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Confirmation");
                        builder.setMessage("Featured status purchased! This will now appear as a featured item at the top of the news feed for " + mFeaturedWeekQuantity + " weeks." +
                                "Please ensure that you still complete the creation of this gig at the bottom of the page.");
                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                dialogInterface.dismiss();
                                mFeaturedItemCheckBox.setEnabled(false);
                                mFeaturedItemCheckBox.setChecked(true);
                                ticketPickerDialog.dismiss();
                            }
                        });
                        builder.show();
                        builder.setCancelable(false);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {

                    }
                });
                builder.show();
            }
        });
        ticketPickerDialog.show();
    }

    public static boolean hasPermissions(Context context, String... permissions)
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null)
        {
            for (String permission : permissions)
            {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                {
                    return false;
                }
            }
        }
        return true;
    }

    // This method processes the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == PERMISSION_ALL)
        {
            // If the permission has been accepted update hasPermission to reflect this
            if (permissions.length == 2 && permissions[0].equals(Manifest.permission.READ_CALENDAR) && permissions[1].equals(Manifest.permission.WRITE_CALENDAR) && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                if (mIsEventWithoutEndDate)
                {
                    CreateCalendarEventWithoutEndDate();
                }

                else if (!mIsEventWithoutEndDate)
                {
                    CreateCalendarEvent();
                }
            }

            // If the permission has been denied then display a message to that effect
            else
            {
                Toast.makeText(getActivity(), "If you wish use this feature," +
                        " please ensure you have given permission to access your device's calendar.", Toast.LENGTH_SHORT).show();
                ReturnToHome();
            }
        }
    }

    private void CreateCalendarEvent()
    {
        // First check that the user has granted permission to write to the calendar
        String[] PERMISSIONS = {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR};

        CalendarProvider provider = new CalendarProvider(getActivity());
        List<me.everything.providers.android.calendar.Calendar> calendars = provider.getCalendars().getList();

        // Insert Event
        ContentResolver cr = getActivity().getContentResolver();
        ContentValues values = new ContentValues();
        TimeZone timeZone = TimeZone.getDefault();
        values.put(CalendarContract.Events.DTSTART, mStartDate.getTime());
        values.put(CalendarContract.Events.DTEND, mFinishDate.getTime());
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
        values.put(CalendarContract.Events.TITLE, mGigNameEditText.getText().toString());
        values.put(CalendarContract.Events.CALENDAR_ID, calendars.get(0).id);
        values.put(CalendarContract.Events.EVENT_LOCATION, mVenueName);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSION_ALL);
        }

        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

        // get the event ID that is the last element in the Uri
        final String eventID = uri.getLastPathSegment();

        if(eventID != null)
        {
            // A dialog is then shown to alert the user that the changes have been made
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Confirmation");
            builder.setIcon(R.drawable.ic_event_available_black_24px);
            builder.setMessage("Calendar Event Added!");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    mDatabase.child("Gigs/" + mGigId + "/calendarEventId").setValue(eventID);
                    ReturnToHome();
                }
            });
            builder.setCancelable(false);
            builder.show();
        }

        else
        {
            // A dialog is then shown to alert the user that the changes have been made
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Error");
            builder.setIcon(R.drawable.ic_event_available_black_24px);
            builder.setMessage("Calendar Event Could Not Be Added!");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    ReturnToHome();
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }

    private void CreateCalendarEventWithoutEndDate()
    {
        // First check that the user has granted permission to write to the calendar
        String[] PERMISSIONS = {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR};

        // This 3rd party library then provides a list of all the calendar providers that the user has
        CalendarProvider provider = new CalendarProvider(getActivity());
        List<me.everything.providers.android.calendar.Calendar> calendars = provider.getCalendars().getList();

        // This then creates and inserts the event with the details into the calendar with the first element of the providers list
        ContentResolver cr = getActivity().getContentResolver();
        ContentValues values = new ContentValues();
        TimeZone timeZone = TimeZone.getDefault();
        values.put(CalendarContract.Events.DTSTART, mStartDate.getTime());
        values.put(CalendarContract.Events.DTEND, mFinishDate.getTime());
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
        values.put(CalendarContract.Events.TITLE, mGigNameEditText.getText().toString());

        if (calendars.size() <= 0)
        {
            Toast.makeText(getActivity(), "Please ensure you have at least one device calendar setup!", Toast.LENGTH_SHORT).show();
        }

        else
        {
            values.put(CalendarContract.Events.CALENDAR_ID, calendars.get(0).id);
            values.put(CalendarContract.Events.EVENT_LOCATION, mVenueName);

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSION_ALL);
            }

            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

            // The event id is then extracted
            final String eventID = uri.getLastPathSegment();

            if (eventID != null)
            {
                // A dialog is then shown to alert the user that the changes have been made
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Confirmation");
                builder.setIcon(R.drawable.ic_event_available_black_24px);
                builder.setMessage("Calendar Event Added!");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        // This value is then stored in the database so edits can be made to the event
                        mDatabase.child("Gigs/" + mGigId + "/calendarEventId").setValue(eventID);
                        ReturnToHome();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        }
    }

    private void ReturnToHome()
    {
        getActivity().finish();
        getActivity().overridePendingTransition(0,0);

        Intent intent = new Intent(getActivity(), VenueUserMainActivity.class);
        startActivity(intent);
        getFragmentManager().popBackStackImmediate();
    }
}