package com.liamd.giggity_app;

/**
 * Created by liamd on 24/04/2017.
 */

public class NewsFeedItem
{
    private String userName;
    private String message;
    private String gigId;
    private String bandId;

    public NewsFeedItem()
    {

    }

    public NewsFeedItem(String userName, String message, String gigId, String bandId)
    {
        this.userName = userName;
        this.message = message;
        this.gigId = gigId;
        this.bandId = bandId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getGigId()
    {
        return gigId;
    }

    public void setGigId(String gigId)
    {
        this.gigId = gigId;
    }

    public String getBandId()
    {
        return bandId;
    }

    public void setBandId(String bandId)
    {
        this.bandId = bandId;
    }
}
