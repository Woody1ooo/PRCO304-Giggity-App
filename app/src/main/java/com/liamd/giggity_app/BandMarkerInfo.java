package com.liamd.giggity_app;

import android.location.Location;

/**
 * Created by liamd on 25/03/2017.
 */

public class BandMarkerInfo
{
    private String markerId;
    private String bandId;
    private String bandName;
    private String bandGenres;
    private String numberOfPositions;
    private double bandDistance;
    private Location bandLocation;
    private Location venueLocation;

    public BandMarkerInfo(String markerId)
    {
        this.markerId = markerId;
    }

    public BandMarkerInfo(String markerId, String bandId, String bandName, String bandGenres, double bandDistance)
    {
        this.markerId = markerId;
        this.bandId = bandId;
        this.bandName = bandName;
        this.bandGenres = bandGenres;
        this.bandDistance = bandDistance;
    }

    public BandMarkerInfo(String markerId, String bandId, String bandName, String bandGenres, String numberOfPositions, double bandDistance)
    {
        this.markerId = markerId;
        this.bandId = bandId;
        this.bandName = bandName;
        this.bandGenres = bandGenres;
        this.numberOfPositions = numberOfPositions;
        this.bandDistance = bandDistance;
    }

    public BandMarkerInfo(String markerId, String bandId, String bandName, String bandGenres, double bandDistance, Location bandLocation, Location venueLocation)
    {
        this.markerId = markerId;
        this.bandId = bandId;
        this.bandName = bandName;
        this.bandGenres = bandGenres;
        this.bandDistance = bandDistance;
        this.bandLocation = bandLocation;
        this.venueLocation = venueLocation;
    }

    public String getMarkerId()
    {
        return markerId;
    }

    public void setMarkerId(String markerId)
    {
        this.markerId = markerId;
    }

    public String getBandId()
    {
        return bandId;
    }

    public void setBandId(String bandId)
    {
        this.bandId = bandId;
    }

    public String getBandName()
    {
        return bandName;
    }

    public void setBandName(String bandName)
    {
        this.bandName = bandName;
    }

    public String getBandGenres()
    {
        return bandGenres;
    }

    public void setBandGenres(String bandGenres)
    {
        this.bandGenres = bandGenres;
    }

    public String getNumberOfPositions()
    {
        return numberOfPositions;
    }

    public void setNumberOfPositions(String numberOfPositions)
    {
        this.numberOfPositions = numberOfPositions;
    }

    public double getBandDistance()
    {
        return bandDistance;
    }

    public void setBandDistance(double bandDistance)
    {
        this.bandDistance = bandDistance;
    }

    public Location getBandLocation()
    {
        return bandLocation;
    }

    public void setBandLocation(Location bandLocation)
    {
        this.bandLocation = bandLocation;
    }

    public Location getVenueLocation()
    {
        return venueLocation;
    }

    public void setVenueLocation(Location venueLocation)
    {
        this.venueLocation = venueLocation;
    }
}
