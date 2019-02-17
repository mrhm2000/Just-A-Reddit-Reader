package com.capstone.jarr.interfaces;
import butterknife.BindView;
import butterknife.ButterKnife;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import com.capstone.jarr.frontapp;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.Listing;
import net.dean.jraw.paginators.UserSubredditsPaginator;
import com.capstone.jarr.R;
import com.capstone.jarr.design.design_subpost;
import com.capstone.jarr.comset.comset_preference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.dean.jraw.RedditClient;

/**
 * Created on 09/09/2018.
 */
public class ui_manage extends AppCompatActivity implements LoaderManager.LoaderCallbacks {
    @BindView(R.id.results)
    RecyclerView rv_sred;
    @BindView(R.id.progressbar)
    ProgressBar pb_pbar;
    @BindView(R.id.noMsg)
    TextView tv_emptymsg;

    private ui_subpost uis_adapter;
    private Tracker tr_tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lout_search);
        ButterKnife.bind(this);

        frontapp application = (frontapp) getApplication();
        tr_tracker = application.getDefaultTracker();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        uis_adapter = new ui_subpost(this);
        rv_sred.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_sred.setHasFixedSize(true);
        rv_sred.setAdapter(uis_adapter);

        pb_pbar.setVisibility(View.VISIBLE);
        getSupportLoaderManager().initLoader(1, null, this).forceLoad();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new UserSubredditAsyncTaskLoader(this);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        pb_pbar.setVisibility(View.GONE);
        tv_emptymsg.setVisibility(View.GONE);
        rv_sred.setVisibility(View.VISIBLE);

        if (data != null) {
            List<design_subpost> mysubreddits = (List<design_subpost>) data;
            uis_adapter.setData(mysubreddits);
        }

        if (uis_adapter.getItemCount() == 0) {
            tv_emptymsg.setVisibility(View.VISIBLE);
            rv_sred.setVisibility(View.GONE);
            if (!networkUp()) {
                tv_emptymsg.setText(R.string.str_connection);
            } else {
                tv_emptymsg.setText(R.string.str_subscribetosub);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    private static class UserSubredditAsyncTaskLoader extends AsyncTaskLoader {
        private Context context;

        public UserSubredditAsyncTaskLoader(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        public Object loadInBackground() {
            try {
                AuthenticationState state = AuthenticationManager.get().checkAuthState();
                if (state == AuthenticationState.NEED_REFRESH) {
                    AuthenticationManager.get().refreshAccessToken(ui_login.CREDENTIALS);
                }
                RedditClient client = AuthenticationManager.get().getRedditClient();
                UserSubredditsPaginator paginator = new UserSubredditsPaginator(client, "subscriber");
                Listing<Subreddit> listings = paginator.next();

                Set<String> favSubredditIds = comset_preference.getFavoriteSubreddits(context);

                List<design_subpost> mySubreddits = new ArrayList<>();
                Set<String> latestIds = new HashSet<>();

                for (Subreddit subreddit : listings) {
                    design_subpost mySubreddit = new design_subpost();
                    mySubreddit.setSubreddit(subreddit);
                    if (favSubredditIds.contains(subreddit.getId())) {
                        mySubreddit.setFavorite(true);
                        latestIds.add(subreddit.getId());
                    } else {
                        mySubreddit.setFavorite(false);
                    }
                    mySubreddits.add(mySubreddit);
                }
                comset_preference.updateFavorites(context, latestIds);
                return mySubreddits;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        tr_tracker.setScreenName(getString(R.string.str_subsmanage));
        tr_tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}