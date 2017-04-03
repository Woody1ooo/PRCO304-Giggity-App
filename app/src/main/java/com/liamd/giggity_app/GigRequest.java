package com.liamd.giggity_app;

/**
 * Created by liamd on 02/04/2017.
 */

public class GigRequest
{
    private String bandID;
    private String gigID;
    private String requestStatus;
    private String venueID;

    public GigRequest()
    {

    }

    public GigRequest(String bandID, String gigID, String requestStatus, String venueID)
    {
        this.bandID = bandID;
        this.gigID = gigID;
        this.requestStatus = requestStatus;
        this.venueID = venueID;
    }

    public String getBandID()
    {
        return bandID;
    }

    public void setBandID(String bandID)
    {
        this.bandID = bandID;
    }

    public String getGigID()
    {
        return gigID;
    }

    public void setGigID(String gigID)
    {
        this.gigID = gigID;
    }

    public String getRequestStatus()
    {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus)
    {
        this.requestStatus = requestStatus;
    }

    public String getVenueID()
    {
        return venueID;
    }

    public void setVenueID(String venueID)
    {
        this.venueID = venueID;
    }
}
