package com.liamd.giggity_app;

/**
 * Created by liamd on 24/04/2017.
 */

public class NewsFeedItem
{
    private String itemId;
    private String userName;
    private String message;
    private String userID;
    private String gigID;
    private String bandID;
    private int likeCount;

    public NewsFeedItem()
    {

    }

    public NewsFeedItem(String itemId, String userName, String message, String gigID, String bandID)
    {
        this.itemId = itemId;
        this.userName = userName;
        this.message = message;
        this.gigID = gigID;
        this.bandID = bandID;
    }

    public NewsFeedItem(String itemId, String userName, String message,  String bandID)
    {
        this.itemId = itemId;
        this.userName = userName;
        this.message = message;
        this.bandID = bandID;
    }

    public String getItemId()
    {
        return itemId;
    }

    public void setItemId(String itemId)
    {
        this.itemId = itemId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getUserID()
    {
        return userID;
    }

    public void setUserID(String userID)
    {
        this.userID = userID;
    }

    public String getGigID()
    {
        return gigID;
    }

    public void setGigID(String gigID)
    {
        this.gigID = gigID;
    }

    public String getBandID()
    {
        return bandID;
    }

    public void setBandID(String bandId)
    {
        this.bandID = bandID;
    }

    public int getLikeCount()
    {
        return likeCount;
    }

    public void setLikeCount(int likeCount)
    {
        this.likeCount = likeCount;
    }
}
