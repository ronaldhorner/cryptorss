package com.horner.ron.cryptorss;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.horner.ron.cryptorss.database.DatabaseHelper;

public class PuzzleSolved extends Activity {

    /** reference to the DatabaseHelper */
    DatabaseHelper dbh = new DatabaseHelper(this);
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // get the entry id out of the bundle
        Bundle bundle = getIntent().getExtras();
        final long entryId = bundle.getLong("entryId");
        
        // set up the content and the title
        setContentView(R.layout.puzzle_solved);
        setTitle(dbh.getEntry(entryId).getTitle());
        
        // get the time spent and add it to the database 
        long timeSpent = bundle.getLong("time");
        dbh.addTime(timeSpent);
        
        // get the average time
        long avgTime = dbh.getAverageTime();
        
        // set the summary text
        TextView summary = (TextView) findViewById(R.id.summary);
        summary.setText(dbh.getEntry(entryId).getSummary());
        
        // set the score
        TextView score = (TextView) findViewById(R.id.score);
        if (avgTime > 0){
            if (timeSpent < avgTime * 0.70 ) score.setText("A+");
            else if (timeSpent < avgTime * 0.77 ) score.setText("A");
            else if (timeSpent < avgTime * 0.85 ) score.setText("A-");
            else if (timeSpent < avgTime * 0.90 ) score.setText("B+");
            else if (timeSpent < avgTime) score.setText("B");
            else if (timeSpent < avgTime  * 1.20) score.setText("B-");
            else if (timeSpent < avgTime * 1.50) score.setText("C+");
            else if (timeSpent < avgTime * 2.0) score.setText("C");
            else if (timeSpent < avgTime  * 2.70) score.setText("C-");
            else if (timeSpent < avgTime  * 3.60) score.setText("D+");
            else if (timeSpent < avgTime  * 4.7) score.setText("D");
            else if (timeSpent < avgTime  * 6.0) score.setText("D-");
            else score.setText("F");
        } else {
            score.setText("?");
        }
        // set up the time spent
        TextView time = (TextView) findViewById(R.id.time);
        long seconds = timeSpent / 1000;
        long mseconds = timeSpent % 1000;
        time.setText("Time: " + seconds +"."+mseconds +" sec");
        time.setGravity(Gravity.CENTER);
        
        // set up the average time
        if (avgTime > 0){
            TextView avg = (TextView) findViewById(R.id.avg);
            seconds = avgTime / 1000;
            mseconds = avgTime % 1000;
            avg.setText("AVG: " + seconds +"."+mseconds +" sec");
            avg.setGravity(Gravity.CENTER);
        }
        
        // set up the completed button
        Button exit = (Button) findViewById(R.id.done);
        exit.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        // set up the read button
        Button read = (Button) findViewById(R.id.read);
        read.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                Intent loadWebPage = new Intent(Intent.ACTION_VIEW);
                loadWebPage.setData(Uri.parse(dbh.getEntry(entryId).getUrl()));
                startActivity(loadWebPage);
                finish();
            }
        });
        
        // mark this entry as solved
        dbh.setPuzzleSolved(entryId);
        // clear out the puzzle
        Persistence.getInstance().setPuzzle(null);
        
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            finish();
        return true;
    }
}
