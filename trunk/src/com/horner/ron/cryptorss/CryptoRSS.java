package com.horner.ron.cryptorss;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import uk.org.catnip.eddie.Entry;
import uk.org.catnip.eddie.FeedData;
import uk.org.catnip.eddie.parser.Parser;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.horner.ron.cryptorss.database.DatabaseHelper;
import com.horner.ron.cryptorss.feed.FeedListAdapter;
import com.horner.ron.cryptorss.feed.RssFeed;


/**
 * This is the first screen displayed.  It does all the heavy lifting
 * for managing feeds, adding, removing, updating, and renaming
 * 
 * @author Ron Horner
 *
 */
public class CryptoRSS extends Activity {
    
    /** id of the update context menu option */
    private static final int UPDATE_FEED = 100;
    
    /** id of the delete context menu option */
    private static final int DELETE_FEED = 101;
    
    /** id for the progress dialog */
    private static final int PROGRESS_DIALOG = 102;
    
    /** id for the add dialog */
    private static final int ADD_DIALOG = 103;
    
    /** id for the rename dialog */
    private static final int RENAME_DIALOG = 104;

    /** id for the rename context menu option */
    private static final int RENAME_FEED = 105;
    
    /** id for the game mode dialog */
    private static final int MODE_DIALOG = 106;
    
    /** id for the splash dialog */
    private static final int SPLASH_DIALOG = 107;
    
    /** global reference to the Eddie RSS Parser */
    private Parser parser = new Parser();
    
    /** global reference to the DatabaseHelper */
    DatabaseHelper dh = new DatabaseHelper(this);

    /** global reference to the progress thread */
    private ProgressThread progressThread;

    /** global reference to the progress dialog */
    private ProgressDialog progressDialog;

    /** the url to use when doing an add or update */
    private String progressURL;
    
