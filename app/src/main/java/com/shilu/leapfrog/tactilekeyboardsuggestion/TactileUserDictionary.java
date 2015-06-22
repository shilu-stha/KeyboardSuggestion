package com.shilu.leapfrog.tactilekeyboardsuggestion;

/**
 * Make required communications with UserDictionary
 *
 * @author shilushrestha
 * @date 6/19/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.UserDictionary;

import java.util.ArrayList;
import java.util.List;

public class TactileUserDictionary {
    private static final int FREQ_NONE = -1;
    private static final String APP_ID = "UID_Tactile_Keyboard";
    private static final String WORD_TYPE = "User_Dictionary";
    private final Context context;
    String[] SELECTION_COLUMNS = { UserDictionary.Words._ID, UserDictionary.Words.WORD, UserDictionary.Words.FREQUENCY };
    String SELECTION_CONDITION = UserDictionary.Words.WORD + " LIKE ? ";
    String UPDATE_CONDITION = UserDictionary.Words.WORD + " =?";

    TactileUserDictionary(Context context){
        this.context = context;
    }

    /**
     * Check and adds or updates the word into the userDictionary.
     *
     * @param suggestionToUpdate
     *
     * @author shilushrestha
     */
    public void checkIfFrequencyNeedsToBeUpdated(String suggestionToUpdate) {
        Cursor c = context.getContentResolver().query(UserDictionary.Words.CONTENT_URI, SELECTION_COLUMNS, UPDATE_CONDITION,
                new String[]{suggestionToUpdate}, null);
        c.moveToFirst();
        if (!c.isAfterLast()) {

                int dictFreq = c.getInt(c.getColumnIndex(UserDictionary.Words.FREQUENCY));
                ContentValues contentValue = new ContentValues();
                contentValue.put(UserDictionary.Words.APP_ID, APP_ID);
                contentValue.put(UserDictionary.Words.FREQUENCY, String.valueOf(dictFreq + 1));
                context.getContentResolver().update(UserDictionary.Words.CONTENT_URI, contentValue, UPDATE_CONDITION,
                        new String[]{suggestionToUpdate});
        } else {
            ContentValues contentValue = new ContentValues();
            contentValue.put(UserDictionary.Words.APP_ID, APP_ID);
            contentValue.put(UserDictionary.Words.FREQUENCY, FREQ_NONE);
            contentValue.put(UserDictionary.Words.WORD, suggestionToUpdate);
            context.getContentResolver().insert(UserDictionary.Words.CONTENT_URI, contentValue);
        }
    }

    /**
     * Returns the probable word from userDictionary.
     *
     * @param args
     * @return
     *
     * @author shilushrestha
     */
    public List<DictionaryWrapper> addWordFromDictionary(String[] args){
        List<DictionaryWrapper> suggestionList = new ArrayList<>();

        Cursor c = context.getContentResolver().query(UserDictionary.Words.CONTENT_URI, SELECTION_COLUMNS, SELECTION_CONDITION, args, null);

        c.moveToFirst();
        if (!c.isAfterLast()) {
            //gets the value from the column.
            DictionaryWrapper wrapper = new DictionaryWrapper();
//            wrapper.APP_ID = c.getInt(c.getColumnIndex(UserDictionary.Words.APP_ID));
                wrapper.FREQUENCY = c.getInt(c.getColumnIndex(UserDictionary.Words.FREQUENCY));
                wrapper.ID = c.getInt(c.getColumnIndex(UserDictionary.Words._ID));
                wrapper.WORD = c.getString(c.getColumnIndex(UserDictionary.Words.WORD));
                wrapper.TYPE = WORD_TYPE;
                suggestionList.add(wrapper);
                TactileSpellChecker.temp_suggestions.add(wrapper.WORD);
            }while (c.moveToNext());
       return suggestionList;
    }
}
