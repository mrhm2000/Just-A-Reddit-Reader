package com.capstone.jarr.interfaces;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.capstone.jarr.frontapp;
import com.capstone.jarr.R;
import com.capstone.jarr.comset.comset_date;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.models.LoggedInAccount;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;


/**
 * Created on 09/09/2018.
 */
public class ui_profile extends AppCompatActivity {
    @BindView(R.id.name)
    TextView tv_uname;
    @BindView(R.id.karma)
    TextView tv_karma;
    @BindView(R.id.gcredit)
    TextView tv_goldcredit;
    @BindView(R.id.redmember)
    TextView tv_redage;
    @BindView(R.id.progressbar)
    ProgressBar pb_pbar;
    @BindView(R.id.cardview)
    CardView cv_cardview;
    @BindView(R.id.noMsg)
    TextView tv_emptymsg;

    private Tracker tr_tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lout_profile);
        ButterKnife.bind(this);

        frontapp application = (frontapp) getApplication();
        tr_tracker = application.getDefaultTracker();

        new AsyncTask<Void, Void, Object>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pb_pbar.setVisibility(View.VISIBLE);
                cv_cardview.setVisibility(View.GONE);
                tv_emptymsg.setVisibility(View.GONE);
            }

            @Override
            protected Object doInBackground(Void... voids) {
                try {
                    AuthenticationState state = AuthenticationManager.get().checkAuthState();
                    if (state == AuthenticationState.NEED_REFRESH) {
                        AuthenticationManager.get().refreshAccessToken(ui_login.CREDENTIALS);
                    }
                    return AuthenticationManager.get().getRedditClient().me();
                } catch (Exception e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object data) {
                pb_pbar.setVisibility(View.GONE);
                if (data != null) {
                    cv_cardview.setVisibility(View.VISIBLE);
                    LoggedInAccount account = (LoggedInAccount) data;
                    tv_uname.append(account.getFullName());
                    tv_karma.setText(String.valueOf(account.getLinkKarma()).concat(getString(R.string.str_karma)));
                    tv_goldcredit.setText(String.valueOf(account.getCreddits())
                            .concat(getString(R.string.str_gcredit)));
                    tv_redage.setText(comset_date.convert(account.getCreated().getTime())
                            .concat(getString(R.string.str_redage)));
                } else {
                    if (!networkUp()) {
                        tv_emptymsg.setVisibility(View.VISIBLE);
                        tv_emptymsg.setText(R.string.str_connection);
                    }
                }
            }
        }.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tr_tracker.setScreenName(getString(R.string.str_uprofilescreen));
        tr_tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
