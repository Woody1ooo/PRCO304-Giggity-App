package com.liamd.giggity_app;

/**
 * Created by liamd on 28/03/2017.
 */

public class BandRequest
{
    private String bandID;
    private String bandName;
    private String bandPosition;
    private String positionInstruments;
    private String userInstruments;
    private String requestStatus;
    private String userName;
    private String userID;

    public BandRequest()
    {

    }

    public BandRequest(String bandID, String bandName, String bandPosition, String positionInstruments, String requestStatus)
    {
        this.bandID = bandID;
        this.bandName = bandName;
        this.bandPosition = bandPosition;
        this.positionInstruments = positionInstruments;
        this.requestStatus = requestStatus;
    }

    public BandRequest(String bandID, String bandName, String bandPosition, String positionInstruments, String requestStatus, String userName)
    {
        this.bandID = bandID;
        this.bandName = bandName;
        this.bandPosition = bandPosition;
        this.positionInstruments = positionInstruments;
        this.requestStatus = requestStatus;
    }

    public BandRequest(String bandID, String bandName, String bandPosition, String positionInstruments, String requestStatus, String userName, String userID)
    {
        this.bandID = bandID;
        this.bandName = bandName;
        this.bandPosition = bandPosition;
        this.positionInstruments = positionInstruments;
        this.requestStatus = requestStatus;
        this.userID = userID;
    }

    public BandRequest(String bandID, String bandName, String bandPosition, String positionInstruments, String userInstruments, String requestStatus, String userName, String userID)
    {
        this.bandID = bandID;
        this.bandName = bandName;
        this.bandPosition = bandPosition;
        this.positionInstruments = positionInstruments;
        this.userInstruments = userInstruments;
        this.requestStatus = requestStatus;
        this.userName = userName;
        this.userID = userID;
    }

    public String getBandID()
    {
        return bandID;
    }

    public void setBandID(String bandID)
    {
        this.bandID = bandID;
    }

    public String getBandName()
    {
        return bandName;
    }

    public void setBandName(String bandName)
    {
        this.bandName = bandName;
    }

    public String getBandPosition()
    {
        return bandPosition;
    }

    public void setBandPosition(String bandPosition)
    {
        this.bandPosition = bandPosition;
    }

    public String getPositionInstruments()
    {
        return positionInstruments;
    }

    public void setPositionInstruments(String positionInstruments)
    {
        this.positionInstruments = positionInstruments;
    }

    public String getRequestStatus()
    {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus)
    {
        this.requestStatus = requestStatus;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getUserID()
    {
        return userID;
    }

    public void setUserID(String userID)
    {
        this.userID = userID;
    }

    public String getUserInstruments()
    {
        return userInstruments;
    }

    public void setUserInstruments(String userInstruments)
    {
        this.userInstruments = userInstruments;
    }
}
