package com.liamd.giggity_app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class VenueUserTicketScannerFragment extends Fragment
{
    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Declare general variables
    private ArrayList<Ticket> mListOfTickets = new ArrayList<>();
    private String mVenueId;

    public VenueUserTicketScannerFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.venue_user_fragment_ticket_scanner, container, false);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialise visual components
        FrameLayout layout = (FrameLayout) fragmentView.findViewById(R.id.layout);

        // If the layout is clicked load the scanner
        layout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LoadQRScanner();
            }
        });

        // This method loads the barcode scanner
        LoadQRScanner();

        getActivity().setTitle("Ticket Scanner");

        return fragmentView;
    }

    // When called it means the scanner has detected something
    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null)
        {
            // If there is no data in the barcode display a message to this effect
            if (result.getContents() == null)
            {
                Toast.makeText(getActivity(), "No Barcode Scanned!", Toast.LENGTH_LONG).show();
            }

            // If there is data process it as follows
            else
            {
                try
                {
                    // This splits the data around the line breaks thus separating it into individual components
                    String splitResult[] = result.getContents().split("\\n");
                    final String ticketIdFromScan = splitResult[0];
                    final int quantityFromScan = Integer.parseInt(splitResult[1]);
                    final String gigIdFromScan = splitResult[2];
                    final String[] userId = {""};
                    final String gigName = splitResult[3];

                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            // Loop through the snapshot using the gig id scanned and take the key as the user id
                            Iterable<DataSnapshot> children = dataSnapshot.child("Tickets/" + gigIdFromScan).getChildren();
                            for (DataSnapshot child : children)
                            {
                                userId[0] = child.getKey();

                                // Add the ticket to the list of tickets
                                Ticket ticket;
                                ticket = child.getValue(Ticket.class);
                                mListOfTickets.add(ticket);
                            }

                            // Get the logged in user's venue id to check that the ticket they are confirming is one at their own venue
                            mVenueId = dataSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/venueID").getValue().toString();

                            // For each ticket in list of tickets check that the gig id, ticket id, quantity, and venue Id all match
                            for (Ticket databaseTicket : mListOfTickets)
                            {
                                if (databaseTicket.getGigID().equals(gigIdFromScan)
                                        && databaseTicket.getTicketID().equals(ticketIdFromScan)
                                        && databaseTicket.getAdmissionQuantity() == quantityFromScan
                                        && dataSnapshot.child("Gigs/" + databaseTicket.getGigID() + "/venueID").getValue().equals(mVenueId))
                                {
                                    if(databaseTicket.getTicketStatus().equals("Valid"))
                                    {
                                        // If this is the case show it's a valid ticket and ask the user to confirm
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setTitle("Valid Ticket!");
                                        builder.setMessage("This ticket grants " + quantityFromScan + " person(s) entry to the following gig: \n\n" + gigName +
                                                "\n\nAre you sure you wish to confirm this ticket?");
                                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
                                        {
                                            // If the user confirms this the ticket is removed from the database to prevent it from being used multiple times
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i)
                                            {
                                                mDatabase.child("Tickets/" + gigIdFromScan + "/" + userId[0] + "/ticketStatus").setValue("Redeemed");
                                                mListOfTickets.clear();

                                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                builder.setTitle("Confirmation");
                                                builder.setMessage("Ticket Processed!");
                                                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i)
                                                    {
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
                                    else
                                    {
                                        Toast.makeText(getActivity(), "Error! This ticket has already been scanned.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                else
                                {
                                    Toast.makeText(getActivity(), "Something went wrong! Are you sure this gig is at your venue?", Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
                }
                catch(IndexOutOfBoundsException e)
                {
                    System.out.println(e);
                    Toast.makeText(getActivity(), "Something went wrong! Are you sure this is a valid Giggity ticket?", Toast.LENGTH_LONG).show();
                }
            }
        }

        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void LoadQRScanner()
    {
        // Because I am using a fragment this line is required to initiate the scan otherwise the onActivityResult method is not called
        IntentIntegrator.forFragment(this).initiateScan();
    }
}
