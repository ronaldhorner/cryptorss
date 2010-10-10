package com.horner.ron.cryptorss.feed;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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
            view = new TextView(context);
        } 
        
        TextView tv = (TextView) view;
        String title = entries.get(pos).getTitle();
        tv.setText(title);
        tv.setLines(1);
        tv.setHorizontallyScrolling(true);
        tv.setSingleLine(true);
        tv.setPadding(10, 0, 10, 0);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        tv.setEllipsize(TruncateAt.END);
        tv.setTextColor(Color.WHITE);
        
        if (entries.get(pos).getState() == 1)
            tv.setTextColor(Color.GRAY);
        
        tv.invalidate();
        
        return tv;
    }

}
