package com.liamd.giggity_app;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by liamd on 25/03/2017.
 */

public class MusicianBandsAdapter extends ArrayAdapter<Band>
{
    // Declare visual components
    private TextView mBandNameTextView;
    private TextView mNumberOfMembers;
    private TextView mGenresTextView;
    private TextView mDistanceTextView;

    // Declare various variables required
    private int resource;

    public MusicianBandsAdapter(Context context, int resource, List<Band> items)
    {
        super(context, resource, items);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout bandsListView;

        Band band = getItem(position);

        if(convertView == null)
        {
            bandsListView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater layoutInflater;
            layoutInflater = (LayoutInflater)getContext().getSystemService(inflater);
            layoutInflater.inflate(resource, bandsListView, true);
        }

        else
        {
            bandsListView = (LinearLayout) convertView;
        }

        // Initialise visual components
        mBandNameTextView = (TextView) bandsListView.findViewById(R.id.bandName);
        mDistanceTextView = (TextView) bandsListView.findViewById(R.id.distance);
        mGenresTextView = (TextView) bandsListView.findViewById(R.id.genres);
        mNumberOfMembers = (TextView) bandsListView.findViewById(R.id.numberOfMembers);

        // Set list view fields to display the correct information
        mBandNameTextView.setText(band.getName());
        mBandNameTextView.setTypeface(null, Typeface.BOLD);
        mDistanceTextView.setText(band.getBandDistance() + "km");
        mGenresTextView.setText(band.getGenres());
        mNumberOfMembers.setText("Total Positions: " + band.getNumberOfPositions());

        return bandsListView;
    }
}
