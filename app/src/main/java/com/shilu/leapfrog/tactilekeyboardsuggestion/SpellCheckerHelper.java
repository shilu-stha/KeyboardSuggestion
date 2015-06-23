package com.shilu.leapfrog.tactilekeyboardsuggestion;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.Toast;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * SpellChecker Helper
 *
 */
public class SpellCheckerHelper implements SpellCheckerSession.SpellCheckerSessionListener {

    private SpellCheckerSession spellCheckerSession;
    private OnTextSearchCompleteListener listener;
    private HashMap<String, Timestamp> ticket;
    private Context context;
    private static SpellCheckerHelper instance;
    private String enteredWord;
    private int timesSuggestionTried;

    private static final String SETTINGS_PKG = "com.android.settings";
    private static final String SETTINGS_CLS = "com.android.settings.Settings$SpellCheckersSettingsActivity";
    private static final String ERROR_ENABLESPELLCHECKER_MSG = "Please turn on the spell checker from setting";
    private static final String WORD_TYPE_INBUILD = "InBuilt_Dictionary";


    private SpellCheckerHelper(Context context, OnTextSearchCompleteListener listener) {
        this.listener = listener;
        this.context = context;
        TextServicesManager textServicesManager = (TextServicesManager) context.getSystemService(
                Context.TEXT_SERVICES_MANAGER_SERVICE);
        spellCheckerSession = textServicesManager.newSpellCheckerSession(null, Locale.getDefault(), this, true);
    }

    public static SpellCheckerHelper getInstance(Context context, OnTextSearchCompleteListener listener) {
        if (instance == null) {
            instance = new SpellCheckerHelper(context, listener);
        }
        return instance;
    }

    @Override
    public void onGetSuggestions(SuggestionsInfo[] results) {
        ArrayList<DictionaryWrapper> suggestionList = new ArrayList<>();
        ArrayList<DictionaryWrapper> suggestions = new ArrayList<>();

        for (int i = 0; i < results.length; i++) {
            // Returned finalSuggestion are contained in SuggestionsInfo
            final int len = results[i].getSuggestionsCount();
            for (int j = 0; j < len; j++) {
                DictionaryWrapper wrapper = new DictionaryWrapper();
                wrapper.word = results[i].getSuggestionAt(j);
                wrapper.frequency = 255;
                wrapper.type = WORD_TYPE_INBUILD;
                suggestions.add(wrapper);
            }
        }
        suggestionList.addAll(suggestions);

        if (suggestions.size() == 0) {
            if (timesSuggestionTried == 0) {
                spellCheckerSession.getSuggestions(new TextInfo(enteredWord + ","), 100);
                timesSuggestionTried = timesSuggestionTried + 1;
            }
        }
        listener.onTextSearchComplete(enteredWord, ticket, suggestionList);
    }

    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {

    }

    /**
     * Get suggestions from spellchecker
     *
     * @param word
     * @param ticket
     * @param tactileWordSuggestor
     */
    public void getSuggestions(String word, HashMap<String, Timestamp> ticket, TactileWordSuggestor tactileWordSuggestor) {

        this.enteredWord = word;
        this.ticket = ticket;
        if (spellCheckerSession != null) {
            timesSuggestionTried = 0;
            spellCheckerSession.getSuggestions(new TextInfo(enteredWord), 100);
        } else {
            gotoSpellCheckerSetting();
        }

    }

    /**
     * Show toast.
     * Goto spellchecker settings.
     */
    private void gotoSpellCheckerSetting() {
        // Show the message to user
        Toast.makeText(context, ERROR_ENABLESPELLCHECKER_MSG, Toast.LENGTH_LONG).show();
        // open the settings page for user to turn spell checker ON
        ComponentName componentToLaunch = new ComponentName(SETTINGS_PKG, SETTINGS_CLS);
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(componentToLaunch);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Error
        }
    }
}