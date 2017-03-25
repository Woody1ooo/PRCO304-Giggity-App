package com.liamd.giggity_app;

/**
 * Created by liamd on 23/03/2017.
 */

public class Band
{
    String bandID;
    String name;
    String genres;
    String numberOfPositions;
    String positionOne;
    String positionTwo;
    String positionThree;
    String positionFour;
    String positionFive;
    LatLng baseLocation;
    private double bandDistance;

    public Band()
    {

    }

    public Band(String bandID, String name, String genres, String numberOfPositions, String positionOne , LatLng baseLocation)
    {
        this.bandID = bandID;
        this.name = name;
        this.genres = genres;
        this.numberOfPositions = numberOfPositions;
        this.positionOne = positionOne;
        this.baseLocation = baseLocation;
    }

    public Band(String bandID, String name, String genres, String numberOfPositions, String positionOne, String positionTwo, LatLng baseLocation)
    {
        this.bandID = bandID;
        this.name = name;
        this.genres = genres;
        this.numberOfPositions = numberOfPositions;
        this.positionOne = positionOne;
        this.positionTwo = positionTwo;
        this.baseLocation = baseLocation;
    }

    public Band(String bandID, String name, String genres, String numberOfPositions, String positionOne, String positionTwo, String positionThree, LatLng baseLocation)
    {
        this.bandID = bandID;
        this.name = name;
        this.genres = genres;
        this.numberOfPositions = numberOfPositions;
        this.positionOne = positionOne;
        this.positionTwo = positionTwo;
        this.positionThree = positionThree;
        this.baseLocation = baseLocation;
    }

    public Band(String bandID, String name, String genres, String numberOfPositions, String positionOne, String positionTwo, String positionThree, String positionFour, LatLng baseLocation)
    {
        this.bandID = bandID;
        this.name = name;
        this.genres = genres;
        this.numberOfPositions = numberOfPositions;
        this.positionOne = positionOne;
        this.positionTwo = positionTwo;
        this.positionThree = positionThree;
        this.positionFour = positionFour;
        this.baseLocation = baseLocation;
    }

    public Band(String bandID, String name, String genres, String numberOfPositions, String positionOne, String positionTwo, String positionThree, String positionFour, String positionFive, LatLng baseLocation)
    {
        this.bandID = bandID;
        this.name = name;
        this.genres = genres;
        this.numberOfPositions = numberOfPositions;
        this.positionOne = positionOne;
        this.positionTwo = positionTwo;
        this.positionThree = positionThree;
        this.positionFour = positionFour;
        this.positionFive = positionFive;
        this.baseLocation = baseLocation;
    }

    public String getBandID()
    {
        return bandID;
    }

    public void setBandID(String bandID)
    {
        this.bandID = bandID;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getGenres()
    {
        return genres;
    }

    public void setGenres(String genres)
    {
        this.genres = genres;
    }

    public String getNumberOfPositions()
    {
        return numberOfPositions;
    }

    public void setNumberOfPositions(String numberOfPositions)
    {
        this.numberOfPositions = numberOfPositions;
    }

    public String getPositionOne()
    {
        return positionOne;
    }

    public void setPositionOne(String positionOne)
    {
        this.positionOne = positionOne;
    }

    public String getPositionTwo()
    {
        return positionTwo;
    }

    public void setPositionTwo(String positionTwo)
    {
        this.positionTwo = positionTwo;
    }

    public String getPositionThree()
    {
        return positionThree;
    }

    public void setPositionThree(String positionThree)
    {
        this.positionThree = positionThree;
    }

    public String getPositionFour()
    {
        return positionFour;
    }

    public void setPositionFour(String positionFour)
    {
        this.positionFour = positionFour;
    }

    public String getPositionFive()
    {
        return positionFive;
    }

    public void setPositionFive(String positionFive)
    {
        this.positionFive = positionFive;
    }

    public LatLng getBaseLocation()
    {
        return baseLocation;
    }

    public void setBaseLocation(LatLng baseLocation)
    {
        this.baseLocation = baseLocation;
    }

    public double getBandDistance()
    {
        return bandDistance;
    }

    public void setBandDistance(double bandDistance)
    {
        this.bandDistance = bandDistance;
    }
}
