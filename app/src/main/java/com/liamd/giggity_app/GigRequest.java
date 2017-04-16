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
    private Date gigStartDate;
    private Date gigEndDate;

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

    public GigRequest(String bandID, String bandName, String gigID, String gigName, String requestStatus, String venueID, String venueName, Date startDate, Date gigEndDate)
    {
        this.bandID = bandID;
        this.bandName = bandName;
        this.gigID = gigID;
        this.gigName = gigName;
        this.requestStatus = requestStatus;
        this.venueID = venueID;
        this.venueName = venueName;
        this.gigStartDate = startDate;
        this.gigEndDate = gigEndDate;
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

    public Date getGigStartDate()
    {
        return gigStartDate;
    }

    public void setGigStartDate(Date startDate)
    {
        this.gigStartDate = startDate;
    }

    public Date getGigEndDate()
    {
        return gigEndDate;
    }

    public void setGigEndDate(Date gigEndDate)
    {
        this.gigEndDate = gigEndDate;
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
