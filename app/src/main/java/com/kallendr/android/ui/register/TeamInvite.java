package com.kallendr.android.ui.register;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TeamInvite {

        ArrayList<String> emails;
        public TeamInvite(ArrayList<String> emails) {
            this.emails = emails;
        }

        /*
        * Send emails to people invited to the team
        * */
        public void sendEmails() {
            new SendEmailsTask().execute(emails);
        }

        private static class SendEmailsTask extends AsyncTask<ArrayList<String>, Void, Void> {

            @Override
            protected Void doInBackground(ArrayList<String>... emails) {
                for (String userEmail : emails[0]) {
                    try {
                        System.out.println("Emailing: " + userEmail);
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

            }
        }

}
