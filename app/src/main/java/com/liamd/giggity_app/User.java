package com.liamd.giggity_app;

/**
 * Created by liamd on 16/02/2017.
 */

public class User
{
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String userID;
    private Boolean hasCompletedSetup;
    private String accountType;
    private String genres;
    private String instruments;
    private LatLng homeLocation;
    private String homeAddress;
    private double musicianDistance;
    private boolean isInBand;
    private int age;

    public User()
    {

    }

    public User(String email, String firstName, String lastName, String password, String userID, Boolean hasLoggedIn)
    {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.userID = userID;
        this.hasCompletedSetup = hasLoggedIn;
    }

    public User(String accountType, String email, String firstName, String genres, Boolean hasCompletedSetup, String homeAddress, LatLng homeLocation, String instruments, String lastName, String userID)
    {
        this.accountType = accountType;
        this.email = email;
        this.firstName = firstName;
        this.genres = genres;
        this.hasCompletedSetup = hasCompletedSetup;
        this.homeAddress = homeAddress;
        this.homeLocation = homeLocation;
        this.instruments = instruments;
        this.lastName = lastName;
        this.userID = userID;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
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

    public String getAccountType()
    {
        return accountType;
    }

    public void setAccountType(String accountType)
    {
        this.accountType = accountType;
    }

    public String getGenres()
    {
        return genres;
    }

    public void setGenres(String genres)
    {
        this.genres = genres;
    }

    public String getInstruments()
    {
        return instruments;
    }

    public void setInstruments(String instruments)
    {
        this.instruments = instruments;
    }

    public LatLng getHomeLocation()
    {
        return homeLocation;
    }

    public void setHomeLocation(LatLng homeLocation)
    {
        this.homeLocation = homeLocation;
    }

    public String getHomeAddress()
    {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress)
    {
        this.homeAddress = homeAddress;
    }

    public double getMusicianDistance()
    {
        return musicianDistance;
    }

    public void setMusicianDistance(double musicianDistance)
    {
        this.musicianDistance = musicianDistance;
    }

    public boolean isInBand()
    {
        return isInBand;
    }

    public void setInBand(boolean inBand)
    {
        isInBand = inBand;
    }

    public int getAge()
    {
        return age;
    }

    public void setAge(int age)
    {
        this.age = age;
    }
}
