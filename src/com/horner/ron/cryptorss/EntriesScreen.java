package com.horner.ron.cryptorss;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.horner.ron.cryptorss.database.DatabaseHelper;
import com.horner.ron.cryptorss.feed.EntryListAdapter;
import com.horner.ron.cryptorss.feed.RssEntry;

public class EntriesScreen extends Activity {
    
    /** global reference to the database helper */
    DatabaseHelper dh = new DatabaseHelper(this);
    
    /** the associated feed */
    private long feedId = -1;
    
    /** id for the delete entry context menu option */
    private static final int DELETE_ENTRY = 101;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entries);
        System.out.println("EntriesScreen.onCreate()");
        Bundle b = this.getIntent().getExtras();
        feedId = b.getLong("feedId");
        // set the title to the name of the feed
        setTitle(dh.getFeed(feedId).getName());
        updateList();
    }
    
    /**
     * Rebuilds the list of entries.  So far it's the only way I've found to
     * get the displayed list to get updated.
     */
    private void updateList(){
        final ListView lv = (ListView)findViewById(R.id.entries);
        
        EntryListAdapter fla = new EntryListAdapter(this, dh.getEntries(feedId));
        lv.setAdapter(fla);
        lv.setOnItemClickListener(new OnItemClickListener(){
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                RssEntry entry = (RssEntry)lv.getItemAtPosition(pos);
                Bundle b = new Bundle();
                b.putLong("entryId", entry.getId());
                
                Intent intent = new Intent(EntriesScreen.this, PuzzleScreen.class);
                intent.putExtras(b);
                
                EntriesScreen.this.startActivity(intent);
            }
        });
        lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener(){
            public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
                menu.setHeaderTitle("Entry Actions");
                menu.add(0, DELETE_ENTRY, 0, "Delete");
            }
        });
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        ListView lv = (ListView)findViewById(R.id.entries);
        RssEntry entry = (RssEntry)lv.getItemAtPosition(menuInfo.position);
        long entryId = entry.getId();
        
        switch (item.getItemId()) {
            case DELETE_ENTRY :
                dh.deleteEntry(entryId);
                updateList();
                return true;
        }
        return false;
    }
    
    @Override
    protected void onResume() {
        updateList();
        System.out.println("EntriesScreen.OnResume()");
        super.onResume();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.entry_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
        case R.id.remove_entries_menu :  // clean up all the finished entries
            DatabaseHelper dbh = new DatabaseHelper(this);
            dbh.deleteCompletedEntries(feedId);
            updateList();
            return true;
        default :
            return super.onOptionsItemSelected(item);
        }
    }
}
