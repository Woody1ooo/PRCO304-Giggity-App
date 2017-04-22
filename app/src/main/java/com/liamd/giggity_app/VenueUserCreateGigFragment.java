package com.liamd.giggity_app;


import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
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

    private Boolean isStartDate;

    private final static int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR = 1;
    private boolean hasPermission = true;
    private String mGigId;

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

        mCreateGigButton = (Button) fragmentView.findViewById(R.id.createGigButton);

        // Add items to the genre list, and set the spinner to use these
        mGenreList = new ArrayList<>();
        mGenreList.add("Classic Rock");
        mGenreList.add("Alternative Rock");
        mGenreList.add("Blues");
        mGenreList.add("Indie");
        mGenreList.add("Metal");
        mGenreList.add("Pop");
        mGenreList.add("Classical");
        mGenreList.add("Jazz");
        mGenreList.add("Acoustic");

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
        builder.setIcon(R.drawable.ic_info_outline_black_24dp);
        builder.setTitle("Create Gig");
        builder.setMessage("Are you sure you want to create this gig?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                // This ensures that a value has been set for
                // name, start date, start time, and finish time
                if (mGigNameEditText.getText().toString().matches("")
                        || mStartDateSelectedTextView.getText().equals("No date selected!")
                        || mStartTimeSelectedTextView.getText().equals("No time selected!")
                        || mFinishTimeSelectedTextView.getText().equals("No time selected"))
                {
                    Toast.makeText(getActivity(),
                            "Please ensure you have given a value for all the required fields!",
                            Toast.LENGTH_SHORT).show();
                } else
                {
                    if (mGenresSpinner.getSelectedItem() == "")
                    {
                        Toast.makeText(getActivity(),
                                "Please ensure you have given a value for all the required fields!",
                                Toast.LENGTH_SHORT).show();
                    } else
                    {
                        try
                        {
                            // This creates a date object and populates it with the start date data.
                            // The seconds and milliseconds are hardcoded as I believe this level of
                            // specificity is not required.
                            mStartDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").parse(mStartDay + "/" +
                                    mStartMonth + "/" + mStartYear + " " +
                                    mStartHour + ":" + mStartMinute + ":" + "00" + "." + "000");
                        } catch (ParseException e)
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

                                // This is then inserted into the database using a push
                                // command to generate a new random identifier
                                mDatabase.child("Gigs/" + mGigId).setValue(gigToInsert);

                                mDatabase.child("Gigs/" + mGigId).child("genres").setValue(mGenresSpinner.getSelectedItemsAsString());
                                mDatabase.child("Gigs/" + mGigId).child("bookedAct").setValue("Vacant");

                                // A dialog is then shown to alert the user that the changes have been made
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Confirmation");
                                builder.setIcon(R.drawable.ic_event_available_black_24dp);
                                builder.setMessage("Gig Created! Would you like to add this to your device calendar?");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        // First check that the user has granted permission to write to the calendar
                                        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
                                        {
                                            requestPermissions(new String[]{Manifest.permission.WRITE_CALENDAR}, MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
                                        }

                                        if (hasPermission)
                                        {
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
                                                Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

                                                // The event id is then extracted
                                                final String eventID = uri.getLastPathSegment();

                                                if (eventID != null)
                                                {
                                                    // A dialog is then shown to alert the user that the changes have been made
                                                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    builder.setTitle("Confirmation");
                                                    builder.setIcon(R.drawable.ic_event_available_black_24dp);
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

                                        else
                                        {
                                            // A dialog is then shown to alert the user that the changes have been made
                                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setTitle("Error");
                                            builder.setIcon(R.drawable.ic_event_available_black_24dp);
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

                                    // This is then inserted into the database using a push
                                    // command to generate a new random identifier
                                    mDatabase.child("Gigs/" + mGigId).setValue(gigToInsert);

                                    mDatabase.child("Gigs/" + mGigId).child("genres").setValue(mGenresSpinner.getSelectedItemsAsString());
                                    mDatabase.child("Gigs/" + mGigId).child("bookedAct").setValue("Vacant");

                                    // A dialog is then shown to alert the user that the changes have been made
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setTitle("Confirmation");
                                    builder.setMessage("Gig Created! Would you like to add this to your device calendar?");
                                    builder.setIcon(R.drawable.ic_event_available_black_24dp);
                                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i)
                                        {
                                            if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
                                            {
                                                requestPermissions(new String[]{Manifest.permission.WRITE_CALENDAR}, MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
                                            }

                                            if(hasPermission)
                                            {
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
                                                Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

                                                // get the event ID that is the last element in the Uri
                                                final String eventID = uri.getLastPathSegment();

                                                if(eventID != null)
                                                {
                                                    // A dialog is then shown to alert the user that the changes have been made
                                                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    builder.setTitle("Confirmation");
                                                    builder.setIcon(R.drawable.ic_event_available_black_24dp);
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
                                                    builder.setIcon(R.drawable.ic_event_available_black_24dp);
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

    // This method processes the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_CALENDAR)

            // If the permission has been accepted update hasPermission to reflect this
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.WRITE_CALENDAR) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                hasPermission = true;
            }

            // If the permission has been denied then display a message to that effect
            else
            {
                Toast.makeText(getActivity(), "If you wish use this feature," +
                        " please ensure you have given permission to access your device's calendar.", Toast.LENGTH_SHORT).show();

                hasPermission = false;
            }
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

    private void ReturnToHome()
    {
        getActivity().finish();
        getActivity().overridePendingTransition(0,0);

        Intent intent = new Intent(getActivity(), VenueUserMainActivity.class);
        startActivity(intent);
        getFragmentManager().popBackStackImmediate();
    }
}
