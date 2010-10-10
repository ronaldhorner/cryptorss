package com.horner.ron.cryptorss.feed;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.horner.ron.cryptorss.R;
import com.horner.ron.cryptorss.database.DatabaseHelper;

public class FeedListAdapter extends BaseAdapter {

    /** The list of feeds */
    private ArrayList<RssFeed> feeds = new ArrayList<RssFeed>();
    
    /** A context so we can build the TextView objects*/
    private Context context = null;
    
    /**
     * Simple constructor
     * @param c the context
     * @param fs the list of RssFeeds
     */
    public FeedListAdapter(Context c, ArrayList<RssFeed> fs){
        this.feeds = fs;
        this.context = c;
    }
    
    /**
     * Get the number of feeds
     * 
     * @return the number of feeds
     */
    public int getCount() {
        // TODO Auto-generated method stub
        return feeds.size();
    }

    /**
     * Get the RssEntry at the specific position
     * 
     * @param the position of the feed
     */
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return feeds.get(arg0);
    }

    /**
     * We are always returning 0 here
     * 
     * @return 0
     */
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Return back a Relative Layout with the feed name and 
     * a fraction representing the number of completed puzzles
     * to total puzzles
     * 
     * @param pos the position of the feed
     * @param view the view representing the feed
     * @param arg2 unused
     */
    public View getView(int pos, View view, ViewGroup group) {
        long feedId = feeds.get(pos).getId();
        
        if (view == null){
            view = new RelativeLayout(context);
        } 
        
        // get the view as a Relative Layout and inflate the stored layout into it
        RelativeLayout tl = (RelativeLayout) view;
        LayoutInflater.from(context).inflate(R.layout.feed, tl);
        
        // set the title of the TextView
        TextView tv = (TextView)tl.findViewById(R.id.feed_title);
        String name = feeds.get(pos).getName();
        tv.setText(name);
        
        // set the url of the TextView
        TextView uv = (TextView)tl.findViewById(R.id.feed_url);
        String url = feeds.get(pos).getUrl();
        uv.setText(url);
        
        // Set the fraction for the TextView
        DatabaseHelper dbh = new DatabaseHelper(context);
        TextView fv = (TextView)tl.findViewById(R.id.feed_fraction);
        long completed = dbh.getEntryCompletedCount(feedId);
        long total = dbh.getEntryCount(feedId);
        long incomplete = total - completed;
        fv.setText( incomplete + "/" + total);
        
        return tl;
    }

}
