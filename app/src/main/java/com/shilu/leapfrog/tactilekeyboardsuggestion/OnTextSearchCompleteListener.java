package com.shilu.leapfrog.tactilekeyboardsuggestion;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Listener to notify text search complete
 *
 */
public interface OnTextSearchCompleteListener {
    void onTextSearchComplete(String word,HashMap<String, Timestamp> ticket, ArrayList<DictionaryWrapper> suggestionList);

}
