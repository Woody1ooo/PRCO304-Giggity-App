package com.liamd.giggity_app;

import java.util.Date;

/**
 * Created by liamd on 24/02/2017.
 */

public class Gig
{
    private String title;
    private String venueID;
    private Date startDate;
    private Date endDate;

    public Gig()
    {

    }

    public Gig(Date endDate, Date startDate, String title, String venueID)
    {
        this.endDate = endDate;
        this.startDate = startDate;
        this.title = title;
        this.venueID = venueID;
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
}
