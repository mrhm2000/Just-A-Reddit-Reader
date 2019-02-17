package com.capstone.jarr.interfaces;
import java.net.URL;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.capstone.jarr.frontapp;
import com.capstone.jarr.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;

/**
 * Created on 09/09/2018.
 */
public class ui_login extends AppCompatActivity {

    public static final Credentials CREDENTIALS = Credentials.installedApp("FDkoX6MSFjQahQ",
            "https://mrhm2000.github.io/my-project/");

    private static final String str_tag = ui_login.class.getSimpleName();
    private Tracker tr_tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lout_credential);

        frontapp application = (frontapp) getApplication();
        tr_tracker = application.getDefaultTracker();

        final OAuthHelper helper = AuthenticationManager.get().getRedditClient().getOAuthHelper();

        String[] scopes = {"identity", "read", "subscribe", "mysubreddits", "vote"};

        final URL authorizationUrl = helper.getAuthorizationUrl(CREDENTIALS, true, true, scopes);
        final WebView webView = ((WebView) findViewById(R.id.vw_web));

        webView.loadUrl(authorizationUrl.toExternalForm());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.contains("code=")) {
                    // Redirect URL
                    onUserChallenge(url, CREDENTIALS);
                } else if (url.contains("error=")) {
                    Toast.makeText(ui_login.this, R.string.str_mustlog, Toast.LENGTH_SHORT).show();
                    webView.loadUrl(authorizationUrl.toExternalForm());
                }
            }
        });
    }

    private void onUserChallenge(final String url, final Credentials creds) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    OAuthData data = AuthenticationManager.get().getRedditClient().getOAuthHelper().onUserChallenge(params[0], creds);
                    AuthenticationManager.get().getRedditClient().authenticate(data);
                    return AuthenticationManager.get().getRedditClient().getAuthenticatedUser();
                } catch (NetworkException | OAuthException | IllegalStateException e) {
                    Log.e(str_tag, "Could not log in", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                ui_login.this.finish();
            }
        }.execute(url);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        tr_tracker.setScreenName(getString(R.string.str_screenlog));
        tr_tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
