package com.liamd.giggity_app;

import android.location.Location;

/**
 * Created by liamd on 05/04/2017.
 */

public class UserMarkerInfo
{
    private String markerId;
    private String userId;
    private String userName;
    private String userGenres;
    private String userInstruments;
    private double userDistance;
    private Location userLocation;
    private Location BandLocation;

    public UserMarkerInfo(String markerId)
    {
        this.markerId = markerId;
    }

    public UserMarkerInfo(String markerId, String userId, String userName, String userGenres, String userInstruments, double userDistance, Location userLocation, Location bandLocation)
    {
        this.markerId = markerId;
        this.userId = userId;
        this.userName = userName;
        this.userGenres = userGenres;
        this.userInstruments = userInstruments;
        this.userDistance = userDistance;
        this.userLocation = userLocation;
        BandLocation = bandLocation;
    }

    public String getMarkerId()
    {
        return markerId;
    }

    public void setMarkerId(String markerId)
    {
        this.markerId = markerId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getUserGenres()
    {
        return userGenres;
    }

    public void setUserGenres(String userGenres)
    {
        this.userGenres = userGenres;
    }

    public String getUserInstruments()
    {
        return userInstruments;
    }

    public void setUserInstruments(String userInstruments)
    {
        this.userInstruments = userInstruments;
    }

    public double getUserDistance()
    {
        return userDistance;
    }

    public void setUserDistance(double userDistance)
    {
        this.userDistance = userDistance;
    }

    public Location getUserLocation()
    {
        return userLocation;
    }

    public void setUserLocation(Location userLocation)
    {
        this.userLocation = userLocation;
    }

    public Location getBandLocation()
    {
        return BandLocation;
    }

    public void setBandLocation(Location bandLocation)
    {
        BandLocation = bandLocation;
    }
}
