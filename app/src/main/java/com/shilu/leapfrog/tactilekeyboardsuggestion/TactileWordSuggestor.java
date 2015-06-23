package com.shilu.leapfrog.tactilekeyboardsuggestion;

import android.content.Context;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.TextServicesManager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Base class to implement text suggestion
 */
public class TactileWordSuggestor implements OnTextSearchCompleteListener {
    private static TactileWordSuggestor instance = null;

    private final SpellCheckerHelper mSpellChecker;
    private final UserDictionaryHelper mUserDictionary;
    private final OnTextSearchCompleteListener listener;
    private final SpellCheckerSession spellCheckerSession;

    ArrayList<DictionaryWrapper> userDictionarList;
    ArrayList<DictionaryWrapper> spellCheckerList;
    ArrayList<HashMap<String, Timestamp>> mainTicket = new ArrayList<>(2);

    private static final String WORD_TYPE_NEW = "New_Word";
    private static final String KEY_DICT = "UserDictionary";
    private static final String KEY_SPELLCHECKER = "SpellChecker";
    private static final int LIST_SIZE = 2;
    Context context;

    private TactileWordSuggestor(Context context, OnTextSearchCompleteListener listener) {
        this.listener = listener;
        mSpellChecker = SpellCheckerHelper.getInstance(context, this);

        mUserDictionary = UserDictionaryHelper.getInstance(context, this);
        TextServicesManager textServicesManager = (TextServicesManager) context.getSystemService(
                Context.TEXT_SERVICES_MANAGER_SERVICE);
        spellCheckerSession = textServicesManager.newSpellCheckerSession(null, Locale.getDefault(), mSpellChecker, true);
        mSpellChecker.setSession(spellCheckerSession);
    }

    public static TactileWordSuggestor getInstance(Context context, OnTextSearchCompleteListener listener) {
        if (instance == null) {
            instance = new TactileWordSuggestor(context, listener);
        }
        return instance;
    }

    /**
     * Initialize methods to get suggestions from User Dictionary and Spell Checker
     *
     * @param word
     */
    public void getSuggestions(String word) {
        Timestamp timestamp = getTimeStamp();

        for (int i = 0; i < LIST_SIZE; i++) {
            HashMap<String, Timestamp> map = new HashMap<>();
            switch (i) {
                case 0:
                    map.put(KEY_DICT, timestamp);
                    break;
                case 1:
                    map.put(KEY_SPELLCHECKER, timestamp);
                    break;
            }
            mainTicket.add(i, map);
        }
        mSpellChecker.getSuggestions(word, mainTicket.get(1));
        mUserDictionary.getSuggestions(word, mainTicket.get(0));
    }

    /**
     * Generate current timestamp
     * To be used to check tickets
     *
     * @return
     */
    public Timestamp getTimeStamp() {
        Date date = new Date();
        return new Timestamp(date.getTime());
    }


    @Override
    public void onTextSearchComplete(String word, HashMap<String, Timestamp> ticket, ArrayList<DictionaryWrapper> suggestionList) {
        userDictionarList = new ArrayList<>();
        spellCheckerList = new ArrayList<>();

//        for(int i=0; i< mainTicket.size(); i++){
//            HashMap<String, Timestamp> map = mainTicket.get(i);
//            for(Map.Entry set : map.entrySet()){
//
//            }
//        }

        //update respective list
        for (Map.Entry entry : ticket.entrySet()) {
            switch (entry.getKey().toString()) {
                case KEY_DICT:
                    userDictionarList.addAll(suggestionList);
                    break;
                case KEY_SPELLCHECKER:
                    spellCheckerList.addAll(suggestionList);
                    break;
            }
        }

        spellCheckerList.removeAll(userDictionarList);
        spellCheckerList.remove(addNewWord(word));
        userDictionarList.addAll(spellCheckerList);
//        userDictionarList.remove(word);

        userDictionarList.add(0, addNewWord(word));

        listener.onTextSearchComplete(word, ticket, userDictionarList);
    }

    /**
     * Add typed word to the top of list
     *
     * @param word
     * @return
     */
    private DictionaryWrapper addNewWord(String word) {
        DictionaryWrapper wrapper = new DictionaryWrapper();
        wrapper.word = word;
        wrapper.frequency = 255;
        wrapper.type = WORD_TYPE_NEW;

        return wrapper;
    }

    /**
     * Add selected word to User Dictionary
     *
     * @param position
     */
    public void addToDictionary(int position) {
        mUserDictionary.updateNewEntry(userDictionarList.get(position).word);

    }

    public void reInitialize(){
        SpellCheckerHelper.getInstance(context,listener).reInitialize();
    }

}
