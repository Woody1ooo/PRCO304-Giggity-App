package com.liamd.giggity_app;

/**
 * Created by liamd on 06/05/2017.
 */

public class Notification
{
    private String notificationID;
    private String notificationMessage;
    private String notificationType;

    public Notification()
    {

    }

    public Notification(String notificationID, String notificationMessage, String notificationType)
    {
        this.notificationID = notificationID;
        this.notificationMessage = notificationMessage;
        this.notificationType = notificationType;
    }

    public String getNotificationID()
    {
        return notificationID;
    }

    public void setNotificationID(String notificationID)
    {
        this.notificationID = notificationID;
    }

    public String getNotificationMessage()
    {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage)
    {
        this.notificationMessage = notificationMessage;
    }

    public String getNotificationType()
    {
        return notificationType;
    }

    public void setNotificationType(String notificationType)
    {
        this.notificationType = notificationType;
    }
}
