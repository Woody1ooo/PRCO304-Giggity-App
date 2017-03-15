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
    private String gigId;
    private double gigDistance;

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

    public Gig(Date endDate, Date startDate, String title, String venueID, String gigId)
    {
        this.endDate = endDate;
        this.startDate = startDate;
        this.title = title;
        this.venueID = venueID;
        this.gigId = gigId;
    }

    public Gig(Date endDate, Date startDate, String title, String venueID, String gigId, double gigDistance)
    {
        this.endDate = endDate;
        this.startDate = startDate;
        this.title = title;
        this.venueID = venueID;
        this.gigId = gigId;
        this.gigDistance = gigDistance;
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

    public String getGigId()
    {
        return gigId;
    }

    public void setGigId(String gigId)
    {
        this.gigId = gigId;
    }

    public double getGigDistance()
    {
        return gigDistance;
    }

    public void setGigDistance(double gigDistance)
    {
        this.gigDistance = gigDistance;
    }
}
