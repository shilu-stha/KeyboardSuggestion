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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SpellCheckerActivity extends Activity implements OnTextSearchCompleteListener {

    EditText edt_input;

    public static final List<DictionaryWrapper> finalSuggestion = new ArrayList<>();

    DictionaryAdapter adapter;
    ListView listView;
    String[] words;

    private TactileWordSuggestor wordSuggestor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spell_checker_layout);
        wordSuggestor = TactileWordSuggestor.getInstance(getApplicationContext(), this);

        setElements();
    }

    /**
     * Initialize editText, listView, adapters.
     * Add textChangerListeners
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

                                                     wordSuggestor.getSuggestions(words[words.length - 1]);
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

                                                words[words.length - 1] = finalSuggestion.get(position).word;
                                                String newSentence = "";

                                                for (int i = 0; i < words.length; i++) {
                                                    newSentence = newSentence + words[i];
                                                    if (i != words.length - 1) {
                                                        newSentence = newSentence + " ";
                                                    }
                                                }
                                                //add selected words to UserDictionary
                                                wordSuggestor.addToDictionary(position);

                                                edt_input.setText(newSentence);
                                                edt_input.setSelection(edt_input.getText().length());
                                            }
                                        }

        );

    }

    @Override
    public void onTextSearchComplete(String word, HashMap<String, Timestamp> ticket, ArrayList<DictionaryWrapper> suggestionList) {
        if (suggestionList.size() != 0) {
            finalSuggestion.clear();
            listView.setVisibility(View.VISIBLE);
            finalSuggestion.addAll(suggestionList);
            adapter.notifyDataSetChanged();
        }
    }
}
