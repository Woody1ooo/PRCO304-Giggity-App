package com.liamd.giggity_app;

/**
 * Created by liamd on 16/02/2017.
 */

public class User
{
    private String email;
    private String password;
    private String userID;
    private Boolean hasCompletedSetup;

    public User()
    {

    }

    public User(String email, String password, String userID, Boolean hasLoggedIn)
    {
        this.email = email;
        this.password = password;
        this.userID = userID;
        this.hasCompletedSetup = hasLoggedIn;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getUserID()
    {
        return userID;
    }

    public void setUserID(String userID)
    {
        this.userID = userID;
    }

    public Boolean getHasCompletedSetup()
    {
        return hasCompletedSetup;
    }

    public void setHasCompletedSetup(Boolean hasLoggedIn)
    {
        this.hasCompletedSetup = hasLoggedIn;
    }
}
