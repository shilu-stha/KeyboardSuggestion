package com.shilu.leapfrog.tactilekeyboardsuggestion;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SpellCheckerActivity extends Activity implements SpellCheckerSession.SpellCheckerSessionListener {

    EditText edt_input;

    SpellCheckerSession mScs;
    TextServicesManager tsm;

    // will contain appended suggestions from userdictionary and default suggestions
    public static final List<DictionaryWrapper> finalSuggestion = new ArrayList<>();
    // will contain suggestions for userdictionary
    public static final List<DictionaryWrapper> suggestions = new ArrayList<>();
    public static final List<String> temp_suggestions = new ArrayList<>();
    //store how many times suggestion has been tried for current word
    int timesSuggestionTried = 0;

    DictionaryAdapter adapter;
    ListView listView;
    String[] words;
    private TactileUserDictionary tactDictionary;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spell_checker_layout);

        setElements();
    }


    @Override
    public void onGetSuggestions(SuggestionsInfo[] results) {
        finalSuggestion.clear();
        suggestions.clear();
        temp_suggestions.clear();

        //add from userdictionary
        String[] args = {"%" + words[words.length - 1] + "%"};
        finalSuggestion.addAll(tactDictionary.addWordFromDictionary(args));

        for (int i = 0; i < results.length; i++) {
            // Returned finalSuggestion are contained in SuggestionsInfo
            final int len = results[i].getSuggestionsCount();
            for (int j = 0; j < len; j++) {
                DictionaryWrapper wrapper = new DictionaryWrapper();
                wrapper.WORD = results[i].getSuggestionAt(j);
                wrapper.FREQUENCY = 255;

                //String suggestion = results[i].getSuggestionAt(j);
                if (!temp_suggestions.contains(wrapper.WORD)) {
                    suggestions.add(wrapper);
                    temp_suggestions.add(wrapper.WORD);
                }
            }
            finalSuggestion.addAll(suggestions);
        }
        updateAdapter();

    }

    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {

    }

    void updateAdapter() {
        if (suggestions.size() != 0) {

            if (!temp_suggestions.contains(words[words.length - 1])) {

                DictionaryWrapper wrapper = new DictionaryWrapper();
                wrapper.WORD = words[words.length - 1];
                wrapper.FREQUENCY = 255;
                finalSuggestion.add(0, wrapper);
            }
            adapter.notifyDataSetChanged();
        } else {

            if (timesSuggestionTried == 0) {
                mScs.getSuggestions(new TextInfo(words[words.length - 1] + ","), 100);
                timesSuggestionTried = timesSuggestionTried + 1;
            } else {
                adapter.notifyDataSetChanged();
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        tsm = (TextServicesManager) getSystemService(
                Context.TEXT_SERVICES_MANAGER_SERVICE);
        mScs = tsm.newSpellCheckerSession(null, Locale.getDefault(), this, true);
        tactDictionary = new TactileUserDictionary(getApplicationContext());
    }

    /**
     * Initialize editText, listView, adapters.
     * Add textChangerListeners.
     *
     * @author Manas Shrestha
     */
    public void setElements() {
        edt_input = (EditText) findViewById(R.id.edt_input);

        listView = (ListView) findViewById(R.id.simpleListView);
//        adapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_list_item_1, finalSuggestion);

        adapter = new DictionaryAdapter(this,
                R.layout.activity_main, finalSuggestion);


        listView.setAdapter(adapter);

        edt_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mScs != null) {
                    if (s.length() != 0) {
                        listView.setVisibility(View.VISIBLE);
                        String sentence = s.toString();
                        words = sentence.split("\\s+");
                        timesSuggestionTried = 0;
                        mScs.getSuggestions(new TextInfo(words[words.length - 1]), 100);
                    } else {
                        listView.setVisibility(View.GONE);
                    }
                } else {
                    gotoSpellCheckerSetting();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                words[words.length - 1] = finalSuggestion.get(position).WORD;

                String newSentence = "";

                for (int i = 0; i < words.length; i++) {

                    newSentence = newSentence + words[i];
                    if (i != words.length - 1) {
                        newSentence = newSentence + " ";
                    }
                }

                tactDictionary.checkIfFrequencyNeedsToBeUpdated(finalSuggestion.get(position).WORD);

                edt_input.setText(newSentence);
                edt_input.setSelection(edt_input.getText().length());
            }
        });

    }


    /**
     * Show toast.
     * Goto spellchecker settings.
     *
     * @author Manas Shrestha
     */
    public void gotoSpellCheckerSetting() {
        // Show the message to user
        Toast.makeText(SpellCheckerActivity.this, "Please turn on the spell checker from setting", Toast.LENGTH_LONG).show();
        // open the settings page for user to turn spell checker ON
        ComponentName componentToLaunch = new ComponentName("com.android.settings",
                "com.android.settings.Settings$SpellCheckersSettingsActivity");
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(componentToLaunch);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Error
        }
    }
}
