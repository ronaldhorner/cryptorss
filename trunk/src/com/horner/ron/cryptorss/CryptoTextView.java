package com.horner.ron.cryptorss;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;

public class CryptoTextView extends TextView {
    /** the TextView's displayed letter */
    private String letter = "";
    
    /** the TextView crypto letter */
    private String cryptoLetter = "";
    
    /** flag if the TextView is plain text */
    private boolean plainText = false;
    
    /** flag if the TextView is crypto text */
    private boolean cryptoText = false;
    
    /** flag if the TextView is non alpha text */
    private boolean nonAlpha = false;
    
    /** flag if the TextView is a spacer text */
    private boolean spacer = false;
    
    /** the width in dip for the TextView */
    private int width = 20;
    
    /**
     * Sets up the TextView
     * @param c the context
     */
    public CryptoTextView(Context c){
        super(c);
        
        // Convert the dips to pixels
        setMinimumWidth((int) (getResources().getDisplayMetrics().density * width + 0.5f));
        setTextSize((int) (getResources().getDisplayMetrics().density * 12 + 0.5f));
        setTextColor(Color.WHITE);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setBackgroundColor(Color.DKGRAY);
        setFocusable(true);
        setCursorVisible(false);
        setSingleLine(true);
        setClickable(false);
    }
    
    /**
     * @param letter the letter to be set
     */
    public void setLetter(String letter) {
        this.letter = letter;
    }

    /**
     * @return the letter
     */
    public String getLetter() {
        return letter;
    }

    /**
     * @param plainText set the CryptoTextView to plain text
     */
    public void setPlainText(boolean plainText) {
        this.plainText = plainText;
        if (this.plainText){
            this.setText(" ");
            setTextColor(Color.BLACK);
            setBackgroundColor(Color.WHITE);
        }
    }

    /**
     * @return true if the CryptoTextView is plain text
     */
    public boolean isPlainText() {
        return plainText;
    }

    /**
     * @param cryptoText set the CryptoTextView to crypto
     */
    public void setCryptoText(boolean cryptoText) {
        this.cryptoText = cryptoText;
        if (this.cryptoText) {
            this.setText(this.letter);
        }
    }

    /**
     * @return true if the CryptoTextView is crypto text
     */
    public boolean isCryptoText() {
        return cryptoText;
    }

    /**
     * @param nonAlpha set the CryptoTextView to non alpha
     */
    public void setNonAlpha(boolean nonAlpha) {
        this.nonAlpha = nonAlpha;
        if (this.nonAlpha) {
            this.setText(this.letter);
            this.setBackgroundColor(Color.BLACK);
        }
    }

    /**
     * @return true if the CryptoTextView is non alpha
     */
    public boolean isNonAlpha() {
        return nonAlpha;
    }

    /**
     * @param spacer set the CryptoTextView to spacer
     */
    public void setSpacer(boolean spacer) {
        this.spacer = spacer;
        if (this.spacer) {
            this.setText(" ");
            setHeight(5);
            setBackgroundColor(Color.BLACK);
        }
    }

    /**
     * @return true if the CryptoTextView is a spacer
     */
    public boolean isSpacer() {
        return spacer;
    }

    /**
     * @param cryptoLetter the crypto letter to set
     */
    public void setCryptoLetter(String cryptoLetter) {
        this.cryptoLetter = cryptoLetter;
    }

    /**
     * @return the crypto letter
     */
    public String getCryptoLetter() {
        return cryptoLetter;
    }
    
}
