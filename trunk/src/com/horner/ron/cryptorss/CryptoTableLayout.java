package com.horner.ron.cryptorss;

import java.util.ArrayList;
import java.util.HashMap;

import com.horner.ron.cryptorss.database.DatabaseHelper;

import android.content.Context;
import android.graphics.Color;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

public class CryptoTableLayout extends TableLayout {

    /** a context so we can draw our objects */
    private Context context = null;
    
    /** a hash of the plaintext to crypto mapping */
    private HashMap<String, String> cryptoMap = new HashMap<String, String>();

    /** a reference to all the plain text views on the board */
    private ArrayList<CryptoTextView> plainTextViews = new ArrayList<CryptoTextView>();

    /** a reference to all the crypto text views on the board */
    private ArrayList<CryptoTextView> cryptoTextViews = new ArrayList<CryptoTextView>();

    /** the currently focus plain text */
    private CryptoTextView hasFocus = null;
    
    /** the size of the board rows */
    private int ROWSIZE = 0;
    
    /** the number of used clues */
    private int clueCount = 0;
    
    /** the row width if in portrait mode */
    private final int P_WIDTH = 14;
    
    /** the row width if in landscape mode */
    private final int L_WIDTH = 24;
    
    /** the size of letter blocks */
    private final int BLOCK_SIZE = 4;
    
    /** the size of letter blocks */
    public static final int EASY_MODE = 0;
    
    /** the size of letter blocks */
    public static final int NORMAL_MODE = 1;
    
    /**
     * This constructor does all the necessary configuration for the puzzle board
     * 
     * @param ctx the context
     * @param puzzle the puzzle to render
     */
    public CryptoTableLayout(Context ctx, Puzzle puzzle) {
        super(ctx);
        this.context = ctx;
        setStretchAllColumns(true);
        setPadding(0, 5, 0, 0);
        
        // figure out which way the screen is oriented and set the ROWSIZE accordingly
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if (display.getOrientation() == 1)
            ROWSIZE =  L_WIDTH;
        else
            ROWSIZE =  P_WIDTH;
        
        // get our references to the already generated views
        plainTextViews = puzzle.getPlainTextViews();
        cryptoMap = puzzle.getCryptoMap();
        
        // remove all children from the table and reinitialize
        for (int i = 0; i < plainTextViews.size(); i++){
            setCryptoTextViewOnTouchListener(plainTextViews.get(i));
            if (plainTextViews.get(i).getParent() != null){
                ViewParent parent = plainTextViews.get(i).getParent();
                
                if (parent instanceof TableRow && ((TableRow)parent).getChildCount() > 0) {
                    ((ViewGroup) parent).removeAllViews();
                } 
            }
        }
        // remove all the views from the table
        removeAllViews();
        
        DatabaseHelper dbh = new DatabaseHelper(context);
        
        if (dbh.getGameMode() == EASY_MODE){
            // remove any extra spaces from the puzzle text
            removeExtraSpacesFromPuzzleText();
        }
        if (dbh.getGameMode() == NORMAL_MODE){   
            removeAllSpacesFromPuzzleText();
            // re-layout the puzzle text
            generatePuzzleTextLayout();
        }
        
        // pad out the puzzle text for cleaner display
        padPuzzleText();
        
        // generate the crypto layout as a mirror of the plain text layout
        generateCryptoLayout();
        
        // finally initialize the table
        initializeTable();
    }
    
    /**
     * Updates the backgrounds.  It sets all plain text CryptoTextViews
     * to white.  Then sets all CryptTextViews that have the same 
     * plain text to light gray.  And finally sets the CryptoTextView
     * with focus to yellow.
     */
    public void updateBackgrounds(){
        for (int i = 0; i < plainTextViews.size(); i++){
            CryptoTextView cl = plainTextViews.get(i);
            
            if (cl.isPlainText()){
                cl.setTextColor(Color.BLACK);
                cl.setBackgroundColor(Color.WHITE);
            }
            if (cl.isPlainText() && cl.getLetter().equals(hasFocus.getLetter())){
                cl.setBackgroundColor(Color.LTGRAY);
            }
        }
        hasFocus.setBackgroundColor(Color.YELLOW);
        hasFocus.requestFocus();
    }

    /**
     * Loops through all CryptoTextViews and sets their text
     * to the inputted character if their plain text matches
     * the hasFocus plain text.  It then advances through the
     * list of plain text CryptoTextViews to find the next
     * one to give focus and scrolls to it.
     * 
     * @param cha The character to set the CrytpoTextView to
     * @return true if the puzzle is solved
     */
    public boolean updateLetters(String cha){
        if (cha.matches("[a-zA-Z]"))
            cha = cha.toUpperCase();
        else 
            cha = " ";
        this.hasFocus.setText(cha);
        this.hasFocus.refreshDrawableState();
        for (CryptoTextView cl : plainTextViews){
            if ( cl.getCryptoLetter().equals(this.hasFocus.getCryptoLetter())){
                cl.setText(cha);
            }
        }
        scrollToNextFocusable();
        
        return checkSolution();
    }
    
