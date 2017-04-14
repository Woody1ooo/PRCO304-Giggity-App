package com.liamd.giggity_app;

import java.util.Date;

/**
 * Created by liamd on 02/04/2017.
 */

public class GigRequest
{
    private String bandID;
    private String bandName;
    private String gigID;
    private String gigName;
    private String requestStatus;
    private String venueID;
    private String venueName;
    private Date startDate;
    private Date endDate;

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

    public String getBandName()
    {
        return bandName;
    }

    public void setBandName(String bandName)
    {
        this.bandName = bandName;
    }

    public String getVenueName()
    {
        return venueName;
    }

    public void setVenueName(String venueName)
    {
        this.venueName = venueName;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    public String getGigName()
    {
        return gigName;
    }

    public void setGigName(String gigName)
    {
        this.gigName = gigName;
    }
}
