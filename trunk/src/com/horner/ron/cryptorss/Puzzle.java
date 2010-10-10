package com.horner.ron.cryptorss;

import java.util.ArrayList;
import java.util.HashMap;

import com.horner.ron.cryptorss.feed.RssEntry;

/**
 * Basically an RSS Entry, but due to casting constraints, ended up being it's own class
 * 
 * @author Ron Horner
 *
 */
public class Puzzle {
    
    /** reference to the plain text views */
    private ArrayList<CryptoTextView> plainTextViews = new ArrayList<CryptoTextView>();

    /** reference to the crypto map */
    private HashMap<String, String> cryptoMap = new HashMap<String, String>();

    /** the id */
    private long id = -1;
    
    /** the id of the parent feed */
    private long feed = -1;
    
    /** the title of the entry */
    private String title = "";
    
    /** the summary of the entry */
    private String summary = "";
    
    /** the url linking back to the full story */
    private String url = "";
    
    /** the current state of the entry */
    private int state = 0;

    /**
     * Sets up the puzzle with all of the info from the entry
     * @param e the entry to use as a base for the puzzle
     */
    public Puzzle(RssEntry e){
        this.id = e.getId();
        this.feed = e.getFeed();
        this.title = e.getTitle();
        this.summary = e.getSummary();
        this.url = e.getUrl();
        this.state = e.getState();
    }
    
    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param feed the feed to set
     */
    public void setFeed(long feed) {
        this.feed = feed;
    }

    /**
     * @return the feed
     */
    public long getFeed() {
        return feed;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param summary the summary to set
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param state the state to set
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * @return the state
     */
    public int getState() {
        return state;
    }
    
    /**
     * @param plainTextViews the plainTextViews to set
     */
    public void setPlainTextViews(ArrayList<CryptoTextView> plainTextViews) {
        this.plainTextViews = plainTextViews;
    }

    /**
     * @return the plainTextViews
     */
    public ArrayList<CryptoTextView> getPlainTextViews() {
        return plainTextViews;
    }

    /**
     * @param cryptoMap the cryptoMap to set
     */
    public void setCryptoMap(HashMap<String, String> cryptoMap) {
        this.cryptoMap = cryptoMap;
    }

    /**
     * @return the cryptoMap
     */
    public HashMap<String, String> getCryptoMap() {
        return cryptoMap;
    }

}
