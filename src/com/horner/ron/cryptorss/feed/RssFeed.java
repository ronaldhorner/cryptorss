package com.horner.ron.cryptorss.feed;

public class RssFeed {
    
    /** the name of the feed */
    private String name = "";
    
    /** the url for the feed */
    private String url = "";

    /** the feed id */
    private long id = -1;
    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
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
}
