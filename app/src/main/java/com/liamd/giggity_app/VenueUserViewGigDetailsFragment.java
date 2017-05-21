package com.liamd.giggity_app;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class VenueUserViewGigDetailsFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener
{
    // Declare visual components
    private EditText mGigNameEditText;
    private TextView mArtistNameTextView;
    private CircleImageView mArtistImageView;
    private Button mHireButton;
    private Button mRemoveButton;
    private MultiSelectSpinner mGenreSpinner;
    private Button mUpdateStartDateButton;
    private Button mUpdateFinishDateButton;
    private TextView mStartDateSelectedTextView;
    private TextView mFinishDateSelectedTextView;
    private Button mUpdateStartTimeButton;
    private Button mUpdateFinishTimeButton;
    private TextView mStartTimeSelectedTextView;
    private TextView mFinishTimeSelectedTextView;
    private NumberPicker mTicketCostNumberPicker;
    private TextView mTicketCostSelectedTextView;
    private CheckBox mMatchVenueCapacityCheckBox;
    private EditText mTicketQuantityEditText;
    private EditText mEntryAgeSelectedEditText;
    private Button mUpdateGigButton;
    private Button mDeleteGigButton;

    // Declare Firebase specific variables
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;

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
    private int mUpdatedStartHour;
    private int mUpdatedStartMinute;
    private int mUpdatedStartYear;
    private int mUpdatedStartMonth;
    private int mUpdatedStartDay;

    // Declare new finish date variables
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
    private boolean isStartDate;
    private boolean goodDates = false;
    private String mFormattedStartDateMinutes;
    private String mFormattedEndDateMinutes;
    private String mGigGenres;
    private String mVenueGenres;
    private String mBandId;
    private String mCalendarEventId;
    private int mTicketCost;
    private int mVenueCapacity;
    private int mTicketQuantity;
    private int mAge;

    // These variables determine which elements of the gig have been edited
    private Boolean isStartDateEdited = false;
    private Boolean isFinishDateEdited = false;
    private Boolean isStartTimeEdited = false;
    private Boolean isFinishTimeEdited = false;

    // These variables hold the converted month values e.g. from Jan to 01
    private String mExistingStartDateConvertedMonth;
    private String mExistingFinishDateConvertedMonth;

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

        mArtistNameTextView = (TextView) fragmentView.findViewById(R.id.artistNameTextView);
        mArtistImageView = (CircleImageView) fragmentView.findViewById(R.id.artistProfileImageView);
        mHireButton = (Button) fragmentView.findViewById(R.id.hireActButton);
        mRemoveButton = (Button) fragmentView.findViewById(R.id.removeActButton);

        mGenreSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.genreSpinner);

        mUpdateStartDateButton = (Button) fragmentView.findViewById(R.id.UpdateStartDatePickerButton);
        mUpdateFinishDateButton = (Button) fragmentView.findViewById(R.id.UpdateFinishDatePickerButton);

        mStartDateSelectedTextView = (TextView) fragmentView.findViewById(R.id.StartDateSelectedLabel);
        mFinishDateSelectedTextView = (TextView) fragmentView.findViewById(R.id.FinishDateSelectedLabel);

        mUpdateStartTimeButton = (Button) fragmentView.findViewById(R.id.UpdateStartTimePickerButton);
        mUpdateFinishTimeButton = (Button) fragmentView.findViewById(R.id.UpdateFinishTimePickerButton);

        mStartTimeSelectedTextView = (TextView) fragmentView.findViewById(R.id.StartTimeSelectedLabel);
        mFinishTimeSelectedTextView = (TextView) fragmentView.findViewById(R.id.FinishTimeSelectedLabel);

        mTicketCostSelectedTextView = (TextView) fragmentView.findViewById(R.id.TicketCostSelectedTextView);
        mTicketCostNumberPicker = (NumberPicker) fragmentView.findViewById(R.id.ticketCostNumberPicker);
        mTicketCostNumberPicker.setMinValue(0);
        mTicketCostNumberPicker.setMaxValue(100);

        mMatchVenueCapacityCheckBox = (CheckBox) fragmentView.findViewById(R.id.matchVenueCapacityItemCheckBox);
        mTicketQuantityEditText = (EditText) fragmentView.findViewById(R.id.ticketQuantityEditText);
        mEntryAgeSelectedEditText = (EditText) fragmentView.findViewById(R.id.entryAgeEditText);

        mUpdateGigButton = (Button) fragmentView.findViewById(R.id.UpdateGigButton);
        mDeleteGigButton = (Button) fragmentView.findViewById(R.id.DeleteGigButton);

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mProfileImageReference = mStorage.getReference();

        // Call the populate fields method to pull through the data of the gig to
        // allow it to be edited
        PopulateFields();

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Check if the gig has a calendar event associated with it
                if(dataSnapshot.child("Gigs/" + mGigID + "/calendarEventId").exists())
                {
                    // If so assign the value to this variable
                    mCalendarEventId = dataSnapshot.child("Gigs/" + mGigID + "/calendarEventId").getValue().toString();
                }

                // Obtain the details of the artist hired assuming there is one
                // If there isn't display the hire button
                if(dataSnapshot.child("Gigs/" + mGigID + "/bookedAct").getValue().equals("Vacant"))
                {
                    mHireButton.setVisibility(View.VISIBLE);
                    mRemoveButton.setVisibility(View.GONE);
                }

                // Otherwise populate the fields with the band's data
                else
                {
                    mHireButton.setVisibility(View.GONE);
                    mRemoveButton.setVisibility(View.VISIBLE);

                    mBandId = dataSnapshot.child("Gigs/" + mGigID + "/bookedAct").getValue().toString();
                    mArtistNameTextView.setText(dataSnapshot.child("Bands/" + mBandId + "/name").getValue().toString());

                    // This reference looks at the Firebase storage and works out whether the band has an image
                    mProfileImageReference.child("BandProfileImages/" + mBandId + "/profileImage")
                            .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                    {
                        // If the band has an image this is loaded into the image view
                        @Override
                        public void onSuccess(Uri uri)
                        {
                            // The caching and memory features have been disabled to allow only the latest image to display
                            Glide.with(getContext()).using(new FirebaseImageLoader()).load
                                    (mProfileImageReference.child("BandProfileImages/" + mBandId + "/profileImage"))
                                    .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(mArtistImageView);
                        }

                        // If the band doesn't have an image the default image is loaded
                    }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Picasso.with(getContext()).load(R.drawable.com_facebook_profile_picture_blank_portrait).resize(350, 350).into(mArtistImageView);
                        }
                    });
                }

                // This obtains the venue ID from the previous fragment
                mVenueId = getArguments().getString("GigVenueID");

                // Pull the venue capacity from the database
                mVenueCapacity = Integer.parseInt(dataSnapshot.child("Venues/" + mVenueId + "/capacity").getValue().toString());

                // Pull the ticket values from the previous fragment
                mTicketQuantity = getArguments().getInt("GigTicketQuantity");
                mTicketCost = getArguments().getInt("GigTicketCost");

                // Set the age to the value from the database assuming there is one set
                mAge = Integer.parseInt(dataSnapshot.child("Gigs/" + mGigID + "/ageRestriction").getValue().toString());

                // Set the UI components to these values
                mTicketCostNumberPicker.setValue(mTicketCost);
                mTicketCostSelectedTextView.setText("£" + mTicketCost + ".00");
                mTicketQuantityEditText.setText(String.valueOf(mTicketQuantity));
                mEntryAgeSelectedEditText.setText(String.valueOf(mAge));

                // Get the genres set against the venue
                mVenueGenres = dataSnapshot.child("Venues/" + mVenueId + "/genre").getValue().toString();
                mGigGenres = getArguments().getString("GigGenres");

                // This method populates the genre spinner with the genres the user
                // selected when setting up their account
                mGenreSpinner.setItems(PopulateUserGenreData(mVenueGenres));
                mGenreSpinner.setSelection(PopulateUserGenreData(mGigGenres));

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
                            mTicketQuantityEditText.setText(String.valueOf(mVenueCapacity));
                            mTicketQuantityEditText.setEnabled(false);
                            mTicketQuantity = mVenueCapacity;
                        }

                        else
                        {
                            mTicketQuantityEditText.setEnabled(true);
                        }
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

                mDeleteGigButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        DeleteGig();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        mTicketQuantityEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                if(!mTicketQuantityEditText.getText().toString().equals(""))
                {
                    mTicketQuantity = Integer.parseInt(mTicketQuantityEditText.getText().toString());
                }

                else
                {
                    mTicketQuantity = 0;
                }
            }
        });

        mEntryAgeSelectedEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                if(!mEntryAgeSelectedEditText.getText().toString().equals(""))
                {
                    mAge = Integer.parseInt(mEntryAgeSelectedEditText.getText().toString());
                }

                else
                {
                    mAge = 0;
                }
            }
        });

        mHireButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // The user is then taken to the my gigs fragment
                Bundle arguments = new Bundle();
                arguments.putString("GigId", mGigID);

                VenueUserBandFinderFragment fragment = new VenueUserBandFinderFragment();
                fragment.setArguments(arguments);

                getActivity().setTitle("Band Finder");
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                ft.replace(R.id.frame, fragment, "VenueUserBandFinderFragment")
                        .addToBackStack(null).commit();
            }
        });

        mRemoveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                RemoveAct();
            }
        });

        getActivity().setTitle("My Gigs");

        return fragmentView;
    }

    // This method takes a snapshot of the database as a parameter and returns a
    // list of trimmed strings to populate the list of genres that the user selected
    // when they created their account
    private ArrayList<String> PopulateUserGenreData(String gigGenres)
    {
        // This then splits this string into an array of strings, each separated by
        // a comma
        List<String> splitUserPulledGenres = Arrays.asList(gigGenres.split(","));

        // For the select list to understand this, they need any leading or trailing
        // spaces to be removed
        ArrayList<String> splitPulledGenresFormatted = new ArrayList<>();

        // The string array is then iterated through and added to a separate string
        // array and passed to the spinner.
        for (int i = 0; i < splitUserPulledGenres.size(); i++)
        {
            String formattedGenreStringToAdd;

            formattedGenreStringToAdd = splitUserPulledGenres.get(i).trim();

            splitPulledGenresFormatted.add(formattedGenreStringToAdd);
        }

        return splitPulledGenresFormatted;
    }

    // This method populates the fields with the data from the selected gig for reference
    // when editing
    private void PopulateFields()
    {
        // Populate components and variables with the details of the chosen gig
        mGigID = getArguments().getString("GigID");
        mExistingStartDate = getArguments().getString("GigStartDate");
        mExistingFinishDate = getArguments().getString("GigEndDate");

        // These lines splits the existing dates into two string arrays
        // so the individual date elements can be extracted and formatted into
        // the correct format
        String[] mExistingStartDateSplit = mExistingStartDate.split("\\s+");
        String[] mExistingFinishDateSplit = mExistingFinishDate.split("\\s+");

        // These switch statements facilitate the conversion from
        // Jan to 01 for example
        switch (mExistingStartDateSplit[1])
        {
            case "Jan":
                mExistingStartDateConvertedMonth = "01";
                break;
            case "Feb":
                mExistingStartDateConvertedMonth = "02";
                break;
            case "Mar":
                mExistingStartDateConvertedMonth = "03";
                break;
            case "Apr":
                mExistingStartDateConvertedMonth = "04";
                break;
            case "May":
                mExistingStartDateConvertedMonth = "05";
                break;
            case "Jun":
                mExistingStartDateConvertedMonth = "06";
                break;
            case "Jul":
                mExistingStartDateConvertedMonth = "07";
                break;
            case "Aug":
                mExistingStartDateConvertedMonth = "08";
                break;
            case "Sep":
                mExistingStartDateConvertedMonth = "09";
                break;
            case "Oct":
                mExistingStartDateConvertedMonth = "10";
                break;
            case "Nov":
                mExistingStartDateConvertedMonth = "11";
                break;
            case "Dec":
                mExistingStartDateConvertedMonth = "12";
                break;
        }

        switch (mExistingFinishDateSplit[1])
        {
            case "Jan":
                mExistingFinishDateConvertedMonth = "01";
                break;
            case "Feb":
                mExistingFinishDateConvertedMonth = "02";
                break;
            case "Mar":
                mExistingFinishDateConvertedMonth = "03";
                break;
            case "Apr":
                mExistingFinishDateConvertedMonth = "04";
                break;
            case "May":
                mExistingFinishDateConvertedMonth = "05";
                break;
            case "Jun":
                mExistingFinishDateConvertedMonth = "06";
                break;
            case "Jul":
                mExistingFinishDateConvertedMonth = "07";
                break;
            case "Aug":
                mExistingFinishDateConvertedMonth = "08";
                break;
            case "Sep":
                mExistingFinishDateConvertedMonth = "09";
                break;
            case "Oct":
                mExistingFinishDateConvertedMonth = "10";
                break;
            case "Nov":
                mExistingFinishDateConvertedMonth = "11";
                break;
            case "Dec":
                mExistingFinishDateConvertedMonth = "12";
                break;
        }

        // This parses the dates from a String back into a Date object
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        try
        {
            // The elements of the split string are then concatenated together
            // to form the correct date format where they are then converted into
            // date objects and stored within mParsedStartDate and mParsedFinishDate
            String startDateToParse = mExistingStartDateSplit[2] + "/" + mExistingStartDateConvertedMonth + "/" + mExistingStartDateSplit[5] + " " + mExistingStartDateSplit[3] + "." + "000";
            String endDateToParse = mExistingFinishDateSplit[2] + "/" + mExistingFinishDateConvertedMonth + "/" + mExistingFinishDateSplit[5] + " " + mExistingFinishDateSplit[3] + "." + "000";
            mParsedStartDate = format.parse(startDateToParse);
            mParsedFinishDate = format.parse(endDateToParse);

            // These variables are then used to populate the visual components.
            // The additions at the end are required due to the....unique way that
            // java stores dates...
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

        if(mParsedStartDate.getMinutes() < 10)
        {
            mFormattedStartDateMinutes = "0" + mParsedStartDate.getMinutes();
            mStartTimeSelectedTextView.setText(mParsedStartDate.getHours() + ":" + mFormattedStartDateMinutes);
        }

        if(mParsedFinishDate.getMinutes() < 10)
        {
            mFormattedEndDateMinutes = "0" + mParsedFinishDate.getMinutes();
            mFinishTimeSelectedTextView.setText(mParsedFinishDate.getHours() + ":" + mFormattedEndDateMinutes);
        }

        if(mParsedStartDate.getMinutes() >= 10)
        {
            mStartTimeSelectedTextView.setText(mParsedStartDate.getHours() + ":" + mParsedStartDate.getMinutes());
        }

        if(mParsedFinishDate.getMinutes() >= 10)
        {
            mFinishTimeSelectedTextView.setText(mParsedFinishDate.getHours() + ":" + mParsedFinishDate.getMinutes());
        }
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
        // Defaults the time picker to midnight
        int mHour = 0;
        int mMinute = 0;
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

                if(selectedMinute < 10 && selectedHour < 10)
                {
                    mStartTimeSelectedTextView.setText("0" + String.valueOf(selectedHour) + ":" + "0" + (String.valueOf(selectedMinute)));
                }

                else if(selectedMinute < 10)
                {
                    mStartTimeSelectedTextView.setText(String.valueOf(selectedHour) + ":" + "0" + (String.valueOf(selectedMinute)));
                }

                else if(selectedHour < 10)
                {
                    mStartTimeSelectedTextView.setText("0" + String.valueOf(selectedHour) + ":" + (String.valueOf(selectedMinute)));
                }

                else
                {
                    mStartTimeSelectedTextView.setText(selectedHour + ":" + selectedMinute);
                }

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
        // Defaults the time picker to midnight
        int mHour = 0;
        int mMinute = 0;
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
            {
                mUpdatedFinishHour = selectedHour;
                mUpdatedFinishMinute = selectedMinute;

                if(selectedMinute < 10 && selectedHour < 10)
                {
                    mFinishTimeSelectedTextView.setText("0" + String.valueOf(selectedHour) + ":" + "0" + (String.valueOf(selectedMinute)));
                }

                else if(selectedMinute < 10)
                {
                    mFinishTimeSelectedTextView.setText(String.valueOf(selectedHour) + ":" + "0" + (String.valueOf(selectedMinute)));
                }

                else if(selectedHour < 10)
                {
                    mFinishTimeSelectedTextView.setText("0" + String.valueOf(selectedHour) + ":" + (String.valueOf(selectedMinute)));
                }

                else
                {
                    mFinishTimeSelectedTextView.setText(selectedHour + ":" + selectedMinute);
                }

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

    // This method handles what happens when a gig is updated. Because of the way this needs to be
    // handled, the only way I could feasibly manage this was to take into account each edit possibility
    // and then update only these features accordingly
    private void UpdateGig()
    {
        // This dialog is created to confirm that the users want to edit the fields
        // they have chosen
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Save Gig Changes");
        builder.setMessage("Are you sure you wish to save these changes?");
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if(mGenreSpinner.getSelectedItem() == "" || mTicketQuantity <= 0 || mAge < 0)
                {
                    Toast.makeText(getActivity(),
                            "Please ensure you have given a value for all the required fields!",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (!isStartDateEdited && !isFinishDateEdited && !isStartTimeEdited && !isFinishTimeEdited && TitleIsCompleted())
                    {
                        mDatabase.child("Gigs/" + mGigID + "/" + "title").setValue(mGigNameEditText.getText().toString());
                        mDatabase.child("Gigs/" + mGigID + "/" + "venueID").setValue(mVenueId);
                        mDatabase.child("Gigs/" + mGigID + "/" + "genres").setValue(mGenreSpinner.getSelectedItemsAsString());
                        mDatabase.child("Gigs/" + mGigID + "/" + "ticketCost").setValue(mTicketCostNumberPicker.getValue());
                        mDatabase.child("Gigs/" + mGigID + "/" + "ticketQuantity").setValue(mTicketQuantity);
                        mDatabase.child("Gigs/" + mGigID + "/" + "ageRestriction").setValue(mAge);

                        // If the event has a calendar event update the relevant fields
                        if(mCalendarEventId != null)
                        {
                            ContentValues values = new ContentValues();

                            // Set the user's chosen title against the event
                            values.put(CalendarContract.Events.TITLE, mGigNameEditText.getText().toString());
                            Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(mCalendarEventId));
                            getActivity().getContentResolver().update(updateUri, values, null, null);
                        }

                        ConfirmationDialog();
                    }

                    if (isStartDateEdited && isFinishDateEdited && isStartTimeEdited && isFinishTimeEdited && TitleIsCompleted())
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

                            //newStartDate.setMonth(newStartDate.getMonth() + 1);
                            //newStartDate.setYear(newStartDate.getYear() + 1900);
                            //newEndDate.setMonth(newEndDate.getMonth() + 1);
                            //newEndDate.setYear(newEndDate.getYear() + 1900);

                            if(CheckDates(newStartDate, newEndDate))
                            {
                                mDatabase.child("Gigs/" + mGigID + "/" + "title").setValue(mGigNameEditText.getText().toString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "venueID").setValue(mVenueId);
                                mDatabase.child("Gigs/" + mGigID + "/" + "startDate").setValue(newStartDate);
                                mDatabase.child("Gigs/" + mGigID + "/" + "endDate").setValue(newEndDate);
                                mDatabase.child("Gigs/" + mGigID + "/" + "genres").setValue(mGenreSpinner.getSelectedItemsAsString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketCost").setValue(mTicketCostNumberPicker.getValue());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketQuantity").setValue(mTicketQuantity);
                                mDatabase.child("Gigs/" + mGigID + "/" + "ageRestriction").setValue(mAge);

                                // If the event has a calendar event update the relevant fields
                                if(mCalendarEventId != null)
                                {
                                    ContentValues values = new ContentValues();
                                    values.put(CalendarContract.Events.TITLE, mGigNameEditText.getText().toString());
                                    values.put(CalendarContract.Events.DTSTART, newStartDate.getTime());
                                    values.put(CalendarContract.Events.DTEND, newEndDate.getTime());
                                    Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(mCalendarEventId));
                                    getActivity().getContentResolver().update(updateUri, values, null, null);
                                }

                                ConfirmationDialog();
                            }

                            else
                            {
                                Toast.makeText(getActivity(),
                                        "Please ensure the start dates and times are before the end date" +
                                                "and times!",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                        catch (ParseException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    else if (isStartDateEdited && !isFinishDateEdited && isStartTimeEdited && !isFinishTimeEdited && TitleIsCompleted())
                    {
                        Date newStartDate;

                        try
                        {
                            newStartDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                                    .parse(mUpdatedStartDay + "/" + mUpdatedStartMonth
                                            + "/" + mUpdatedStartYear + " " + mUpdatedStartHour
                                            + ":" + mUpdatedStartMinute + ":" + 00.000);

                            //newStartDate.setMonth(newStartDate.getMonth() + 1);
                            //newStartDate.setYear(newStartDate.getYear() + 1900);

                            if(CheckDates(newStartDate, mParsedFinishDate))
                            {
                                mDatabase.child("Gigs/" + mGigID + "/" + "title").setValue(mGigNameEditText.getText().toString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "startDate").setValue(newStartDate);
                                mDatabase.child("Gigs/" + mGigID + "/" + "venueID").setValue(mVenueId);
                                mDatabase.child("Gigs/" + mGigID + "/" + "genres").setValue(mGenreSpinner.getSelectedItemsAsString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketCost").setValue(mTicketCostNumberPicker.getValue());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketQuantity").setValue(mTicketQuantity);
                                mDatabase.child("Gigs/" + mGigID + "/" + "ageRestriction").setValue(mAge);

                                // If the event has a calendar event update the relevant fields
                                if(mCalendarEventId != null)
                                {
                                    ContentValues values = new ContentValues();
                                    values.put(CalendarContract.Events.TITLE, mGigNameEditText.getText().toString());
                                    values.put(CalendarContract.Events.DTSTART, newStartDate.getTime());
                                    Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(mCalendarEventId));
                                    getActivity().getContentResolver().update(updateUri, values, null, null);
                                }

                                ConfirmationDialog();
                            }

                            else
                            {
                                Toast.makeText(getActivity(),
                                        "Please ensure the start dates and times are before the end date" +
                                                "and times!",
                                        Toast.LENGTH_SHORT).show();
                            }

                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    else if (!isStartDateEdited && isFinishDateEdited && !isStartTimeEdited && isFinishTimeEdited && TitleIsCompleted())
                    {
                        Date newEndDate;

                        try
                        {
                            newEndDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                                    .parse(mUpdatedFinishDay + "/" + mUpdatedFinishMonth
                                            + "/" + mUpdatedFinishYear + " " + mUpdatedFinishHour
                                            + ":" + mUpdatedFinishMinute + ":" + 00.000);

                            //newEndDate.setMonth(newEndDate.getMonth() + 1);
                            //newEndDate.setYear(newEndDate.getYear() + 1900);

                            if(CheckDates(mParsedStartDate, newEndDate))
                            {
                                mDatabase.child("Gigs/" + mGigID + "/" + "title").setValue(mGigNameEditText.getText().toString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "endDate").setValue(newEndDate);
                                mDatabase.child("Gigs/" + mGigID + "/" + "venueID").setValue(mVenueId);
                                mDatabase.child("Gigs/" + mGigID + "/" + "genres").setValue(mGenreSpinner.getSelectedItemsAsString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketCost").setValue(mTicketCostNumberPicker.getValue());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketQuantity").setValue(mTicketQuantity);
                                mDatabase.child("Gigs/" + mGigID + "/" + "ageRestriction").setValue(mAge);

                                // If the event has a calendar event update the relevant fields
                                if(mCalendarEventId != null)
                                {
                                    ContentValues values = new ContentValues();
                                    values.put(CalendarContract.Events.TITLE, mGigNameEditText.getText().toString());
                                    values.put(CalendarContract.Events.DTSTART, newEndDate.getTime());
                                    Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(mCalendarEventId));
                                    getActivity().getContentResolver().update(updateUri, values, null, null);
                                }

                                ConfirmationDialog();
                            }

                            else
                            {
                                Toast.makeText(getActivity(),
                                        "Please ensure the start dates and times are before the end date" +
                                                "and times!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    // If both the dates are edited, but neither of the times
                    else if (isStartDateEdited && isFinishDateEdited && !isStartTimeEdited && !isFinishTimeEdited && TitleIsCompleted())
                    {
                        Date newStartDateWithExistingTime;
                        Date newFinishDateWithExistingTime;

                        try
                        {
                            newStartDateWithExistingTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                                    .parse(mUpdatedStartDay + "/" + mUpdatedStartMonth
                                            + "/" + mUpdatedStartYear + " " + mParsedStartDate.getHours()
                                            + ":" + mParsedStartDate.getMinutes() + ":" + 00.000);

                            newFinishDateWithExistingTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                                    .parse(mUpdatedFinishDay + "/" + mUpdatedFinishMonth
                                            + "/" + mUpdatedFinishYear + " " + mParsedFinishDate.getHours()
                                            + ":" + mParsedFinishDate.getMinutes() + ":" + 00.000);

                            //newStartDateWithExistingTime.setMonth(newStartDateWithExistingTime.getMonth() + 1);
                            //newStartDateWithExistingTime.setYear(newStartDateWithExistingTime.getYear() + 1900);
                            //newFinishDateWithExistingTime.setMonth(newFinishDateWithExistingTime.getMonth() + 1);
                            //newFinishDateWithExistingTime.setYear(newFinishDateWithExistingTime.getYear() + 1900);


                            if(CheckDates(newStartDateWithExistingTime, newFinishDateWithExistingTime))
                            {
                                mDatabase.child("Gigs/" + mGigID + "/" + "title").setValue(mGigNameEditText.getText().toString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "startDate").setValue(newStartDateWithExistingTime);
                                mDatabase.child("Gigs/" + mGigID + "/" + "endDate").setValue(newFinishDateWithExistingTime);
                                mDatabase.child("Gigs/" + mGigID + "/" + "venueID").setValue(mVenueId);
                                mDatabase.child("Gigs/" + mGigID + "/" + "genres").setValue(mGenreSpinner.getSelectedItemsAsString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketCost").setValue(mTicketCostNumberPicker.getValue());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketQuantity").setValue(mTicketQuantity);
                                mDatabase.child("Gigs/" + mGigID + "/" + "ageRestriction").setValue(mAge);

                                // If the event has a calendar event update the relevant fields
                                if(mCalendarEventId != null)
                                {
                                    ContentValues values = new ContentValues();
                                    values.put(CalendarContract.Events.TITLE, mGigNameEditText.getText().toString());
                                    values.put(CalendarContract.Events.DTSTART, newStartDateWithExistingTime.getTime());
                                    values.put(CalendarContract.Events.DTEND, newFinishDateWithExistingTime.getTime());
                                    Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(mCalendarEventId));
                                    getActivity().getContentResolver().update(updateUri, values, null, null);
                                }

                                ConfirmationDialog();
                            }
                            else
                            {
                                Toast.makeText(getActivity(),
                                        "Please ensure the start dates and times are before the end date" +
                                                "and times!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    else if (!isStartDateEdited && !isFinishDateEdited && isStartTimeEdited && isFinishTimeEdited && TitleIsCompleted())
                    {
                        Date newStartTimeWithExistingDate;
                        Date newFinishTimeWithExistingDate;

                        try
                        {
                            newStartTimeWithExistingDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                                    .parse(mParsedStartDate.getDate() + "/" + mParsedStartDate.getMonth()
                                            + "/" + mParsedStartDate.getYear() + " " + mUpdatedStartHour
                                            + ":" + mUpdatedStartMinute + ":" + 00.000);

                            newFinishTimeWithExistingDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                                    .parse(mParsedFinishDate.getDate() + "/" + mParsedFinishDate.getMonth()
                                            + "/" + mParsedFinishDate.getYear() + " " + mUpdatedFinishHour
                                            + ":" + mUpdatedFinishMinute + ":" + 00.000);

                            newStartTimeWithExistingDate.setMonth(newStartTimeWithExistingDate.getMonth() + 1);
                            newStartTimeWithExistingDate.setYear(newStartTimeWithExistingDate.getYear() + 1900);
                            newFinishTimeWithExistingDate.setMonth(newFinishTimeWithExistingDate.getMonth() + 1);
                            newFinishTimeWithExistingDate.setYear(newFinishTimeWithExistingDate.getYear() + 1900);

                            if(CheckDates(newStartTimeWithExistingDate, newFinishTimeWithExistingDate))
                            {
                                mDatabase.child("Gigs/" + mGigID + "/" + "title").setValue(mGigNameEditText.getText().toString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "startDate").setValue(newStartTimeWithExistingDate);
                                mDatabase.child("Gigs/" + mGigID + "/" + "endDate").setValue(newFinishTimeWithExistingDate);
                                mDatabase.child("Gigs/" + mGigID + "/" + "venueID").setValue(mVenueId);
                                mDatabase.child("Gigs/" + mGigID + "/" + "genres").setValue(mGenreSpinner.getSelectedItemsAsString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketCost").setValue(mTicketCostNumberPicker.getValue());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketQuantity").setValue(mTicketQuantity);
                                mDatabase.child("Gigs/" + mGigID + "/" + "ageRestriction").setValue(mAge);

                                // If the event has a calendar event update the relevant fields
                                if(mCalendarEventId != null)
                                {
                                    ContentValues values = new ContentValues();
                                    values.put(CalendarContract.Events.TITLE, mGigNameEditText.getText().toString());
                                    values.put(CalendarContract.Events.DTSTART, newStartTimeWithExistingDate.getTime());
                                    values.put(CalendarContract.Events.DTEND, newFinishTimeWithExistingDate.getTime());
                                    Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(mCalendarEventId));
                                    getActivity().getContentResolver().update(updateUri, values, null, null);
                                }

                                ConfirmationDialog();
                            }
                            else
                            {
                                Toast.makeText(getActivity(),
                                        "Please ensure the start dates and times are before the end date" +
                                                "and times!",
                                        Toast.LENGTH_SHORT).show();
                            }

                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    else if (!isStartDateEdited && isFinishDateEdited && isStartTimeEdited && !isFinishTimeEdited && TitleIsCompleted())
                    {
                        Date newStartTimeWithExistingDate;
                        Date newFinishDateWithExistingTime;

                        try
                        {
                            newStartTimeWithExistingDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                                    .parse(mParsedStartDate.getDate() + "/" + mParsedStartDate.getMonth()
                                            + "/" + mParsedStartDate.getYear() + " " + mUpdatedStartHour
                                            + ":" + mUpdatedStartMinute + ":" + 00.000);

                            newFinishDateWithExistingTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                                    .parse(mUpdatedFinishDay + "/" + mUpdatedFinishMonth
                                            + "/" + mUpdatedFinishYear + " " + mParsedFinishDate.getHours()
                                            + ":" + mParsedFinishDate.getMinutes() + ":" + 00.000);

                            newStartTimeWithExistingDate.setMonth(newStartTimeWithExistingDate.getMonth() + 1);
                            newStartTimeWithExistingDate.setYear(newStartTimeWithExistingDate.getYear() + 1900);
                            //newFinishDateWithExistingTime.setMonth(newFinishDateWithExistingTime.getMonth() + 1);
                            //newFinishDateWithExistingTime.setYear(newFinishDateWithExistingTime.getYear() + 1900);

                            if(CheckDates(newStartTimeWithExistingDate, newFinishDateWithExistingTime))
                            {
                                mDatabase.child("Gigs/" + mGigID + "/" + "title").setValue(mGigNameEditText.getText().toString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "startDate").setValue(newStartTimeWithExistingDate);
                                mDatabase.child("Gigs/" + mGigID + "/" + "endDate").setValue(newFinishDateWithExistingTime);
                                mDatabase.child("Gigs/" + mGigID + "/" + "venueID").setValue(mVenueId);
                                mDatabase.child("Gigs/" + mGigID + "/" + "genres").setValue(mGenreSpinner.getSelectedItemsAsString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketCost").setValue(mTicketCostNumberPicker.getValue());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketQuantity").setValue(mTicketQuantity);
                                mDatabase.child("Gigs/" + mGigID + "/" + "ageRestriction").setValue(mAge);

                                // If the event has a calendar event update the relevant fields
                                if(mCalendarEventId != null)
                                {
                                    ContentValues values = new ContentValues();
                                    values.put(CalendarContract.Events.TITLE, mGigNameEditText.getText().toString());
                                    values.put(CalendarContract.Events.DTSTART, newStartTimeWithExistingDate.getTime());
                                    values.put(CalendarContract.Events.DTEND, newFinishDateWithExistingTime.getTime());
                                    Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(mCalendarEventId));
                                    getActivity().getContentResolver().update(updateUri, values, null, null);
                                }

                                ConfirmationDialog();
                            }

                            else
                            {
                                Toast.makeText(getActivity(),
                                        "Please ensure the start dates and times are before the end date" +
                                                "and times!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }

                    }

                    else if (isStartDateEdited && !isFinishDateEdited && !isStartTimeEdited && isFinishTimeEdited && TitleIsCompleted())
                    {
                        Date newStartDateWithExistingTime;
                        Date newFinishTimeWithExistingDate;

                        try
                        {
                            newStartDateWithExistingTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                                    .parse(mUpdatedStartDay + "/" + mUpdatedStartMonth
                                            + "/" + mUpdatedStartYear + " " + mParsedStartDate.getHours()
                                            + ":" + mParsedStartDate.getMinutes() + ":" + 00.000);

                            newFinishTimeWithExistingDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                                    .parse(mParsedFinishDate.getDate() + "/" + mParsedFinishDate.getMonth()
                                            + "/" + mParsedFinishDate.getYear() + " " + mUpdatedFinishHour
                                            + ":" + mUpdatedFinishMinute + ":" + 00.000);

                            //newStartDateWithExistingTime.setMonth(newStartDateWithExistingTime.getMonth() + 1);
                            //newStartDateWithExistingTime.setYear(newStartDateWithExistingTime.getYear() + 1900);
                            newFinishTimeWithExistingDate.setMonth(newFinishTimeWithExistingDate.getMonth() + 1);
                            newFinishTimeWithExistingDate.setYear(newFinishTimeWithExistingDate.getYear() + 1900);

                            if(CheckDates(newStartDateWithExistingTime, newFinishTimeWithExistingDate))
                            {
                                mDatabase.child("Gigs/" + mGigID + "/" + "title").setValue(mGigNameEditText.getText().toString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "startDate").setValue(newStartDateWithExistingTime);
                                mDatabase.child("Gigs/" + mGigID + "/" + "endDate").setValue(newFinishTimeWithExistingDate);
                                mDatabase.child("Gigs/" + mGigID + "/" + "venueID").setValue(mVenueId);
                                mDatabase.child("Gigs/" + mGigID + "/" + "genres").setValue(mGenreSpinner.getSelectedItemsAsString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketCost").setValue(mTicketCostNumberPicker.getValue());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketQuantity").setValue(mTicketQuantity);
                                mDatabase.child("Gigs/" + mGigID + "/" + "ageRestriction").setValue(mAge);

                                // If the event has a calendar event update the relevant fields
                                if(mCalendarEventId != null)
                                {
                                    ContentValues values = new ContentValues();
                                    values.put(CalendarContract.Events.TITLE, mGigNameEditText.getText().toString());
                                    values.put(CalendarContract.Events.DTSTART, newStartDateWithExistingTime.getTime());
                                    values.put(CalendarContract.Events.DTEND, newFinishTimeWithExistingDate.getTime());
                                    Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(mCalendarEventId));
                                    getActivity().getContentResolver().update(updateUri, values, null, null);
                                }

                                ConfirmationDialog();
                            }

                            else
                            {
                                Toast.makeText(getActivity(),
                                        "Please ensure the start dates and times are before the end date" +
                                                "and times!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    else if (!isStartDateEdited && !isFinishDateEdited && !isStartTimeEdited && isFinishTimeEdited && TitleIsCompleted())
                    {
                        Date newFinishTimeWithExistingDate;

                        try
                        {
                            newFinishTimeWithExistingDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                                    .parse(mParsedFinishDate.getDate() + "/" + mParsedFinishDate.getMonth()
                                            + "/" + mParsedFinishDate.getYear() + " " + mUpdatedFinishHour
                                            + ":" + mUpdatedFinishMinute + ":" + 00.000);

                            newFinishTimeWithExistingDate.setMonth(newFinishTimeWithExistingDate.getMonth() + 1);
                            newFinishTimeWithExistingDate.setYear(newFinishTimeWithExistingDate.getYear() + 1900);

                            if(CheckDates(mParsedStartDate, newFinishTimeWithExistingDate))
                            {
                                mDatabase.child("Gigs/" + mGigID + "/" + "title").setValue(mGigNameEditText.getText().toString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "endDate").setValue(newFinishTimeWithExistingDate);
                                mDatabase.child("Gigs/" + mGigID + "/" + "venueID").setValue(mVenueId);
                                mDatabase.child("Gigs/" + mGigID + "/" + "genres").setValue(mGenreSpinner.getSelectedItemsAsString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketCost").setValue(mTicketCostNumberPicker.getValue());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketQuantity").setValue(mTicketQuantity);
                                mDatabase.child("Gigs/" + mGigID + "/" + "ageRestriction").setValue(mAge);

                                // If the event has a calendar event update the relevant fields
                                if(mCalendarEventId != null)
                                {
                                    ContentValues values = new ContentValues();
                                    values.put(CalendarContract.Events.TITLE, mGigNameEditText.getText().toString());
                                    values.put(CalendarContract.Events.DTEND, newFinishTimeWithExistingDate.getTime());
                                    Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(mCalendarEventId));
                                    getActivity().getContentResolver().update(updateUri, values, null, null);
                                }

                                ConfirmationDialog();
                            }

                            else
                            {
                                Toast.makeText(getActivity(),
                                        "Please ensure the start dates and times are before the end date" +
                                                "and times!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    else if (!isStartDateEdited && !isFinishDateEdited && isStartTimeEdited && !isFinishTimeEdited && TitleIsCompleted())
                    {
                        Date newStartTimeWithExistingDate;

                        try
                        {
                            newStartTimeWithExistingDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                                    .parse(mParsedStartDate.getDate() + "/" + mParsedStartDate.getMonth()
                                            + "/" + mParsedStartDate.getYear() + " " + mUpdatedStartHour
                                            + ":" + mUpdatedStartMinute + ":" + 00.000);

                            newStartTimeWithExistingDate.setMonth(newStartTimeWithExistingDate.getMonth() + 1);
                            newStartTimeWithExistingDate.setYear(newStartTimeWithExistingDate.getYear() + 1900);

                            if(CheckDates(newStartTimeWithExistingDate, mParsedFinishDate))
                            {
                                mDatabase.child("Gigs/" + mGigID + "/" + "title").setValue(mGigNameEditText.getText().toString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "startDate").setValue(newStartTimeWithExistingDate);
                                mDatabase.child("Gigs/" + mGigID + "/" + "venueID").setValue(mVenueId);
                                mDatabase.child("Gigs/" + mGigID + "/" + "genres").setValue(mGenreSpinner.getSelectedItemsAsString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketCost").setValue(mTicketCostNumberPicker.getValue());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketQuantity").setValue(mTicketQuantity);
                                mDatabase.child("Gigs/" + mGigID + "/" + "ageRestriction").setValue(mAge);

                                // If the event has a calendar event update the relevant fields
                                if(mCalendarEventId != null)
                                {
                                    ContentValues values = new ContentValues();
                                    values.put(CalendarContract.Events.TITLE, mGigNameEditText.getText().toString());
                                    values.put(CalendarContract.Events.DTEND, newStartTimeWithExistingDate.getTime());
                                    Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(mCalendarEventId));
                                    getActivity().getContentResolver().update(updateUri, values, null, null);
                                }

                                ConfirmationDialog();
                            }
                            else
                            {
                                Toast.makeText(getActivity(),
                                        "Please ensure the start dates and times are before the end date" +
                                                "and times!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    else if (!isStartDateEdited && isFinishDateEdited && !isStartTimeEdited && !isFinishTimeEdited && TitleIsCompleted())
                    {
                        Date newFinishDateWithExistingTime;

                        try
                        {
                            newFinishDateWithExistingTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                                    .parse(mUpdatedFinishDay + "/" + mUpdatedFinishMonth
                                            + "/" + mUpdatedFinishYear + " " + mParsedFinishDate.getHours()
                                            + ":" + mParsedFinishDate.getMinutes() + ":" + 00.000);

                            //newFinishDateWithExistingTime.setMonth(newFinishDateWithExistingTime.getMonth() + 1);
                            //newFinishDateWithExistingTime.setYear(newFinishDateWithExistingTime.getYear() + 1900);

                            if(CheckDates(mParsedStartDate, newFinishDateWithExistingTime))
                            {
                                mDatabase.child("Gigs/" + mGigID + "/" + "title").setValue(mGigNameEditText.getText().toString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "endDate").setValue(newFinishDateWithExistingTime);
                                mDatabase.child("Gigs/" + mGigID + "/" + "venueID").setValue(mVenueId);
                                mDatabase.child("Gigs/" + mGigID + "/" + "genres").setValue(mGenreSpinner.getSelectedItemsAsString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketCost").setValue(mTicketCostNumberPicker.getValue());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketQuantity").setValue(mTicketQuantity);
                                mDatabase.child("Gigs/" + mGigID + "/" + "ageRestriction").setValue(mAge);

                                // If the event has a calendar event update the relevant fields
                                if(mCalendarEventId != null)
                                {
                                    ContentValues values = new ContentValues();
                                    values.put(CalendarContract.Events.TITLE, mGigNameEditText.getText().toString());
                                    values.put(CalendarContract.Events.DTEND, newFinishDateWithExistingTime.getTime());
                                    Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(mCalendarEventId));
                                    getActivity().getContentResolver().update(updateUri, values, null, null);
                                }

                                ConfirmationDialog();
                            }
                            else
                            {
                                Toast.makeText(getActivity(),
                                        "Please ensure the start dates and times are before the end date" +
                                                "and times!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }

                    }

                    else if (isStartDateEdited && !isFinishDateEdited && !isStartTimeEdited && !isFinishTimeEdited && TitleIsCompleted())
                    {
                        Date newStartDateWithExistingTime;

                        try
                        {
                            newStartDateWithExistingTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
                                    .parse(mUpdatedStartDay + "/" + mUpdatedStartMonth
                                            + "/" + mUpdatedStartYear + " " + mParsedStartDate.getHours()
                                            + ":" + mParsedStartDate.getMinutes() + ":" + 00.000);

                            //newStartDateWithExistingTime.setMonth(newStartDateWithExistingTime.getMonth() + 1);
                            //newStartDateWithExistingTime.setYear(newStartDateWithExistingTime.getYear() + 1900);

                            if(CheckDates(newStartDateWithExistingTime, mParsedFinishDate))
                            {
                                mDatabase.child("Gigs/" + mGigID + "/" + "title").setValue(mGigNameEditText.getText().toString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "startDate").setValue(newStartDateWithExistingTime);
                                mDatabase.child("Gigs/" + mGigID + "/" + "venueID").setValue(mVenueId);
                                mDatabase.child("Gigs/" + mGigID + "/" + "genres").setValue(mGenreSpinner.getSelectedItemsAsString());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketCost").setValue(mTicketCostNumberPicker.getValue());
                                mDatabase.child("Gigs/" + mGigID + "/" + "ticketQuantity").setValue(mTicketQuantity);
                                mDatabase.child("Gigs/" + mGigID + "/" + "ageRestriction").setValue(mAge);

                                // If the event has a calendar event update the relevant fields
                                if(mCalendarEventId != null)
                                {
                                    ContentValues values = new ContentValues();
                                    values.put(CalendarContract.Events.TITLE, mGigNameEditText.getText().toString());
                                    values.put(CalendarContract.Events.DTSTART, newStartDateWithExistingTime.getTime());
                                    Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(mCalendarEventId));
                                    getActivity().getContentResolver().update(updateUri, values, null, null);
                                }

                                ConfirmationDialog();
                            }
                            else
                            {
                                Toast.makeText(getActivity(),
                                        "Please ensure the start dates and times are before the end date" +
                                                "and times!",
                                        Toast.LENGTH_SHORT).show();
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

    // This method checks that the title has been completed, and returns a boolean to that effect
    private boolean TitleIsCompleted()
    {
        boolean isTitleCompleted = true;

        if(mGigNameEditText.getText().toString().matches(""))
        {
            Toast.makeText(getActivity(),
                    "Please ensure you have given a value for gig name!",
                    Toast.LENGTH_SHORT).show();

            isTitleCompleted = false;
        }

        return isTitleCompleted;
    }

    private boolean CheckDates(Date startDate, Date endDate)
    {
        if(startDate.getTime() < endDate.getTime())
        {
            goodDates = true;
        }

        return goodDates;
    }

    // Android seems to think this method is required even
    // though it's implemented elsewhere. If it's removed it
    // throws an error so it's been left in for that purpose.
    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1)
    {
    }

    // This method allows the user to delete gigs from the database and from the google calendar
    private void DeleteGig()
    {
        // A dialog is shown to alert the user that they are about to delete a gig
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Gig");
        builder.setMessage("Are you sure you want to delete this gig?");
        builder.setIcon(R.drawable.ic_event_available_black_24px);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                mDatabase.child("Gigs/" + mGigID).removeValue();

                // If the event has a calendar event ensure it's deleted from the calendar
                if(mCalendarEventId != null)
                {
                    Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(mCalendarEventId));
                    getActivity().getContentResolver().delete(deleteUri, null, null);
                }

                // A dialog is then shown to alert the user that the changes have been made
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Confirmation");
                builder.setMessage("Gig Deleted!");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        // The user is then taken to the my gigs fragment
                        ReturnHome();
                    }
                });
                builder.setCancelable(false);
                builder.show();
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

    // This method removes the act booked for a gig
    private void RemoveAct()
    {
        // A dialog is shown to alert the user that they are about to delete a gig
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Remove Act");
        builder.setMessage("Are you sure you want to remove this act from your gig?");
        builder.setIcon(R.drawable.ic_info_outline_black_24px);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                mDatabase.child("Gigs/" + mGigID + "/bookedAct").setValue("Vacant");

                // A dialog is then shown to alert the user that the changes have been made
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Confirmation");
                builder.setMessage("Act Removed!");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        // The fragment is then refreshed
                        RefreshFragment();
                    }
                });
                builder.setCancelable(false);
                builder.show();
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
    private void ConfirmationDialog()
    {
        // A dialog is then shown to alert the user that the changes have been made
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirmation");
        builder.setMessage("Gig Updated!");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                // The user is then taken to the my gigs fragment
                ReturnHome();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void RefreshFragment()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

    private void ReturnHome()
    {
        getActivity().finish();
        getActivity().overridePendingTransition(0,0);

        Intent intent = new Intent(getActivity(), VenueUserMainActivity.class);
        startActivity(intent);

        getFragmentManager().popBackStackImmediate();
    }
}
