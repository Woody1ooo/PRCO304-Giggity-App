package com.liamd.giggity_app;

/**
 * Created by liamd on 18/04/2017.
 */

public class UserGigInformation
{
    private String calendarEventID;
    private String gigID;

    public UserGigInformation()
    {

    }

    public UserGigInformation(String calendarEventID, String gigID)
    {
        this.calendarEventID = calendarEventID;
        this.gigID = gigID;
    }

    public String getCalendarEventID()
    {
        return calendarEventID;
    }

    public void setCalendarEventID(String calendarEventID)
    {
        this.calendarEventID = calendarEventID;
    }

    public String getGigID()
    {
        return gigID;
    }

    public void setGigID(String gigID)
    {
        this.gigID = gigID;
    }
}
