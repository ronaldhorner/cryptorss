package com.horner.ron.cryptorss.feed;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.horner.ron.cryptorss.R;

public class EntryListAdapter extends BaseAdapter {

    /** The list of entries */
    ArrayList<RssEntry> entries = new ArrayList<RssEntry>();
    
    /** A context so we can build the TextView objects*/
    private Context context = null;
    
    /**
     * Simple constructor
     * @param c the context
     * @param fs the list of RssEntries
     */
    public EntryListAdapter(Context c, ArrayList<RssEntry> fs){
        this.entries = fs;
        this.context = c;
    }
    
    /**
     * Get the number of entries
     * 
     * @return the number of entries
     */
    public int getCount() {
        return entries.size();
    }

    /**
     * Get the RssEntry at the specific position
     * 
     * @param the position of the entry
     */
    public Object getItem(int arg0) {
        return entries.get(arg0);
    }

    /**
     * We are always returning 0 here
     * 
     * @return 0
     */
    public long getItemId(int arg0) {
        return 0;
    }

    /**
     * Return back a TextView with the entry title
     * 
     * @param pos the position of the entry
     * @param view the view representing the entry
     * @param arg2 unused
     */
    public View getView(int pos, View view, ViewGroup arg2) {
        
        if (view == null){
            view = new RelativeLayout(context);
        } 
        
        // get the view as a Relative Layout and inflate the stored layout into it
        RelativeLayout tl = (RelativeLayout) view;
        LayoutInflater.from(context).inflate(R.layout.entry, tl);
        
        // set the title of the TextView
        TextView tv = (TextView)tl.findViewById(R.id.entry_title);
        String name = entries.get(pos).getTitle();
        tv.setText(name);
        
        // set the url of the TextView
        TextView uv = (TextView)tl.findViewById(R.id.entry_url);
        String url = entries.get(pos).getUrl();
        uv.setText(url);

        if (entries.get(pos).getState() == 1){
            tv.setTextColor(Color.GRAY);
            uv.setTextColor(Color.GRAY);
        }
        
        return tl;
    }

}
