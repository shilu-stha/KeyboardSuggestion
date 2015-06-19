package com.shilu.leapfrog.tactilekeyboardsuggestion;

/**
 * Created by leapfrog on 6/19/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.UserDictionary;

import java.util.ArrayList;
import java.util.List;

public class TactileUserDictionary {
    private final Context context;
    String[] SELECTION_COLUMNS = { UserDictionary.Words._ID, UserDictionary.Words.WORD, UserDictionary.Words.FREQUENCY };
    String SELECTION_CONDITION = UserDictionary.Words.WORD + " LIKE ? ";
    String UPDATE_CONDITION = UserDictionary.Words.WORD + " =?";

    TactileUserDictionary(Context context){
        this.context = context;
    }
    /**
     * Check if frequency of the userDictionary needs to be increased.
     *
     * @param suggestionToUpdate
     * @author Shilu
     */
    public void checkIfFrequencyNeedsToBeUpdated(String suggestionToUpdate) {
        Cursor c = context.getContentResolver().query(UserDictionary.Words.CONTENT_URI, SELECTION_COLUMNS, UPDATE_CONDITION,
                new String[]{suggestionToUpdate}, null);
        c.moveToFirst();
        if (!c.isAfterLast()) {

                android.util.Log.e("update freq == ", suggestionToUpdate);
                int dictFreq = c.getInt(c.getColumnIndex(UserDictionary.Words.FREQUENCY));
                android.util.Log.e("update freq table == ", dictFreq + "");
                ContentValues contentValue = new ContentValues();
                contentValue.put(UserDictionary.Words.APP_ID, "UID_Tactile_Keyboard");
                contentValue.put(UserDictionary.Words.FREQUENCY, String.valueOf(dictFreq + 1));
                context.getContentResolver().update(UserDictionary.Words.CONTENT_URI, contentValue, UPDATE_CONDITION,
                        new String[]{suggestionToUpdate});
        } else {
            android.util.Log.e("add freq == ", suggestionToUpdate);
            ContentValues contentValue = new ContentValues();
            contentValue.put(UserDictionary.Words.APP_ID, "UID_Tactile_Keyboard");
            contentValue.put(UserDictionary.Words.FREQUENCY, "10");
            contentValue.put(UserDictionary.Words.WORD, suggestionToUpdate);
            context.getContentResolver().insert(UserDictionary.Words.CONTENT_URI, contentValue);
        }
    }

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

            suggestionList.add(wrapper);
        }
       return suggestionList;
    }
}
