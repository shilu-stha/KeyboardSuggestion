package com.shilu.leapfrog.tactilekeyboardsuggestion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.UserDictionary;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Make required communications with UserDictionary Table
 *
 */
public class UserDictionaryHelper {

    private static UserDictionaryHelper instance;
    private OnTextSearchCompleteListener listener;
    private HashMap<String, Timestamp> ticket;
    private Context context;

    private static final int FREQ_NONE = -1;
    private static final String APP_ID = "UID_Tactile_Keyboard";
    private static final String WORD_TYPE = "User_Dictionary";
    String[] SELECTION_COLUMNS = {UserDictionary.Words._ID, UserDictionary.Words.WORD, UserDictionary.Words.FREQUENCY};
    String SELECTION_CONDITION = UserDictionary.Words.WORD + " LIKE ? ";
    String UPDATE_CONDITION = UserDictionary.Words.WORD + " =?";

    private UserDictionaryHelper(Context context, OnTextSearchCompleteListener listener){
        this.listener = listener;
        this.context = context;
    }

    public static UserDictionaryHelper getInstance(Context context, OnTextSearchCompleteListener listener){
        if(instance==null){
            instance = new UserDictionaryHelper(context, listener);
        }
        return instance;
    }

    /**
     * Get suggestions from User Dictionary
     *
     * @param word
     * @param ticket
     */
    public void getSuggestions(String word, HashMap<String, Timestamp> ticket) {
        this.ticket =ticket;

        ArrayList<DictionaryWrapper> suggestionList = new ArrayList<>();

        Cursor c = context.getContentResolver().query(UserDictionary.Words.CONTENT_URI, SELECTION_COLUMNS, SELECTION_CONDITION, new String[]{word}, null);

        c.moveToFirst();
        while (!c.isAfterLast()) {
            DictionaryWrapper wrapper = new DictionaryWrapper();
//            wrapper.appId = c.getInt(c.getColumnIndex(UserDictionary.Words.appId));
            wrapper.frequency = c.getInt(c.getColumnIndex(UserDictionary.Words.FREQUENCY));
            wrapper.id = c.getInt(c.getColumnIndex(UserDictionary.Words._ID));
            wrapper.word = c.getString(c.getColumnIndex(UserDictionary.Words.WORD));
            wrapper.type = WORD_TYPE;
            suggestionList.add(wrapper);
            c.moveToNext();
        }
        listener.onTextSearchComplete(word, ticket, suggestionList);
    }

    /**
     * Check and adds or updates the word into the userDictionary.
     *
     * @param suggestionToUpdate
     */
    public void updateNewEntry(String suggestionToUpdate) {
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


}
