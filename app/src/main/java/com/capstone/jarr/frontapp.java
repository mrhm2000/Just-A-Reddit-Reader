package com.capstone.jarr;
import net.dean.jraw.auth.RefreshTokenHandler;
import net.dean.jraw.http.LoggingMode;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import net.dean.jraw.android.lib_redclient;
import net.dean.jraw.android.lib_token;
import com.facebook.stetho.Stetho;
import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;


/**
 * Created on 09/09/2018.
 */
public class frontapp extends Application {

    private static GoogleAnalytics sAnalytics;
    private static Tracker sTracker;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sAnalytics = GoogleAnalytics.getInstance(this);

        RedditClient reddit = new lib_redclient(this);
        reddit.setLoggingMode(LoggingMode.ALWAYS);
        AuthenticationManager.get().init(reddit, new RefreshTokenHandler(new lib_token(this), reddit));

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
    }

    synchronized public Tracker getDefaultTracker() {
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker(R.xml.global_tracker);
        }
        return sTracker;
    }


}