    /**
     * Searches for the next blank plain text view
     */
    public void scrollToNextFocusable(){
        // check if the currently focused plain text view is blank
        // this check is to keep it from advancing during a delete action
        if (!this.hasFocus.getText().toString().equals("") && !this.hasFocus.getText().toString().equals(" ")){
            // if it's not then we need to find the next one
            // start by searching from right after the currently focused
            boolean foundNextFocusable = false;
            for (int i = 0; i < plainTextViews.size(); i++){
                CryptoTextView cl = plainTextViews.get(i);
                if (cl.isPlainText() && plainTextViews.indexOf(cl) > plainTextViews.indexOf(hasFocus)){
                    CharSequence ssb = cl.getText();
                    if (ssb.toString().equals(" ")){
                        foundNextFocusable = true;
                        this.hasFocus = plainTextViews.get(i);
                        scrollToHasFocus();
                        break;
                    }
                }
            }
            // if we didn't find one ahead of it, let's find the next one behind it
            // starting from the first view to the current one
            if (!foundNextFocusable){
                for (int i = 0; i < plainTextViews.size(); i++){
                    CryptoTextView cl = plainTextViews.get(i);
                    if (cl.isPlainText()){
                        CharSequence ssb = cl.getText();
                        if (ssb.toString().equals(" ")){
                            foundNextFocusable = true;
                            this.hasFocus = plainTextViews.get(i);
                            scrollToHasFocus();
                            break;
                        }
                    }
                }
            }
            // update the backgrounds
            updateBackgrounds();
        }
    }
    
    /**
     * This function scrolls to the next focusable plain text view
     * and places it in the middle of the scroll view if it can
     */
    private void scrollToHasFocus(){
        int[] location = new int[2];
        // safety check and calculate the location to scroll to
        if (hasFocus.getParent() instanceof TableRow){
            Integer row = (Integer) ((TableRow)hasFocus.getParent()).getTag(R.id.row_number);
            location[1] = row.intValue() * (hasFocus.getHeight() * 2 + 5);
            System.out.println("SCROLL TO :: " + location[1]);
        }
        // safety check and set the smooth scroll
        if (this.getParent() instanceof ScrollView){
            ScrollView sv = (ScrollView)this.getParent();
            sv.smoothScrollTo(location[0], location[1] - sv.getHeight()/2);
        }
    }
    
    /**
     * Moves the focused TextView to the previous text view.  Used in conjunction
     * with the up and left arrows on the keyboard.
     */
    public void scrollArrowPressUpLeft(){
        // see if we can move to the left
        boolean foundNextFocusable = false;
        for (int i = plainTextViews.indexOf(hasFocus)-1; i >=0; i--){
            CryptoTextView cl = plainTextViews.get(i);
            if (cl.isPlainText()){
                this.hasFocus = plainTextViews.get(i);
                foundNextFocusable = true;
                scrollToHasFocus();
                break;
            }
        }
        // if we can't move to the next spot starting at the end going left
        if (!foundNextFocusable){
            for (int i = plainTextViews.size()-1; i >= 0; i--){
                //System.out.println("Looking...");
                CryptoTextView cl = plainTextViews.get(i);
                if (cl.isPlainText()){
                    this.hasFocus = plainTextViews.get(i);
                    foundNextFocusable = true;
                    scrollToHasFocus();
                    //System.out.println("Found one");
                    break;
                }
            }
        }
        // update the backgrounds
        updateBackgrounds();
    }
    
    /**
     * Moves the focused TextView to the next text view. Used in conjunction
     * with the down and right arrows on the keyboard.
     */
    public void scrollArrowPressDownRight(){
        //see if we can move to the right
        boolean foundNextFocusable = false;
        for (int i = plainTextViews.indexOf(hasFocus)+1; i < plainTextViews.size(); i++){
            CryptoTextView cl = plainTextViews.get(i);
            if (cl.isPlainText()){
                this.hasFocus = plainTextViews.get(i);
                foundNextFocusable = true;
                scrollToHasFocus();
                break;
            }
        }

        // and if we can't try moving to right starting at the beginning
        if (!foundNextFocusable){
            for (int i = 0; i < plainTextViews.size(); i++){
                CryptoTextView cl = plainTextViews.get(i);
                if (cl.isPlainText()){
                    this.hasFocus = plainTextViews.get(i);
                    foundNextFocusable = true;
                    scrollToHasFocus();
                    break;
                }
            }
        }
        updateBackgrounds();
    }
    
