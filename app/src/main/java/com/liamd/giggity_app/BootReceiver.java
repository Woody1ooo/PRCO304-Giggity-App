package com.liamd.giggity_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by liamd on 08/04/2017.
 */

public class BootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        // When the device is booted the notification service is started
        context.startService(new Intent(context, NotificationService.class));
    }
}
