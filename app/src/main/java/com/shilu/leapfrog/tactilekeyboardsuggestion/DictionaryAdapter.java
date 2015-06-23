package com.shilu.leapfrog.tactilekeyboardsuggestion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by shilushrestha on 6/19/15.
 */
public class DictionaryAdapter extends ArrayAdapter<DictionaryWrapper>{

    List<DictionaryWrapper> list;
    public DictionaryAdapter(Context context, int resource, List<DictionaryWrapper> list) {
        super(context, resource);
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(android.R.layout.simple_list_item_1, null);
        }

		DictionaryWrapper wrapper = list.get(position);

        if (wrapper != null) {
            TextView tt = (TextView) v.findViewById(android.R.id.text1);
            if (tt != null){
                tt.setText(wrapper.word);
            }

        }
        return v;
    }

    @Override
    public int getCount() {
        return list.size();
    }
}
