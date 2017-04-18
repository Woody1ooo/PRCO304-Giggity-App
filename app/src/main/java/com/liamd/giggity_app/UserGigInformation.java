package com.liamd.giggity_app;

/**
 * Created by liamd on 18/04/2017.
 */

public class UserGigInformation
{
    private String calendarEventID;
    private String gigID;
    private String memberConfirmedRequest;

    public UserGigInformation()
    {

    }

    public UserGigInformation(String calendarEventID, String gigID, String memberConfirmedRequest)
    {
        this.calendarEventID = calendarEventID;
        this.gigID = gigID;
        this.memberConfirmedRequest = memberConfirmedRequest;
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

    public String getMemberConfirmedRequest()
    {
        return memberConfirmedRequest;
    }

    public void setMemberConfirmedRequest(String memberConfirmedRequest)
    {
        this.memberConfirmedRequest = memberConfirmedRequest;
    }
}
