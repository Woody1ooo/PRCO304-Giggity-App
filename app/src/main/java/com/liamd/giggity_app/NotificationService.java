package com.liamd.giggity_app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        if(mAuth.getCurrentUser() != null)
        {
            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/notifications").addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if (mAuth.getCurrentUser() != null)
                    {
                        int notificationTypeId = 0;

                        Iterable<DataSnapshot> notifications = dataSnapshot.getChildren();
                        for (DataSnapshot child : notifications)
                        {
                            com.liamd.giggity_app.Notification notification;
                            notification = child.getValue(com.liamd.giggity_app.Notification.class);

                            // Only trigger the notification if the notification user ID matches the currently logged in user
                            if(notification.getNotificationUserID().equals(mAuth.getCurrentUser().getUid()))
                            {
                                switch (notification.getNotificationType())
                                {
                                    case "MusicianSentBandRequestPending":
                                        notificationTypeId = 0;
                                        break;
                                    case "MusicianSentBandRequestAccepted":
                                        notificationTypeId = 1;
                                        break;
                                    case "MusicianSentBandRequestRejected":
                                        notificationTypeId = 2;
                                        break;
                                    case "BandSentMusicianRequestPending":
                                        notificationTypeId = 3;
                                        break;
                                    case "BandSentMusicianRequestAccepted":
                                        notificationTypeId = 4;
                                        break;
                                    case "BandSentMusicianRequestRejected":
                                        notificationTypeId = 5;
                                        break;
                                    case "BandSentGigRequestPending":
                                        notificationTypeId = 6;
                                        break;
                                    case "BandSentGigRequestAccepted":
                                        notificationTypeId = 7;
                                        break;
                                    case "BandSentGigRequestRejected":
                                        notificationTypeId = 8;
                                        break;
                                    case "VenueSentGigRequestPending":
                                        notificationTypeId = 9;
                                        break;
                                    case "VenueSentGigRequestAccepted":
                                        notificationTypeId = 10;
                                        break;
                                    case "VenueSentGigRequestRejected":
                                        notificationTypeId = 11;
                                        break;
                                }

                                SendNotification(notification.getNotificationMessage(), "Click here to view details", notificationTypeId);
                                mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/notifications/" + notification.getNotificationID()).removeValue();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }
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
        sendBroadcast(intent);
    }

    // This method creates a notification and displays it
    public void SendNotification(String title, String text, int notificationTypeId)
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
                .setSmallIcon(R.drawable.ic_info_outline_black_24px);

        if(notificationTypeId == 0)
        {
            Intent notificationIntent = new Intent(this, MusicianUserMainActivity.class);
            notificationIntent.putExtra("FragmentToOpenExtra", "MusicianUserBandRequestsInBandFragment");
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);
        }

        else if(notificationTypeId == 1)
        {
            Intent notificationIntent = new Intent(this, MusicianUserMainActivity.class);
            notificationIntent.putExtra("FragmentToOpenExtra", "MusicianUserBandMembersFragment");
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);
        }

        else if(notificationTypeId == 2)
        {
            Intent notificationIntent = new Intent(this, MusicianUserMainActivity.class);
            notificationIntent.putExtra("FragmentToOpenExtra", "MusicianUserBandRequestsInBandFragment");
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);
        }

        else if(notificationTypeId == 3)
        {
            Intent notificationIntent = new Intent(this, MusicianUserMainActivity.class);
            notificationIntent.putExtra("FragmentToOpenExtra", "MusicianUserBandRequestsNotInBandFragment");
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);
        }

        else if(notificationTypeId == 4)
        {
            Intent notificationIntent = new Intent(this, MusicianUserMainActivity.class);
            notificationIntent.putExtra("FragmentToOpenExtra", "MusicianUserBandRequestsNotInBandFragment");
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);
        }

        else if(notificationTypeId == 5)
        {
            Intent notificationIntent = new Intent(this, MusicianUserMainActivity.class);
            notificationIntent.putExtra("FragmentToOpenExtra", "MusicianUserBandRequestsNotInBandFragment");
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);
        }

        else if(notificationTypeId == 6)
        {
            Intent notificationIntent = new Intent(this, VenueUserMainActivity.class);
            notificationIntent.putExtra("FragmentToOpenExtra", "VenueUserGigRequestsFragment");
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);
        }

        else if(notificationTypeId == 7)
        {
            Intent notificationIntent = new Intent(this, MusicianUserMainActivity.class);
            notificationIntent.putExtra("FragmentToOpenExtra", "MusicianUserGigRequestsFragment");
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);
        }

        else if(notificationTypeId == 8)
        {
            Intent notificationIntent = new Intent(this, MusicianUserMainActivity.class);
            notificationIntent.putExtra("FragmentToOpenExtra", "MusicianUserGigRequestsFragment");
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);
        }

        else if(notificationTypeId == 9)
        {
            Intent notificationIntent = new Intent(this, MusicianUserMainActivity.class);
            notificationIntent.putExtra("FragmentToOpenExtra", "MusicianUserGigRequestsFragment");
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);
        }

        else if(notificationTypeId == 10)
        {
            Intent notificationIntent = new Intent(this, VenueUserMainActivity.class);
            notificationIntent.putExtra("FragmentToOpenExtra", "VenueUserGigRequestsFragment");
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);
        }

        else if(notificationTypeId == 11)
        {
            Intent notificationIntent = new Intent(this, VenueUserMainActivity.class);
            notificationIntent.putExtra("FragmentToOpenExtra", "VenueUserGigRequestsFragment");
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);
        }

        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationTypeId, notification);
    }
}