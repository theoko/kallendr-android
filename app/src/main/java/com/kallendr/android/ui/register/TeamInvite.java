package com.kallendr.android.ui.register;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.kallendr.android.data.Database;
import com.kallendr.android.helpers.interfaces.OnTaskCompleted;
import com.kallendr.android.helpers.interfaces.Result;

import java.util.ArrayList;

public class TeamInvite {

        ArrayList<String> emails;
        static ProgressBar progressBar;

        public TeamInvite(ArrayList<String> emails, ProgressBar pBar) {
            this.emails = emails;
            progressBar = pBar;
        }

        /*
        * Send emails to people invited to the team
        * */
        public void sendEmails(OnTaskCompleted onTaskCompleted) {
            new SendEmailsTask(onTaskCompleted).execute(emails);
        }

        public static void doneSendingEmails() {
            progressBar.setVisibility(View.GONE);
        }

        public static class SendEmailsTask extends AsyncTask<ArrayList<String>, Void, Void> {

            private OnTaskCompleted listener;
            private boolean taskSuccessful;
            private String taskMessage;

            public SendEmailsTask(OnTaskCompleted listener) {
                this.listener = listener;
            }

            @Override
            protected Void doInBackground(ArrayList<String>... emails) {
                for (String userEmail : emails[0]) {
                    // TODO: replace with real method
                    System.out.println("Emailing: " + userEmail);
                }
                Database.getInstance().setTeamNameAndAddEmailsToInvitationList(emails[0], new Result<String>() {
                    @Override
                    public void success(String arg) {
                        taskSuccessful = true;
                        taskMessage = arg;
                    }

                    @Override
                    public void fail(String arg) {
                        taskSuccessful = false;
                        taskMessage = arg;
                    }
                });

                // TODO: Fix this to return success or failure
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                doneSendingEmails();
                listener.onTaskCompleted();
            }
        }

}
