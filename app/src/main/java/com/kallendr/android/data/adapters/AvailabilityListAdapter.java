package com.kallendr.android.data.adapters;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.kallendr.android.R;
import com.kallendr.android.data.model.AvailableGroupMember;

import java.util.ArrayList;

public class AvailabilityListAdapter implements ListAdapter {

    Context context;
    ArrayList<AvailableGroupMember> membersList;

    public AvailabilityListAdapter(Context context, ArrayList<AvailableGroupMember> membersList) {
        this.context = context;
        this.membersList = membersList;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return membersList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AvailableGroupMember availableGroupMember = membersList.get(position);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        convertView = layoutInflater.inflate(R.layout.list_available_person, null);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        TextView usernameHeader = convertView.findViewById(R.id.usernameHeader);
        ImageView availabilityImage = convertView.findViewById(R.id.availabilityImage);
        TextView availabilityDescription = convertView.findViewById(R.id.availabilityDescription);

        usernameHeader.setText(availableGroupMember.getEmail());
        availabilityImage.setImageResource(
                availableGroupMember.getAvailable() ? R.mipmap.ic_green_check : R.mipmap.ic_red_clear
        );
        availabilityDescription.setText(availableGroupMember.getAvailable()  ?  "Available" : "Busy");
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return membersList.size();
    }

    @Override
    public boolean isEmpty() {
        return membersList.size() > 0;
    }
}
