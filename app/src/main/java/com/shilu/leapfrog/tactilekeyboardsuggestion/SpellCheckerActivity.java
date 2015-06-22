package com.shilu.leapfrog.tactilekeyboardsuggestion;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class SpellCheckerActivity extends Activity {

    EditText edt_input;

    public static final List<DictionaryWrapper> finalSuggestion = new ArrayList<>();

    DictionaryAdapter adapter;
    ListView listView;
    String[] words;
    private TactileUserDictionary tactDictionary;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spell_checker_layout);
        TactileSpellChecker.getInstance(getApplicationContext()).setTaskListener(checkerListener);
        tactDictionary = new TactileUserDictionary(getApplicationContext());
        setElements();
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

        adapter = new DictionaryAdapter(this,
                android.R.layout.simple_list_item_1, finalSuggestion);

        listView.setAdapter(adapter);

        edt_input.addTextChangedListener(new TextWatcher() {
                                             @Override
                                             public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                             }

                                             @Override
                                             public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                 listView.setVisibility(View.GONE);
                                                 if (TextUtils.isEmpty(s.toString())) {
                                                     finalSuggestion.clear();
                                                 } else {
                                                     String sentence = s.toString();
                                                     words = sentence.split("\\s+");
                                                     TactileSpellChecker.getInstance(getApplicationContext()).getSuggestions(words[words.length - 1]);
                                                 }
                                             }

                                             @Override
                                             public void afterTextChanged(Editable s) {
                                             }
                                         }

        );

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()

                                        {
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
                                                //add selected words to UserDictionary
                                                TactileSpellChecker.getInstance(getApplicationContext()).addToDictionary(position);

                                                edt_input.setText(newSentence);
                                                edt_input.setSelection(edt_input.getText().length());
                                            }
                                        }

        );

    }

    TactileSpellChecker.CheckerListener checkerListener = new TactileSpellChecker.CheckerListener() {

        @Override
        public void getSuggestions(String enteredWord, List<DictionaryWrapper> list) {
            if (list.size() != 0) {
                finalSuggestion.clear();
                listView.setVisibility(View.VISIBLE);
                finalSuggestion.addAll(list);
                adapter.notifyDataSetChanged();
            }
        }

    };

}
