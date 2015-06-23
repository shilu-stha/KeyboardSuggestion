package com.shilu.leapfrog.tactilekeyboardsuggestion;

/**
 * Wrapper class for words
 *
 * @author shilushrestha
 * @date 6/19/15.
 */

public class DictionaryWrapper {
    String word;
    int frequency;
    int id;
    int appId;
    String type;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DictionaryWrapper oth = (DictionaryWrapper) obj;
        return word.equals(oth.word);
    }
}
