package com.liamd.giggity_app;

import java.util.Comparator;

/**
 * Created by liamd on 27/02/2017.
 * A custom implementation of the Comparator class
 * allow the list of gig objects to be sorted by date.
 */

public class CustomNewsItemComparator implements Comparator<NewsFeedItem>
{
    @Override
    public int compare(NewsFeedItem newsFeedItem, NewsFeedItem newsFeedItem1)
    {
        return newsFeedItem1.getPostDate().compareTo(newsFeedItem.getPostDate());
    }
}
