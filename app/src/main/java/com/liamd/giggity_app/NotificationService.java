package com.liamd.giggity_app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by liamd on 08/04/2017.
 */

public class NotificationService extends Service
{
    // Declare firebase variables
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to Firebase's authentication
        mAuth = FirebaseAuth.getInstance();

        // If there are any notifications at this node fire them for the relevant user's device
        mDatabase.child("Notifications/MusicianSentBandRequestsPending/").addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                // If there are any notifications these sent and then deleted after
                if(dataSnapshot.getValue() != null && mAuth.getCurrentUser() != null && dataSnapshot.getKey().equals(mAuth.getCurrentUser().getUid()))
                {
                    SendNotification("A user has requested to join your band!", "Click here to view details");
                    mDatabase.child("Notifications/MusicianSentBandRequestsPending/" + mAuth.getCurrentUser().getUid()).removeValue();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s)
            {

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        // If there are any notifications at this node fire them for the relevant user's device
        mDatabase.child("Notifications/MusicianSentBandRequestsAccepted/" + mAuth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                // If there are any notifications these sent and then deleted after
                if(dataSnapshot.getValue() != null && mAuth.getCurrentUser() != null)
                {
                    SendNotification("Your request to join a band has been accepted!", "Click here to view details");
                    mDatabase.child("Notifications/MusicianSentBandRequestsAccepted/" + mAuth.getCurrentUser().getUid()).removeValue();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s)
            {

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return super.onStartCommand(intent, flags, startId);
    }

    // When the application is closed notifications can still be received
    public void onDestroy()
    {
        Intent intent = new Intent("com.liamd.giggity_app");
        intent.putExtra("yourvalue", "torestore");
        sendBroadcast(intent);
    }

    // This method creates a notification and displays it
    public void SendNotification(String title, String text)
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("RSSPullService");

        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(""));
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Context context = getApplicationContext();

        Notification.Builder builder;

        builder = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_info_outline_black_24dp);

        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}