package com.liamd.giggity_app;

/**
 * Created by liamd on 02/04/2017.
 */

public class GigRequest
{
    private String bandID;
    private String gigID;
    private String requestStatus;

    public GigRequest()
    {

    }

    public GigRequest(String bandID, String gigID, String requestStatus)
    {
        this.bandID = bandID;
        this.gigID = gigID;
        this.requestStatus = requestStatus;
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
}
