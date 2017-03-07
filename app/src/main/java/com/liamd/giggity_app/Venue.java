package com.liamd.giggity_app;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by liamd on 20/02/2017.
 */

public class Venue
{
    private String name;
    private String venueID;
    private String userID;
    private com.liamd.giggity_app.LatLng venueLocation;

    public Venue()
    {

    }

    public Venue(String name, String venueID, String userID, com.liamd.giggity_app.LatLng venueLocation)
    {
        this.name = name;
        this.venueID = venueID;
        this.userID = userID;
        this.venueLocation = venueLocation;
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

    public com.liamd.giggity_app.LatLng getVenueLocation()
    {
        return venueLocation;
    }

    public void setVenueLocation(com.liamd.giggity_app.LatLng venueLocation)
    {
        this.venueLocation = venueLocation;
    }
}
