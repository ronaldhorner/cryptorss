package com.horner.ron.cryptorss;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import com.horner.ron.cryptorss.database.DatabaseHelper;

/**
 * I guess one could say that this is where the magic happens, but that might be
 * just a little too kind.
 * 
 * @author Ron Horner
 *
 */
public class PuzzleScreen extends Activity {

    /** id for the result of the next puzzle */
    private static final int NEXT_PUZZLE_RESULT = 0;

    /** reference to the in game key board */
    private KeyboardView keyboardView = null;

    /** reference to itself */
    private PuzzleScreen self = null;

    /** reference to the CryptoTableLayout */
    private CryptoTableLayout table = null;

    /** reference to the ScrollView that holds the CryptoTableLayout */
    private ScrollView scrollView = null;

    /** reference to the DatabaseHelper */
    private DatabaseHelper dbh = new DatabaseHelper(this);

    /** reference to the SecureRandom */
    private SecureRandom srd = new SecureRandom();

    private boolean isLaunchPuzzleSolved = false;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        System.out.println("PuzzleScreen.onCreate()");
        super.onCreate(savedInstanceState);
        // set a reference to ourself
        this.self = this;

        // configure the in game keyboard
        setContentView(R.layout.with_keyboard);
        Keyboard keyboard = new Keyboard(this, R.xml.keyboard);
        keyboardView = (KeyboardView) this.findViewById(R.id.playKeyboard);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(new OnKeyboardActionListener() {   
            public void onKey(int primaryCode, int[] keyCodes) {
                long eventTime = System.currentTimeMillis();
                KeyEvent event = new KeyEvent(eventTime, eventTime,
                        KeyEvent.ACTION_DOWN, primaryCode, 0, 0, 0,
                        0, KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                PuzzleScreen.this.onKeyDown(event.getKeyCode(), event);
            }
            
            // we don't use any of these but we need to have them instantiated
            public void onPress(int primaryCode) {  }
            public void onRelease(int primaryCode) {  }
            public void onText(CharSequence text) {  }
            public void swipeDown() {  }
            public void swipeLeft() {  }
            public void swipeRight() {  }
            public void swipeUp() {  }
        });

        // get the orientation
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int orientation = display.getOrientation();

        // hide the keyboard if we don't need it
        if (orientation == 1)
            keyboardView.setVisibility(View.GONE);

        // get the entry id for this puzzle out of the bundle
        Bundle b = this.getIntent().getExtras();
        long entryId = b.getLong("entryId");
        // get the puzzle from the entry id
        Puzzle puzzle = getPuzzle(entryId);
        // set the title
        setTitle(puzzle.getTitle());
        // get ahold of the ScrollView and clear it out
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        scrollView.removeAllViews();
        // get a new CryptoTableLayout with the current puzzle
        table = new CryptoTableLayout(PuzzleScreen.this, puzzle);
        scrollView.addView(table);
        // scroll to the top of the ScrollView
        scrollView.scrollTo(0, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // get the character code of the pressed key
        String c = (char)event.getUnicodeChar() + "";
        // if our current time is 0 then start your engines
        if (Persistence.getInstance().getTime() == 0)
            Persistence.getInstance().setTime(System.currentTimeMillis());

        // check if it was a delete event and if so set the 
        // char to " " so the table can handle the delete
        if (event.getKeyCode() == KeyEvent.KEYCODE_DEL){
            c = " ";  
        } 
        if (event.getKeyCode() == KeyEvent.KEYCODE_SPACE) {
            table.showClue(); // show a clue
            System.out.println("PuzzleScreen.onKeyDown.checkSolution() ::" + table.checkSolution());
            if(table.checkSolution())
                self.launchPuzzleSolved();
        } else if (c.matches("[a-zA-Z\\s]")) { 
            if (table.updateLetters(c)) // update for alpha characters
                self.launchPuzzleSolved();
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT){
            table.scrollArrowPressDownRight(); // move to the next CryptoTextView to the right
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT){
            table.scrollArrowPressUpLeft(); // move to the next CryptoTextView to the left
        } if (event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            Persistence.getInstance().setPuzzle(null); // clean out this puzzle if we cancel out
        }
        return super.onKeyDown(keyCode, event); // finally do the default android action
    }

    /**
     * If the puzzle is solved launch the PuzzleSolved activity
     */
    protected void launchPuzzleSolved() {
        if (!isLaunchPuzzleSolved){
            Intent intent = new Intent(this, PuzzleSolved.class);
    
            Bundle bundle = new Bundle();
            bundle.putLong("time", System.currentTimeMillis()-Persistence.getInstance().getTime());
            bundle.putLong("entryId", this.getIntent().getExtras().getLong("entryId"));
            intent.putExtras(bundle);
    
            this.startActivityForResult(intent, NEXT_PUZZLE_RESULT);
            isLaunchPuzzleSolved = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.puzzle_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
        case R.id.quit_puzzle : // exit the puzzle
            Persistence.getInstance().setPuzzle(null);
            finish();
            return true;
        case R.id.clear_puzzle : // clear out the current guess
            table.clearPuzzle();
            return true;
        case R.id.show_errors : // show errors on the game board
            table.showErrors();
            return true;
        default : // do the default android action
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Take action depending on the result from the PuzzleSolved activity
     * 
     * @param requestCode the request from the PuzzleSolved activity
     * @param resultCode get the result from the PuzzleSolved activity
     * @param data any data sent back
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEXT_PUZZLE_RESULT) {
            if (resultCode == RESULT_OK) {
                
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        // reset our time so that we don't count time the board wasn't displayed against the player
        Persistence.getInstance().setTime(System.currentTimeMillis() - Persistence.getInstance().getTimeSpent());
        Persistence.getInstance().setTimeSpent(0);
        super.onResume();
    }

    @Override
    protected void onPause() {
        // if we pause the game then we need to store the currently spent amount of time
        // so we can calculate it back it when we start back up
        if (Persistence.getInstance().getTime() > 0){
            long timeSpent = System.currentTimeMillis() - Persistence.getInstance().getTime();
            Persistence.getInstance().setTimeSpent(timeSpent);
        }
        super.onPause();
    }

    /**
     * Builds a puzzle based on the supplied entry id
     * 
     * @param entryId the id of the entry for this puzzle
     * @return a fresh puzzle
     */
    public Puzzle getPuzzle(long entryId){
        if (Persistence.getInstance().getPuzzle() != null){
            // if we've already got one just return it
            return Persistence.getInstance().getPuzzle();
        } else {
            // build a fresh puzzle
            Puzzle p = new Puzzle(dbh.getEntry(entryId));
    
            // clip the summary to 200 characters
            String pt = p.getSummary();
            if (pt.length() > 200){
                pt = pt.substring(0, 200);
                if (pt.lastIndexOf(" ") > 0){
                    pt = pt.substring(0, pt.lastIndexOf(" "));
                }
            }
            
            if (dbh.getGameMode() == CryptoTableLayout.NORMAL_MODE){
                // remove all the non word characters
                pt = pt.replaceAll("\\W", "");
            }
            
            // generate the puzzle text
            ArrayList<String> puzzleText = generatePuzzleText(pt);
    
            // generate the crypto map
            HashMap<String, String> cryptoMap = generateCryptoMap(puzzleText);
            p.setCryptoMap(cryptoMap);
    
            // generate the plain text
            ArrayList<CryptoTextView> plainTextViews = generatePlainText(puzzleText, cryptoMap);
            p.setPlainTextViews(plainTextViews);
    
            // stick the puzzle into Persistence
            Persistence.getInstance().setPuzzle(p);
            
            // reset the game times
            Persistence.getInstance().setTime(System.currentTimeMillis());
            Persistence.getInstance().setTimeSpent(0);
            
            return p;
        }
    }

    /**
     * Builds the Puzzle Text Array
     */
    private ArrayList<String> generatePuzzleText(String pt){
        ArrayList<String> puzzleText = new ArrayList<String>();
        for (char p : pt.toCharArray()){
            puzzleText.add(p+"");
        }
        return puzzleText;
    }

    /**
     * Generate the Crypto Mapping 
     *
     * @param pt
     */
    private HashMap<String, String> generateCryptoMap(ArrayList<String> puzzleText) {
        ArrayList<String> letters = new ArrayList<String>(Arrays
                .asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                        "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                        "W", "X", "Y", "Z"));

        HashMap<String, String> cryptoMap = new HashMap<String, String>();
        for (int i = 0; i < puzzleText.size() && letters.size() != 0; i++) {
            String key = puzzleText.get(i).toUpperCase();
            if (key.matches("[a-zA-Z]") && !cryptoMap.containsKey(key)) {
                String value = letters.remove(srd.nextInt(letters.size()));
                cryptoMap.put(key, value);
            }
        }
        return cryptoMap;
    }

    /**
     *  Builds the CryptoLetter objects for the plain text array
     */
    private ArrayList<CryptoTextView> generatePlainText(ArrayList<String> puzzleText, HashMap<String, String> cryptoMap){
        ArrayList<CryptoTextView> plainTextViews = new ArrayList<CryptoTextView>();
        for (String p : puzzleText){
            CryptoTextView cl = new CryptoTextView(this);
            cl.setText("", TextView.BufferType.EDITABLE);
            cl.setLetter(p.toUpperCase());
            if (cl.getLetter().matches("[a-zA-Z]")) {
                cl.setPlainText(true);
                cl.setCryptoLetter(cryptoMap.get(cl.getLetter()));
                cl.setClickable(true);
            }
            else
                cl.setNonAlpha(true);
            plainTextViews.add(cl);
        }
        return plainTextViews;
    }
}