    /**
     * Loops through the plain text CryptoTextViews to check
     * if the solution is correct
     * 
     * @return true if the solution is correct
     */
    public boolean checkSolution(){
        boolean rval = false;
        
        // check if the puzzle is filled in
        boolean filledIn = true;
        for (int i = 0; i < plainTextViews.size() && filledIn ; i++){
            CryptoTextView cl = plainTextViews.get(i);
            if (cl.isPlainText() && cl.getText().toString().equals(" ")){
                filledIn = false;
            }
        }
        
        // if we are filled in check to see if the answer is correct
        if (filledIn){
            System.out.println("Puzzle is complete");
            boolean solved = true;
            for (int i = 0; i < plainTextViews.size() && solved ; i++){
                CryptoTextView cl = plainTextViews.get(i);
                if (!cl.getText().toString().equals(cl.getLetter())){
                    solved = false;
                }
            }
            if (solved){
                rval = true;
            }
        }
        return rval;
    }
    
    /**
     * Fills in the appropriate CryptoTextView for the corresponding clue.
     * Adds time penalties starting with the seventh clue
     * 
     * @return true if a clue was displayed
     */
    public boolean showClue(){
        boolean rval = false;
        String showChar = "";
        switch (clueCount) {
            case 0 : showChar = "R";
                break;
            case 1 : showChar = "S";
                break;
            case 2 : showChar = "T";
                break;
            case 3 : showChar = "L";
                break;
            case 4 : showChar = "N";
                break;
            case 5 : showChar = "E";
                break;
            case 6 : showChar = "A";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 10000);
                break;
            case 7 : showChar = "I";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 10000);
                break;
            case 8 : showChar = "O";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 15000);
                break;
            case 9 : showChar = "U";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 15000);
                break;
            case 10 : showChar = "H";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 20000);
                break;    
            case 11 : showChar = "D"; 
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 20000);
                break; 
            case 12 : showChar = "C";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 60000);
                break;
            case 13 : showChar = "W";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 60000);
                break;
            case 14 : showChar = "M";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 60000);
                break;
            case 15 : showChar = "F";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 60000);
                break;
            case 16 : showChar = "Y";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 60000);
                break;
            case 17 : showChar = "G";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 60000);
                break;
            case 18 : showChar = "P";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 60000);
                break;
            case 19 : showChar = "B";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 60000);
                break;
            case 20 : showChar = "V";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 60000);
                break;
            case 21 : showChar = "K";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 60000);
                break;
            case 22 : showChar = "J";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 60000);
                break;
            case 23 : showChar = "X";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 60000);
                break;
            case 24 : showChar = "Q";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 60000);
                break;
            case 25 : showChar = "Z";
                Persistence.getInstance().setTime(Persistence.getInstance().getTime() - 60000);
                break;
                
        }
       
        // if we have a clue to display then we need to update all the plain text views
        // that have the same crypto letter
        if (!showChar.equals("")){
            for (CryptoTextView cl : plainTextViews){
                if (cl.getLetter().equals(showChar))
                    cl.setText(showChar);
            }
            rval = true;
            clueCount++;
        }
        // update our backgrounds and scroll to the next focusable
        updateBackgrounds();
        scrollToNextFocusable();
        return rval;
    }
    
    
    
    /**
     * Strips out all the spaces from the plain text so that it can be 
     * put into blocks correctly
     */
    private void removeAllSpacesFromPuzzleText(){
        // clean up any extra spaces in the plain text
        for (int i = 0; i < plainTextViews.size()-1;){
            String curr = plainTextViews.get(i).getLetter();
            if ((curr.equals("") || curr.matches("[\\s]+"))){
                plainTextViews.remove(i);
            } else {
                i++;
            }
        }
    }
    
    private void removeExtraSpacesFromPuzzleText(){
        // clean up any extra spaces in the plain text
           for (int i = 0; i < plainTextViews.size()-1;){
               String curr = plainTextViews.get(i).getLetter();
               String next = plainTextViews.get(i+1).getLetter();
               if ((curr.equals("") || curr.matches("[\\s]+"))
                       && (next.equals("") || next.matches("[\\s]+"))){
                   plainTextViews.remove(i+1);
               } else {
                   i++;
               }
           }
       }
    
    /**
     * Pads out the plain text array to be divisible by ROWSIZE
     */
    private void padPuzzleText(){
        while(plainTextViews.size() % ROWSIZE != 0){
            plainTextViews.add(getSpacerLetter());
        }
    }
    
    /**
     * Generate the Plain Text Layout into blocks of 5
     */
    private void generatePuzzleTextLayout() {
        
        for (int i = BLOCK_SIZE; i < plainTextViews.size();){
            // add the first space for the row
            if (i < plainTextViews.size()){
                plainTextViews.add(i, getSpacerLetter());
                i += (BLOCK_SIZE +1);
            }
            
            // add another space if we are in wide view
            if (i < plainTextViews.size() && ROWSIZE == L_WIDTH){
                plainTextViews.add(i, getSpacerLetter());
                i += (BLOCK_SIZE +1);
            }
            
            // add another space if we are in wide view
            if (i < plainTextViews.size() && ROWSIZE == L_WIDTH){
                plainTextViews.add(i, getSpacerLetter());
                i += (BLOCK_SIZE +1);
            }
            
            // add the last space for the row
            if (i < plainTextViews.size()){
                plainTextViews.add(i, getSpacerLetter());
                i += (BLOCK_SIZE * 2 + 1);
            }
        }
    }
    
    /**
     * Sets the on touch listener for the plain text view
     * @param ctv the plain text view
     */
    public void setCryptoTextViewOnTouchListener(CryptoTextView ctv){
        ctv.setOnTouchListener(new OnTouchListener(){
            public boolean onTouch(View obj, MotionEvent arg1) {
                if (obj instanceof CryptoTextView && ((CryptoTextView) obj).isPlainText()){
                    hasFocus = (CryptoTextView) obj;
                    updateBackgrounds();
                }
                return false;
            }
        });
    }
    
    /**
     * Builds the crypto layout as a mirror of the plain text layout
     */
    private void generateCryptoLayout() {
        cryptoTextViews.clear();
        for (CryptoTextView s : plainTextViews) {
            CryptoTextView cl = new CryptoTextView(context);
            if (s.getLetter().matches("[a-zA-Z]")){
                cl.setLetter(cryptoMap.get(s.getLetter()));
                cl.setText(cl.getLetter());
                cl.setCryptoText(true);
            } else {
                cl.setLetter(s.getLetter());
                cl.setText(cl.getLetter());
                cl.setNonAlpha(true);
            }
            
            cryptoTextViews.add(cl);
        }
    }

    /**
     * Get a spacer letter
     * @return a blank CryptoTextView
     */
    private CryptoTextView getSpacerLetter(){
        CryptoTextView cl = new CryptoTextView(context);
        cl.setLetter(" ");
        cl.setSpacer(true);
        return cl;
    }
    
    /**
     * Translates the PlainTextViews and CryptoTextViews into table rows 
     * of length ROWSIZE
     */
    private void initializeTable(){
        int cnt = 0;
        for (int start = 0; start < plainTextViews.size(); cnt++){
            int end = (plainTextViews.size()  > start + ROWSIZE)? start + ROWSIZE : plainTextViews.size();
            TableRow ptRow = new TableRow(this.context);
            ptRow.setTag(R.id.row_number, new Integer(cnt));
            TableRow ctRow = new TableRow(this.context);
            TableRow spRow = new TableRow(this.context);
                        
            for (int i = start; i < end; i++){
                ptRow.addView(plainTextViews.get(i));
                View border = new View(this.context);
                border.setMinimumWidth(1);
                border.setBackgroundColor(Color.BLACK);
                ptRow.addView(border);
                
                ctRow.addView(cryptoTextViews.get(i));
                border = new View(this.context);
                border.setMinimumWidth(1);
                border.setBackgroundColor(Color.BLACK);
                ctRow.addView(border);
                
                spRow.addView(getSpacerLetter());
                border = new View(this.context);
                border.setMinimumWidth(1);
                border.setBackgroundColor(Color.DKGRAY);
                spRow.addView(border);
            }   
            
            this.addView(ptRow);
            this.addView(ctRow);
            this.addView(spRow);
            
            start = end;
        }
        // set our focus to the first plainTextView
        this.hasFocus = plainTextViews.get(0);
        scrollToNextFocusable();
        updateBackgrounds();
    }    
    
    /**
     * Remove all the letters from the plainTextViews
     */
    public void clearPuzzle(){
        for (CryptoTextView ctv : plainTextViews){
            if (ctv.isPlainText())
                ctv.setText(" ");
        }
        clueCount = 0;
    }
    
    /**
     * Highlight all the incorrect plain text in red
     */
    public void showErrors(){
        for (CryptoTextView ctv : plainTextViews){
            if (ctv.isPlainText()){
                String t = ctv.getText().toString();
                if (!t.equals("") && !t.equals(" ") && !t.equals(ctv.getLetter())){
                    ctv.setTextColor(Color.RED);
                }
            }
        }
    }
}
