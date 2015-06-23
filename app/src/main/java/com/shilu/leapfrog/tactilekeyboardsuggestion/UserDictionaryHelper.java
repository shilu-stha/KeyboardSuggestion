package com.shilu.leapfrog.tactilekeyboardsuggestion;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.UserDictionary;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Make required communications with UserDictionary Table
 */
public class UserDictionaryHelper extends AsyncQueryHandler {

    private static final int UPDATE_TOKEN = 003;
    private static UserDictionaryHelper instance;
    private OnTextSearchCompleteListener listener;
    private HashMap<String, Timestamp> ticket;
    private Context context;

    private static final int FREQ_NONE = -1;
    private static final int QUERY_TOKEN = 001;
    private static final int QUERY_UPDATE_TOKEN = 002;
    private static final String APP_ID = "UID_Tactile_Keyboard";
    private static final String WORD_TYPE = "User_Dictionary";
    String[] SELECTION_COLUMNS = {UserDictionary.Words._ID, UserDictionary.Words.WORD, UserDictionary.Words.FREQUENCY};
    String SELECTION_CONDITION = UserDictionary.Words.WORD + " LIKE ? ";
    String UPDATE_CONDITION = UserDictionary.Words.WORD + " =?";
    private ArrayList<DictionaryWrapper> suggestionList;
    private String enteredWord;
    private String suggestionToUpdate;

    public UserDictionaryHelper(ContentResolver cr, OnTextSearchCompleteListener listener) {
        super(cr);
        this.listener = listener;
    }

    //    private UserDictionaryHelper(Context context, OnTextSearchCompleteListener listener){
//        this.listener = listener;
//        this.context = context;
//    }

    public static UserDictionaryHelper getInstance(Context context, OnTextSearchCompleteListener listener) {
        if (instance == null) {
            instance = new UserDictionaryHelper(context.getContentResolver(), listener);
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
        this.ticket = ticket;
        suggestionList = new ArrayList<>();
        enteredWord = word;

        this.startQuery(QUERY_TOKEN, null, UserDictionary.Words.CONTENT_URI, SELECTION_COLUMNS, SELECTION_CONDITION, new String[]{word}, null);
    }

    /**
     * Check and adds or updates the word into the userDictionary.
     *
     * @param suggestionToUpdate
     */
    public void updateNewEntry(String suggestionToUpdate) {
        this.suggestionToUpdate = suggestionToUpdate;
        this.startQuery(QUERY_UPDATE_TOKEN, null ,UserDictionary.Words.CONTENT_URI, SELECTION_COLUMNS, UPDATE_CONDITION,
                new String[]{suggestionToUpdate}, null);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);
        if (token == QUERY_TOKEN) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DictionaryWrapper wrapper = new DictionaryWrapper();
//            wrapper.appId = cursor.getInt(cursor.getColumnIndex(UserDictionary.Words.appId));
                wrapper.frequency = cursor.getInt(cursor.getColumnIndex(UserDictionary.Words.FREQUENCY));
                wrapper.id = cursor.getInt(cursor.getColumnIndex(UserDictionary.Words._ID));
                wrapper.word = cursor.getString(cursor.getColumnIndex(UserDictionary.Words.WORD));
                wrapper.type = WORD_TYPE;
                suggestionList.add(wrapper);
                cursor.moveToNext();
            }
            listener.onTextSearchComplete(enteredWord, ticket, suggestionList);
        }else if( token == QUERY_UPDATE_TOKEN){
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                int dictFreq = cursor.getInt(cursor.getColumnIndex(UserDictionary.Words.FREQUENCY));
                ContentValues contentValue = new ContentValues();
                contentValue.put(UserDictionary.Words.APP_ID, APP_ID);
                contentValue.put(UserDictionary.Words.FREQUENCY, String.valueOf(dictFreq + 1));
                this.startUpdate(UPDATE_TOKEN, null, UserDictionary.Words.CONTENT_URI, contentValue, UPDATE_CONDITION,
                        new String[]{suggestionToUpdate});
            } else {
                ContentValues contentValue = new ContentValues();
                contentValue.put(UserDictionary.Words.APP_ID, APP_ID);
                contentValue.put(UserDictionary.Words.FREQUENCY, FREQ_NONE);
                contentValue.put(UserDictionary.Words.WORD, suggestionToUpdate);
                this.startInsert(UPDATE_TOKEN, null, UserDictionary.Words.CONTENT_URI, contentValue);
            }
        }
    }
}
