package com.liamd.giggity_app;

import java.util.Date;

/**
 * Created by liamd on 24/02/2017.
 */

public class Gig
{
    private String title;
    private String venueName;
    private String venueID;
    private Date startDate;
    private Date endDate;

    public Gig()
    {

    }

    public Gig(Date endDate, Date startDate, String title, String venueID, String venueName)
    {
        this.endDate = endDate;
        this.startDate = startDate;
        this.title = title;
        this.venueID = venueID;
        this.venueName = venueName;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getVenueID()
    {
        return venueID;
    }

    public void setVenueID(String venueID)
    {
        this.venueID = venueID;
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
