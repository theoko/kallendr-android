package com.kallendr.android.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import com.kallendr.android.R;
import com.kallendr.android.data.model.Team;

import java.util.ArrayList;

public class TeamAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Team> teams;

    public TeamAdapter(Context context, ArrayList<Team> events) {
        this.context = context;
        this.teams = events;
    }


    @Override
    public int getCount() {
        return teams.size();
    }

    @Override
    public Object getItem(int position) {
        return teams.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TwoLineListItem twoLineListItem;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            twoLineListItem = (TwoLineListItem) inflater.inflate(
                    R.layout.custom_simple_list_item_2, null);
        } else {
            twoLineListItem = (TwoLineListItem) convertView;
        }

        TextView text1 = twoLineListItem.findViewById(R.id.text1);
        TextView text2 = twoLineListItem.findViewById(R.id.text2);

        text1.setText(teams.get(position).getTeamName());
        text2.setText(teams.get(position).getDescription());

        return twoLineListItem;
    }
}
