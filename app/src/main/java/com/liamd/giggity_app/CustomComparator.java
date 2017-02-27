package com.liamd.giggity_app;

import java.util.Comparator;

/**
 * Created by liamd on 27/02/2017.
 * A custom implementation of the Comparator class
 * allow the list of gig objects to be sorted by date.
 */

public class CustomComparator implements Comparator<Gig>
{
    @Override
    public int compare(Gig gig1, Gig gig2)
    {
        return gig1.getStartDate().compareTo(gig2.getStartDate());
    }
}
