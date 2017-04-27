package com.liamd.giggity_app;

/**
 * Created by liamd on 24/04/2017.
 */

public class NewsFeedItem
{
    private String itemID;
    private String userName;
    private String message;
    private String userID;
    private String gigID;
    private String bandID;
    private int likeCount;
    private boolean featured;
    private int featuredWeeksQuantity;

    public NewsFeedItem()
    {

    }

    public NewsFeedItem(String itemID, String userName, String message, String gigID, boolean featured, int featuredWeeksQuantity)
    {
        this.itemID = itemID;
        this.userName = userName;
        this.message = message;
        this.gigID = gigID;
        this.featured = featured;
        this.featuredWeeksQuantity = featuredWeeksQuantity;
    }

    public NewsFeedItem(String itemID, String userName, String message, String gigID, String bandID)
    {
        this.itemID = itemID;
        this.userName = userName;
        this.message = message;
        this.gigID = gigID;
        this.bandID = bandID;
    }

    public NewsFeedItem(String itemID, String userName, String message, String bandID)
    {
        this.itemID = itemID;
        this.userName = userName;
        this.message = message;
        this.bandID = bandID;
    }

    public String getItemID()
    {
        return itemID;
    }

    public void setItemID(String itemID)
    {
        this.itemID = itemID;
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

    public boolean isFeatured()
    {
        return featured;
    }

    public void setFeatured(boolean featured)
    {
        this.featured = featured;
    }

    public int getFeaturedWeeksQuantity()
    {
        return featuredWeeksQuantity;
    }

    public void setFeaturedWeeksQuantity(int featuredWeeksQuantity)
    {
        this.featuredWeeksQuantity = featuredWeeksQuantity;
    }
}
