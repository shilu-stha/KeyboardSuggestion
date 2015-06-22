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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Plug and Play class to implement spellchecker
 *
 * @author shilushrestha
 * @date 6/22/15.
 */
public class TactileSpellChecker implements SpellCheckerSession.SpellCheckerSessionListener {


    private TextServicesManager textServicesManager;
    private SpellCheckerSession spellCheckerSession;
    private TactileUserDictionary tactileUserDictionary;

    private Context context;
    private String enteredWord;

    public static final List<DictionaryWrapper> finalSuggestion = new ArrayList<>();
    // will contain suggestions for userdictionary
    public static final List<DictionaryWrapper> suggestions = new ArrayList<>();
    public static final List<String> temp_suggestions = new ArrayList<>();

    //store how many times suggestion has been tried for current word
    private int timesSuggestionTried;
    public boolean isSuggestionsGathered = false;


    private static TactileSpellChecker instance = null;
    private CheckerListener mlistener;

    protected TactileSpellChecker(Context context) {
        this.context = context;
        textServicesManager = (TextServicesManager) context.getSystemService(
                Context.TEXT_SERVICES_MANAGER_SERVICE);
        spellCheckerSession = textServicesManager.newSpellCheckerSession(null, Locale.getDefault(), this, true);
        tactileUserDictionary = new TactileUserDictionary(context);
    }

    public static TactileSpellChecker getInstance(Context context) {
        if(instance == null) {
            instance = new TactileSpellChecker(context);
        }
        return instance;
    }



    @Override
    public void onGetSuggestions(SuggestionsInfo[] results) {
        finalSuggestion.clear();
        suggestions.clear();
        temp_suggestions.clear();

        //add from userdictionary
        String[] args = {"%" + enteredWord + "%"};
        finalSuggestion.addAll(tactileUserDictionary.addWordFromDictionary(args));

        for (int i = 0; i < results.length; i++) {
            // Returned finalSuggestion are contained in SuggestionsInfo
            final int len = results[i].getSuggestionsCount();
            for (int j = 0; j < len; j++) {
                DictionaryWrapper wrapper = new DictionaryWrapper();
                wrapper.WORD = results[i].getSuggestionAt(j);
                wrapper.FREQUENCY = 255;
                wrapper.TYPE = "InBuilt_Dictionary";
                //String suggestion = results[i].getSuggestionAt(j);
                if (!temp_suggestions.contains(wrapper.WORD)) {
                    suggestions.add(wrapper);
                    temp_suggestions.add(wrapper.WORD);
                }
            }
            finalSuggestion.addAll(suggestions);
        }
        if (suggestions.size() != 0) {

            if (!temp_suggestions.contains(enteredWord)) {

                DictionaryWrapper wrapper = new DictionaryWrapper();
                wrapper.WORD = enteredWord;
                wrapper.FREQUENCY = 255;
                wrapper.TYPE = "New_Word";
                finalSuggestion.add(0, wrapper);
            }
            isSuggestionsGathered = true;
        } else {
            isSuggestionsGathered = false;
            if (timesSuggestionTried == 0) {
                spellCheckerSession.getSuggestions(new TextInfo(enteredWord + ","), 100);
                timesSuggestionTried = timesSuggestionTried + 1;
            }
        }
        mlistener.getSuggestions(finalSuggestion);
    }

    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {

    }

    public void getSuggestions(String word){
        enteredWord = word;
            if (spellCheckerSession != null) {
                timesSuggestionTried = 0;
                spellCheckerSession.getSuggestions(new TextInfo(word), 100);
            } else {
                gotoSpellCheckerSetting();
            }
    }

    /**
     * Show toast.
     * Goto spellchecker settings.
     *
     * @author Manas Shrestha
     */
    private void gotoSpellCheckerSetting() {
        // Show the message to user
        Toast.makeText(context, "Please turn on the spell checker from setting", Toast.LENGTH_LONG).show();
        // open the settings page for user to turn spell checker ON
        ComponentName componentToLaunch = new ComponentName("com.android.settings",
                "com.android.settings.Settings$SpellCheckersSettingsActivity");
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

    public List<DictionaryWrapper> getFinalList(){
        return finalSuggestion;
    }


    public void setTaskListener(CheckerListener listener) {
        mlistener = listener;
    }

    public interface CheckerListener {
        public void getSuggestions(List<DictionaryWrapper> list);
    }
}