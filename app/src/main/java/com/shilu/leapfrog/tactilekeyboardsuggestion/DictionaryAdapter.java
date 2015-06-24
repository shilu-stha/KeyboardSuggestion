package com.shilu.leapfrog.tactilekeyboardsuggestion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shilushrestha on 6/19/15.
 */
public class DictionaryAdapter extends ArrayAdapter<DictionaryWrapper>{

    ArrayList<DictionaryWrapper> list;
    public DictionaryAdapter(Context context, int resource, ArrayList<DictionaryWrapper> list) {
        super(context, resource);
        this.list = list;
        //System.out.println("size "+list.size());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(android.R.layout.simple_list_item_1, null);
        }

		DictionaryWrapper wrapper = list.get(position);
        //System.out.println("wrapper "+wrapper.word);
        if (wrapper != null) {
            TextView tt = (TextView) v.findViewById(android.R.id.text1);
            if (tt != null){
                tt.setText(wrapper.word);
            }

        }
        return v;
    }

    public void swapItems(ArrayList<DictionaryWrapper> items) {
        this.list = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }
}