    /** the name to use when updating a feed name */
    private RssFeed renameRssFeed;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // set up the list view and add our context menu options
        ListView lv = (ListView)findViewById(R.id.feeds);
        lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener(){
            public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
                menu.setHeaderTitle("Feed Actions");
                menu.add(0, UPDATE_FEED, 0, "Update");
                menu.add(0, RENAME_FEED, 0, "Rename");
                menu.add(0, DELETE_FEED, 0, "Delete");
            }
        });
        
        updateList();
        
        showDialog(SPLASH_DIALOG);
    }
    
    @Override
    protected void onResume() {
        updateList();
        super.onResume();
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // get the selected RssFeed option
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        ListView lv = (ListView)findViewById(R.id.feeds);
        RssFeed feed = (RssFeed)lv.getItemAtPosition(menuInfo.position);
        long feedId = feed.getId();
        
        switch (item.getItemId()) {
            case UPDATE_FEED : // fire off the progressDialog to update the feed
                progressURL = feed.getUrl();
                showDialog(PROGRESS_DIALOG);
                return true;
            case RENAME_FEED : // fire off the rename dialog to rename the feed
                renameRssFeed = feed;
                showDialog(RENAME_DIALOG);
                return true;
            case DELETE_FEED : // delete the feed
                dh.deleteFeed(feedId);
                updateList();
                return true;
        }
        return false;
    }
    
    /**
     * Rebuilds the list.  There's probably better ways to get things to redraw, but 
     * this method seems to work just fine.
     */
    private void updateList(){
        final ListView lv = (ListView)findViewById(R.id.feeds);
        FeedListAdapter fla = new FeedListAdapter(this, dh.getFeeds());
        lv.setAdapter(fla);
        lv.setOnItemClickListener(new OnItemClickListener(){
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                RssFeed feed = (RssFeed)lv.getItemAtPosition(pos);
                Bundle b = new Bundle();
                b.putLong("feedId", feed.getId());
                
                Intent intent = new Intent(CryptoRSS.this, EntriesScreen.class);
                intent.putExtras(b);
                
                CryptoRSS.this.startActivity(intent);
            }
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
        case R.id.add_feed_menu : // fire off the add feed dialog
            showDialog(ADD_DIALOG);
            return true;
        case R.id.faq_feed_menu : // launch the Faq activity 
            Intent intent = new Intent(CryptoRSS.this, Faq.class);
            CryptoRSS.this.startActivity(intent);
            return true;
        case R.id.mode_feed_menu : // fire off the add feed dialog
            showDialog(MODE_DIALOG);
            return true;
        default :
            return super.onOptionsItemSelected(item);
        }
    }
    
    
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == ADD_DIALOG){
            // create a basic dialog to handle getting an rss feed url
            // from the user
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter an RSS Feed");
            builder.setCancelable(true);
            final EditText rssfeed = new EditText(this);
            rssfeed.setText("http://");  // prepopulate with http://
            builder.setView(rssfeed);
            
            // set the positive button to launch the progress dialog
            // to add the feed and its entries
            builder.setPositiveButton("Add", new OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1) {
                    progressURL = rssfeed.getText().toString();
                    showDialog(PROGRESS_DIALOG);
                }
            });
            // set the negative button to dismiss the add dialog
            builder.setNegativeButton("Cancel", new OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1) {
                    dismissDialog(ADD_DIALOG);
                }
            });
            return builder.create();
        } else if (id == PROGRESS_DIALOG){
            // set up the progress dialog
            progressDialog = new ProgressDialog(CryptoRSS.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Loading...");
            // setup a dismiss listener so we can shutdown the thread
            progressDialog.setOnDismissListener(new OnDismissListener(){
                public void onDismiss(DialogInterface dialog) {
                    // Make sure the thread is interupted.
                    progressThread.cancelAction();
                    progressThread.interrupt();
                }
            });
            // build and fire off the progress thread
            progressThread = new ProgressThread(handler, progressURL);
            progressThread.start();
            return progressDialog;
        } else if (id == RENAME_DIALOG){
            // build a basic dialog to get a new name from the user
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter an New Name");
            builder.setCancelable(true);
            
            // set the input to the old name
            final EditText name = new EditText(this);
            name.setText(renameRssFeed.getName());
            
            builder.setView(name);
            
            // set up the positive button to make the change and 
            // force a list update
            builder.setPositiveButton("Rename", new OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1) {
                    dh.setFeedName(renameRssFeed.getId(), name.getText().toString());
                    updateList();
                    removeDialog(RENAME_DIALOG);
                }
            });
            // set up the negative button to completely remove the dialog
            builder.setNegativeButton("Cancel", new OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1) {
                    removeDialog(RENAME_DIALOG);
                }
            });
            return builder.create();
        } else if (id == MODE_DIALOG){
            // create a basic dialog to handle getting an rss feed url
            // from the user
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select a Game Mode");
            builder.setCancelable(true);
            final CharSequence[] items = {"Easy", "Normal"};

            builder.setSingleChoiceItems(items, dh.getGameMode(), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (item == 0){
                        dh.setEasyMode();
                    } else if (item == 1){
                        dh.setNormalMode();
                    }
                    dismissDialog(MODE_DIALOG);
                }
            });

            return builder.create();
        } else if (id == SPLASH_DIALOG && dh.getShowSplash() == 1){
            Dialog dialog = new Dialog(this);
            // set up the content and the title
            dialog.setContentView(R.layout.splash);
            dialog.setTitle("Welcome to CryptoRSS");
            
            final DatabaseHelper dh = new DatabaseHelper(this);
            
            TextView gameMode = (TextView) dialog.findViewById(R.id.splash_game_mode);
            if (dh.getGameMode() == 0)
                gameMode.setText(gameMode.getText() + "Easy");
            
            if (dh.getGameMode() == 1)
                gameMode.setText(gameMode.getText() + "Normal");
            
            final CheckBox checked = (CheckBox) dialog.findViewById(R.id.splash_checkbox);
            checked.setChecked(dh.getShowSplash() == 1);
            
            Button save = (Button) dialog.findViewById(R.id.splash_button);
            save.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                    if (checked.isChecked()){
                        dh.setShowSplash();
                    } else {
                        dh.setHideSplash();
                    }
                    dismissDialog(SPLASH_DIALOG);
                }
            });

            return dialog;
        } else { 
            // if it's not our dialog, show it.
            return super.onCreateDialog(id);
        }
    }
    
    /**
     * Inserts a new entry into the database.  The entry is stripped of all HTML
     * 
     * @param feedId the feed id
     * @param e the RssEntry to be added
     * @return true if the feed had all the necessary data
     */
    private boolean insertEntry(long feedId, Entry e){
        boolean rval = false;
        String d = e.get("description");
        String t = e.getTitle().getValue();
        String l = e.get("link");
        // check to make sure all the needed info is present
        if (d != null && d.length() > 0 && t != null && t.length() > 0 && l != null && l.length() > 0){
            // strip out the annoying HTML entities like &#8217; 
            d = Html.fromHtml(d).toString(); 
            t = Html.fromHtml(t).toString();
            dh.addEntry(feedId, t, d, l); // add the entry to the database
            rval = true;
        }
        return rval;
    }
    
    
    /**
     * Sets up a new handler to handle messages from the progress thread
     */
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String m = msg.getData().getString("message");
            // check to see if the thread has finished
            // and if so, remove the progress dialog
            if (m.equals("done")){
                updateList();
                dismissDialog(PROGRESS_DIALOG);
                removeDialog(PROGRESS_DIALOG);
            } else {
                // otherwise update the progress dialog with the 
                // current status of the thread
                progressDialog.setMessage(m);
            }
        }
    };

    /** Nested class that parses the RSS feed */
    private class ProgressThread extends Thread {
        /** reference to the CryptoRSS handler */
        Handler mHandler;
        
        /** the url to parse */
        String url;
        
        /** the state of the Thread */
        boolean running = true;
        
        /**
         * Basic Constructor
         * @param h the handler
         * @param u the url
         */
        ProgressThread(Handler h, String u) {
            mHandler = h;
            url = u;
        }
       
        /**
         * Parses the thread.  Checks to see if it was interrupted
         * at every interruptable spot in the parsing.
         */
        public void run() {
            InputStream in = null;
            try {
                if (running){
                    sendMessage("Parsing The Feed");
                    // parse the feed
                    URL u = new URL(url);
                    in = u.openStream();
                    FeedData feed = parser.parse(in);
                    in.close();
                    // send the parse finished message to the handler
                    sendMessage("Completed Parsing The Feed");
                    if (running){
                        // strip out the annoying stuff and add it to the database
                        String t = Html.fromHtml(feed.getTitle().getValue()).toString();
                        long feedId = dh.addFeed(t, url);
                        Iterator i = feed.entries();
                        while (i.hasNext()){
                            // add the entries to the database
                            if (running){
                                Entry e = (Entry)i.next();
                                if (insertEntry(feedId, e))
                                    sendMessage("Added " + e.getTitle().getValue());
                            }
                        }
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                sendMessage("The RSS Feed URL Is Incorrect");
            } catch (IOException e) {
                e.printStackTrace();
                sendMessage("The RSS Feed Was Unable To Be Downloaded");
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("The ProgressThread was probably interupted");
            } finally {
                try {
                if (in != null)
                    in.close();
                } catch (IOException ioe){
                    ioe.printStackTrace();
                }
            }
            // when we finish send the done message
            sendMessage("done");
        }
        
        /**
         * Convenience function to send the messages to the handler
         * @param m the message to be sent
         */
        private void sendMessage(String m){
            Message msg = mHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putString("message", m);
            msg.setData(b);
            mHandler.sendMessage(msg);
        }
        
        /**
         * Stop this thread at its next interruptable spot
         */
        public void cancelAction(){
            this.running = false;
        }
        
    }

}