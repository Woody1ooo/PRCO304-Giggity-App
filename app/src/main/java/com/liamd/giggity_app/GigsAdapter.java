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
 * Created by liamd on 27/02/2017.
 */

public class GigsAdapter extends ArrayAdapter<Gig>
{
    // Declare visual components
    private TextView mGigName;
    private TextView mGigDate;

    // Declare various variables required
    private int resource;

    public GigsAdapter(Context context, int resource, List<Gig> items)
    {
        super(context, resource, items);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout gigsListView;

        Gig gig = getItem(position);

        if(convertView == null)
        {
            gigsListView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater layoutInflater;
            layoutInflater = (LayoutInflater)getContext().getSystemService(inflater);
            layoutInflater.inflate(resource, gigsListView, true);
        }

        else
        {
            gigsListView = (LinearLayout) convertView;
        }

        // Initialise visual components
        mGigName = (TextView)gigsListView.findViewById(R.id.gigName);
        mGigDate = (TextView)gigsListView.findViewById(R.id.gigDate);

        mGigName.setText(gig.getTitle());
        mGigName.setTypeface(null, Typeface.BOLD);

        mGigDate.setText(gig.getStartDate().toString());

        return gigsListView;
    }
}
