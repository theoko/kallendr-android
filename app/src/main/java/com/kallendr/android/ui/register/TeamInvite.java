package com.kallendr.android.ui.register;

import android.content.Context;

import java.util.ArrayList;

public class TeamInvite {

        ArrayList<String> emails;
        public TeamInvite(ArrayList<String> emails) {
            this.emails = emails;
        }

        /*
        * Send emails to people invited to the team
        * */
        public void sendEmails() {
            for(String email : this.emails) {

            }
        }

}
