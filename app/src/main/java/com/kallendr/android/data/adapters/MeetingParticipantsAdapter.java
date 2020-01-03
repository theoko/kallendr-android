package com.kallendr.android.data.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kallendr.android.R;
import com.kallendr.android.helpers.Helpers;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MeetingParticipantsAdapter extends RecyclerView.Adapter<MeetingParticipantsAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private ArrayList<String> memberEmails;

    public MeetingParticipantsAdapter(LayoutInflater inflater, ArrayList<String> memberEmails) {
        this.inflater = inflater;
        this.memberEmails = memberEmails;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // Specify layout
        View view = inflater.inflate(R.layout.meeting_participants_list, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Picasso.get().load("https://www.gravatar.com/avatar/" + Helpers.md5(memberEmails.get(i))).fit().into(myViewHolder.profileImageView);
        myViewHolder.emailTextView.setText(memberEmails.get(i));
    }

    @Override
    public int getItemCount() {
        return memberEmails.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView emailTextView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
        }
    }
}
