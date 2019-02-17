package com.capstone.jarr.concurrent;
import android.app.job.JobScheduler;
import android.app.job.JobInfo;
import android.content.Intent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import com.capstone.jarr.data.dat_provider;
import net.dean.jraw.RedditClient;
import com.capstone.jarr.data.dat_contract;
import net.dean.jraw.models.Listing;
import com.capstone.jarr.interfaces.ui_login;
import net.dean.jraw.paginators.SubredditPaginator;
import java.util.ArrayList;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.models.Submission;


/**
 * Created on 09/09/2018.
 */

public class con_postsync {
    private static final int int_idperiodic = 1;
    private static final int int_period = 300000;
    private static final int int_setbackoff = 10000;
    public static final String int_dataupdate = "com.capstone.jarr.int_dataupdate";

    public static void getPosts(Context context) {
        AuthenticationState state = AuthenticationManager.get().checkAuthState();
        if (state == AuthenticationState.NEED_REFRESH) {
            try {
                AuthenticationManager.get().refreshAccessToken(ui_login.CREDENTIALS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        RedditClient redditClient = AuthenticationManager.get().getRedditClient();
        if (redditClient.isAuthenticated()) {
            SubredditPaginator paginator = new SubredditPaginator(redditClient);
            Listing<Submission> posts = paginator.next();

            ArrayList<ContentValues> postCVs = new ArrayList<>();
            for (Submission post : posts) {
                ContentValues postCV = new ContentValues();
               postCV.put(dat_contract.posts.DATA, post.getDataNode().toString());
                postCVs.add(postCV);

            }

            if (postCVs.size() > 0) {
                context.getContentResolver().delete(dat_contract.posts.CONTENT_URI,null, null);
                context.getContentResolver().bulkInsert(dat_contract.posts.CONTENT_URI, postCVs.toArray(
                        new ContentValues[postCVs.size()]
                ));
            }

            Intent dataUpdatedIntent = new Intent(int_dataupdate);
            context.sendBroadcast(dataUpdatedIntent);
        }
    }

    public static synchronized void initialize(final Context context) {
        schedulePeriodic(context);
    }

    private static void schedulePeriodic(Context context) {
        JobInfo.Builder builder = new JobInfo.Builder(int_idperiodic, new ComponentName(context, con_jobpost.class));

        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(int_period)
                .setBackoffCriteria(int_setbackoff, JobInfo.BACKOFF_POLICY_EXPONENTIAL);
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(builder.build());
    }
}
