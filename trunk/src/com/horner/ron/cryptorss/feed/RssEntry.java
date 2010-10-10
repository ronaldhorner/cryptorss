package com.horner.ron.cryptorss.feed;

public class RssEntry {

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
    
    
}
