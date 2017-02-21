package com.liamd.giggity_app;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by liamd on 21/02/2017.
 */

public class MusicianUser extends User
{
    private String homeAddress;
    private LatLng homeLocation;

    public MusicianUser()
    {

    }

    public MusicianUser(String homeAddress, LatLng homeLocation)
    {
        this.homeAddress = homeAddress;
        this.homeLocation = homeLocation;
    }

    public MusicianUser(String email, String firstName, String lastName, String password, String userID, Boolean hasLoggedIn, String homeAddress, LatLng homeLocation)
    {
        super(email, firstName, lastName, password, userID, hasLoggedIn);
        this.homeAddress = homeAddress;
        this.homeLocation = homeLocation;
    }

    public String getHomeAddress()
    {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress)
    {
        this.homeAddress = homeAddress;
    }

    public LatLng getHomeLocation()
    {
        return homeLocation;
    }

    public void setHomeLocation(LatLng homeLocation)
    {
        this.homeLocation = homeLocation;
    }
}
