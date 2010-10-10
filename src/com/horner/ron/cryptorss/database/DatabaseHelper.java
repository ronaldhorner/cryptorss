package com.horner.ron.cryptorss.database;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.horner.ron.cryptorss.feed.RssEntry;
import com.horner.ron.cryptorss.feed.RssFeed;

/**
 * The DatabaseHelper class provides convenience functions to
 * all the database calls for managing feeds, entries, and game time
 * 
 * @author Ron Horner
 *
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    /** Hold onto a context so we can access the file system */
    private Context context = null;
    
    /** the name of the SQLite database */
    private static final String DB_NAME = "cryptorss.db";
    
    /** the path to the SQLite database on the android storage space */
    private static String DB_PATH = "/data/data/com.horner.ron.cryptorss/databases/";
    
    /**
     * Sets up the DatabaseHelper
     * 
     * @param c a context from the application
     */
    public DatabaseHelper(Context c){
        super(c, DB_NAME, null, 1);
        this.context = c;
    }

    @Override
    public void onCreate(SQLiteDatabase arg0) {
        // Nothing exciting here    
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        // Nothing exciting here
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException{
        boolean dbExist = checkDataBase();
 
        if(dbExist){
            //do nothing - database already exist
        }else{
            // By calling this method and empty database will be created into the default system path
            // of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
        this.close();
    }
 
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
        SQLiteDatabase checkDB = null;
        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }catch(SQLiteException e){
            //database does't exist yet.
        }
 
        if(checkDB != null){
            checkDB.close();
        }
 
        return checkDB != null ? true : false;
    }
 
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{
        //Open your local db as the input stream
        InputStream myInput = context.getAssets().open(DB_NAME);
        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;
        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
 
        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }
 
        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
 
    }

    /**
     * Opens a fresh connection to the SQLite database
     * 
     * @return a reference to the SQLite database
     * @throws SQLException
     * @throws IOException
     */
    private SQLiteDatabase openDataBase() throws SQLException, IOException{
        //Open the database
        createDataBase();
        String myPath = DB_PATH + DB_NAME;
        return SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

       
    /**
     * Adds a new feed to the database.  If the feed already exists
     * return the existing feed id
     * @param name the name of the feed
     * @param url the url of the feed
     * @return the id of the added feed
     */
    public long addFeed(String name, String url){
        long feedId = -1;
        SQLiteDatabase db = null;
        try {
            // check to see if the feed already exists
            String[] b = new String[1];
            b[0] = url;
            db = openDataBase();
            Cursor cursor = db.rawQuery("SELECT id FROM feeds WHERE url = ?", b);

            // if not add it
            if (cursor.getCount() == 0){
                ContentValues values = new ContentValues();
                values.put("name", name);
                values.put("url", url);
                feedId = db.insert("feeds", null, values); 
            } else {
                // otherwise get the feed id
                if (cursor.moveToNext())
                    feedId = cursor.getLong(0);
            }
            cursor.close();
            db.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null){
                db.close();
            }
        }
        System.out.println("DatabaseHelper.addFeed() :: " + name);
        return feedId;
    }
        
    /**
     * Add a feed entry to the database
     * 
     * @param feedId the feed it belongs to
     * @param title the title of the entry
     * @param summary the summary of the entry
     * @param url the url of the entry
     */
    public void addEntry(long feedId, String title, String summary, String url){
        SQLiteDatabase db = null;
        try {
            // check to see if the entry already exists
            String[] b = new String[1];
            b[0] = url;
            db = openDataBase();
            Cursor cursor = db.rawQuery("SELECT id FROM entries WHERE url = ?", b);
            
            // if it doesn't, add it
            if (cursor.getCount() == 0){
                Object[] bind = new Object[5];
                bind[0] = new Long(feedId);
                bind[1] = title;
                bind[2] = summary;
                bind[3] = url;
                bind[4] = "0";
                db.execSQL("INSERT INTO entries (feed, title, summary, url, state) VALUES(?,?,?,?,?)", bind);
            }
            cursor.close();
            db.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null){
                db.close();
            }
        }
        System.out.println("DatabaseHelper.addEntry() :: " + title);
    }
    
    /**
     * Delete the entry from the database
     * 
     * @param id the entry id
     */
    public void deleteEntry(long id){
        SQLiteDatabase db = null;
        try {
            db = openDataBase();
            Object[] bind = new Object[1];
            bind[0] = new Long(id);
            db.execSQL("DELETE FROM entries WHERE id = ?", bind);
            db.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null){
                db.close();
            }
        }
        System.out.println("DatabaseHelper.deleteEntry() :: " + id);
    }
    
    /**
     * Delete all the entries for a specific feed
     * 
     * @param feed the id of the feed
     */
    private void deleteFeedEntries(long feed){
        SQLiteDatabase db = null;
        try {
            db = openDataBase();
            Object[] bind = new Object[1];
            bind[0] = new Long(feed);
            db.execSQL("DELETE FROM entries WHERE feed = ?", bind);
            db.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null){
                db.close();
            }
        }
        System.out.println("DatabaseHelper.deleteFeedEntries() :: " + feed);
    }
    
    /**
     * Delete the feed from the database.  Delete the associated entries first
     * and then delete the feed itself.
     * 
     * @param feedId the id of the feed
     */
    public void deleteFeed(long feedId){
        SQLiteDatabase db = null;
        try {
            // delete the feed's entries
            deleteFeedEntries(feedId);
            
            // delete the feed
            db = openDataBase();
            Object[] bind = new Object[1];
            bind[0] = new Long(feedId);
            db.execSQL("DELETE FROM feeds WHERE id = ?", bind);
            db.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null){
                db.close();
            }
        }
        System.out.println("DatabaseHelper.deleteFeed() :: " + feedId);
    }
    
    /**
     * Get the list of feeds
     * 
     * @return an ArrayList of RssFeeds
     */
    public ArrayList<RssFeed> getFeeds(){
        ArrayList<RssFeed> rval = new ArrayList<RssFeed>();
        SQLiteDatabase db = null;
        try {
            db = openDataBase();
            Cursor cursor = db.rawQuery("SELECT * FROM feeds ORDER BY id ASC", null);
            while (cursor.moveToNext()){
                RssFeed rss = new RssFeed();
                rss.setId(cursor.getLong(0));
                rss.setName(cursor.getString(1));
                rss.setUrl(cursor.getString(2));
                rval.add(rss);
            }
            cursor.close();
            db.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null){
                db.close();
            }
        }
        System.out.println("DatabaseHelper.getFeeds() :: " + rval.size());
        return rval;
    }
    
    /**
     * Get all of the feed's associated entries
     * 
     * @param feedId the feed id
     * @return an ArrayList of RssEntries
     */
    public ArrayList<RssEntry> getEntries(long feedId){
        ArrayList<RssEntry> rval = new ArrayList<RssEntry>();
        SQLiteDatabase db = null;
        try {
            db = openDataBase();
            String[] bind = new String[1];
            bind[0] = feedId + "";
            Cursor cursor = db.rawQuery("SELECT id, feed, title, summary, url, state FROM entries where feed = ? ORDER BY id ASC", bind);
            while (cursor.moveToNext()){
                RssEntry rss = new RssEntry();
                rss.setId(cursor.getLong(0));
                rss.setFeed(cursor.getLong(1));
                rss.setTitle(cursor.getString(2));
                rss.setSummary(cursor.getString(3));
                rss.setUrl(cursor.getString(4));
                rss.setState(cursor.getInt(5));
                rval.add(rss);
            }
            cursor.close();
            db.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null){
                db.close();
            }
        }
        System.out.println("DatabaseHelper.getEntries() :: " + rval.size());
        return rval;
    }
    
    /**
     * Get a specific entry
     * 
     * @param entryId the entry id
     * @return an RssEntry containing all the entry information
     */
    public RssEntry getEntry(long entryId){
        RssEntry rval = new RssEntry();
        SQLiteDatabase db = null;
        try {
            db = openDataBase();
            String[] bind = new String[1];
            bind[0] = entryId + "";
            Cursor cursor = db.rawQuery("SELECT id, feed, title, summary, url, state FROM entries where id = ?", bind);
            while (cursor.moveToNext()){
                rval.setId(cursor.getLong(0));
                rval.setFeed(cursor.getLong(1));
                rval.setTitle(cursor.getString(2));
                rval.setSummary(cursor.getString(3));
                rval.setUrl(cursor.getString(4));
                rval.setState(cursor.getInt(5));
            }
            cursor.close();
            db.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null){
                db.close();
            }
        }
        System.out.println("DatabaseHelper.getEntry() :: " + rval.getId());
        return rval;
    }
    
    /**
     * Get a specific feed
     * 
     * @param feedId the feed id
     * @return an RssFeed containing the feed's information
     */
    public RssFeed getFeed(long feedId){
        RssFeed rval = new RssFeed();
        SQLiteDatabase db = null;
        try {
            db = openDataBase();
            String[] bind = new String[1];
            bind[0] = feedId + "";
            Cursor cursor = db.rawQuery("SELECT id, name, url FROM feeds where id = ?", bind);
            while (cursor.moveToNext()){
                rval.setId(cursor.getLong(0));
                rval.setName(cursor.getString(1));
                rval.setUrl(cursor.getString(2));
            }
            cursor.close();
            db.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null){
                db.close();
            }
        }
        System.out.println("DatabaseHelper.getFeed() :: " + rval.getId());
        return rval;
    }
    
    /**
     * Add a new time to the database
     * 
     * @param time the time in milliseconds
     */
    public void addTime(long time){
        SQLiteDatabase db = null;
        try {
            Object[] bind = new Object[1];
            bind[0] = new Long(time);
            db = openDataBase();
            db.execSQL("INSERT INTO times (time) VALUES (?)", bind);
            db.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null){
                db.close();
            }
        }
        System.out.println("DatabaseHelper.addTime() :: " + time);
    }
    
    /**
     * Get the average time so long as at least 5 times already exisit
     * 
     * @return the average time
     */
    public long getAverageTime(){
        long avg = 0;
        SQLiteDatabase db = null;
        try {
            // count the number of times in the database
            db = openDataBase();
            long count = 0;
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM times", null);
            if (cursor.getCount() > 0 && cursor.moveToFirst())
                count = cursor.getLong(0);
            cursor.close();
            
            // if we have more than 5 then get the average
            if (count > 5){
                cursor = db.rawQuery("SELECT AVG(time) FROM times", null);
                if (cursor.getCount() > 0 && cursor.moveToFirst())
                    avg = cursor.getLong(0);
                cursor.close();
            }
            db.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null){
                db.close();
            }
        }
        System.out.println("DatabaseHelper.getAverageTime() :: " + avg);
        return avg;
    }

    /**
     * Get the count of the number of entries for a feed
     * 
     * @param feedId the feed id
     * @return the number of entries for the feed
     */
    public long getEntryCount(long feedId){
        long rval = -1;
        SQLiteDatabase db = null;
        try {
            db = openDataBase();
            String[] bind = new String[1];
            bind[0] = feedId + "";
            Cursor cursor = db.rawQuery("SELECT count(*) FROM entries where feed = ?", bind);
            if (cursor.moveToNext()){
                rval = cursor.getLong(0);
            }
            cursor.close();
            db.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null){
                db.close();
            }
        }
        System.out.println("DatabaseHelper.getEntryCount() :: " + rval);
        return rval;
    }
    
    /**
     * Get the count of the number of completed entries for a feed
     * 
     * @param feedId the feed id
     * @return the number of completed entries
     */
    public long getEntryCompletedCount(long feedId){
        long rval = -1;
        SQLiteDatabase db = null;
        try {
            db = openDataBase();
            String[] bind = new String[1];
            bind[0] = feedId + "";
            Cursor cursor = db.rawQuery("SELECT count(*) FROM entries where state = 1 and feed = ?", bind);
            if (cursor.moveToNext()){
                rval = cursor.getLong(0);
            }
            cursor.close();
            db.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null){
                db.close();
            }
        }
        System.out.println("DatabaseHelper.getEntryCompletedCount() :: " + rval);
        return rval;
    }
    
    /**
     * Mark an entry as being solved
     * 
     * @param entryId the entry id
     */
    public void setPuzzleSolved(long entryId){
        SQLiteDatabase db = null;
        try {
            Object[] bind = new Object[1];
            bind[0] = new Long(entryId);
            db = openDataBase();
            db.execSQL("UPDATE entries SET state = 1 where id = ?", bind);
            db.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null){
                db.close();
            }
        }
        System.out.println("DatabaseHelper.setPuzzleSolved() :: " + entryId);
    }
    
    /**
     * Set the name of the feed
     * 
     * @param feedId the feed id
     * @param name the new feed name
     */
    public void setFeedName(long feedId, String name){
        SQLiteDatabase db = null;
        try {
            Object[] bind = new Object[2];
            bind[0] = name;
            bind[1] = new Long(feedId);
            db = openDataBase();
            db.execSQL("UPDATE feeds SET name = ? where id = ?", bind);
            db.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null){
                db.close();
            }
        }
        System.out.println("DatabaseHelper.setFeedName() :: " + name);
    }
    
    /**
     * Delete all the completed entries that belong to the feed
     * 
     * @param feedId the feed id
     */
    public void deleteCompletedEntries(long feedId){
        SQLiteDatabase db = null;
        try {
            db = openDataBase();
            Object[] bind = new Object[1];
            bind[0] = new Long(feedId);
            db.execSQL("DELETE FROM entries WHERE feed = ? and state = 1", bind);
            db.close();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (db != null){
                db.close();
            }
        }
        System.out.println("DatabaseHelper.deleteFeedEntries() :: " + feedId);
    }
}
