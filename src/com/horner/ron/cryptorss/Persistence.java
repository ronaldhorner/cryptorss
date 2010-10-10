package com.horner.ron.cryptorss;

import android.app.Application;

/**
 * So it turns out that sometimes an activity is rebuilt, like on screen orientation switches
 * and some things are just too cumbersome to be passing around in a bundle.  The Persistence
 * class acts as a sort of application wide store house.
 * 
 * @author Ron Horner
 *
 */
public class Persistence extends Application {

    /** a reference to itself */
    private static Persistence self = null;

    /** the starting time of the puzzle */
    private long time = 0;
        
    /** the amount of time spent on the puzzle */
    private long timeSpent = 0;
    
    /** a reference to the active puzzle */
    private Puzzle puzzle = null;
    
    /**
     * There can be only one
     */
    private Persistence(){
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
    }
    
    /**
     * I said, "There can be only one!"
     * @return static reference to the Persistence
     */
    public static Persistence getInstance(){
        if (self == null)
            self = new Persistence();
        
        return self;
    }

    /**
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }


    /**
     * @param timeSpent the timeSpent to set
     */
    public void setTimeSpent(long timeSpent) {
        this.timeSpent = timeSpent;
    }

    /**
     * @return the timeSpent
     */
    public long getTimeSpent() {
        return timeSpent;
    }

    /**
     * @param puzzle the puzzle to set
     */
    public void setPuzzle(Puzzle puzzle) {
        this.puzzle = puzzle;
    }

    /**
     * @return the puzzle
     */
    public Puzzle getPuzzle() {
        return puzzle;
    }

}
