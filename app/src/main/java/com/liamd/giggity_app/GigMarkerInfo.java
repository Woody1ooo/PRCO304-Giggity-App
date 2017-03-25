package com.liamd.giggity_app;

import java.util.Date;

/**
 * Created by liamd on 11/03/2017.
 */

public class GigMarkerInfo
{
    private String markerId;
    private String gigId;
    private String gigName;
    private String venueName;
    private String venueId;
    private Date gigStartDate;
    private Date gigEndDate;

    public GigMarkerInfo(String markerId)
    {
        this.markerId = markerId;
    }

    public GigMarkerInfo(Date gigEndDate, String gigId, String gigName, Date gigStartDate, String markerId, String venueId, String venueName)
    {
        this.gigEndDate = gigEndDate;
        this.gigId = gigId;
        this.gigName = gigName;
        this.gigStartDate = gigStartDate;
        this.markerId = markerId;
        this.venueId = venueId;
        this.venueName = venueName;
    }

    public Date getGigEndDate()
    {
        return gigEndDate;
    }

    public void setGigEndDate(Date gigEndDate)
    {
        this.gigEndDate = gigEndDate;
    }

    public String getGigId()
    {
        return gigId;
    }

    public void setGigId(String gigId)
    {
        this.gigId = gigId;
    }

    public String getGigName()
    {
        return gigName;
    }

    public void setGigName(String gigName)
    {
        this.gigName = gigName;
    }

    public Date getGigStartDate()
    {
        return gigStartDate;
    }

    public void setGigStartDate(Date gigStartDate)
    {
        this.gigStartDate = gigStartDate;
    }

    public String getMarkerId()
    {
        return markerId;
    }

    public void setMarkerId(String markerId)
    {
        this.markerId = markerId;
    }

    public String getVenueId()
    {
        return venueId;
    }

    public void setVenueId(String venueId)
    {
        this.venueId = venueId;
    }

    public String getVenueName()
    {
        return venueName;
    }

    public void setVenueName(String venueName)
    {
        this.venueName = venueName;
    }
}



