package com.liamd.giggity_app;

/**
 * Created by liamd on 20/02/2017.
 */

public class Venue
{
    private String name;
    private String venueID;
    private String userID;

    public Venue()
    {

    }

    public Venue(String name, String venueID, String userID)
    {
        this.name = name;
        this.venueID = venueID;
        this.userID = userID;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getVenueID()
    {
        return venueID;
    }

    public void setVenueID(String venueID)
    {
        this.venueID = venueID;
    }

    public String getUserID()
    {
        return userID;
    }

    public void setUserID(String userID)
    {
        this.userID = userID;
    }

}
