package com.capstone.jarr.interfaces;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import butterknife.BindView;
import butterknife.ButterKnife;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.SearchView;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import android.view.View;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import java.util.List;
import java.util.Set;
import com.capstone.jarr.frontapp;
import com.capstone.jarr.R;
import com.capstone.jarr.design.design_subpost;
import com.capstone.jarr.comset.comset_preference;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import java.util.ArrayList;
import net.dean.jraw.RedditClient;
import net.dean.jraw.paginators.SubredditSearchPaginator;
import android.content.res.Configuration;



/**
 * Created on 09/09/2018.
 */
public class ui_search extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks {

    public static final String KEY_QUERY = "query";
    @BindView(R.id.results)
    RecyclerView rv_sred;
    @BindView(R.id.progressbar)
    ProgressBar pb_pbar;
    @BindView(R.id.noMsg)
    TextView tv_emptymsg;
    LayoutManager mLayoutManager;

    private final String KEY_RECYCLER_STATE = "recycler_state";
    private static Bundle mBundleRecyclerViewState;
    private ui_subpost uis_adapter;
    private Tracker tr_tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lout_search);
        ButterKnife.bind(this);
        if (getIntent() != null) {

        }

        frontapp application = (frontapp) getApplication();
        tr_tracker = application.getDefaultTracker();

        uis_adapter = new ui_subpost(this);
        rv_sred.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_sred.setHasFixedSize(true);
        rv_sred.setAdapter(uis_adapter);
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        // save RecyclerView state
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = rv_sred.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        tr_tracker.setScreenName(getString(R.string.str_subsearch));
        tr_tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.men_search, menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        MenuItemCompat.expandActionView(item);
        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                finish();
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                pb_pbar.setVisibility(View.VISIBLE);
                rv_sred.setVisibility(View.GONE);
                tv_emptymsg.setVisibility(View.GONE);
                Bundle bundle = new Bundle();
                bundle.putString(KEY_QUERY, query);
                getSupportLoaderManager()
                        .restartLoader(1, bundle, ui_search.this).forceLoad();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new SearchSubredditAsyncTaskLoader(this, args.getString(KEY_QUERY));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.e("On Config Change","LANDSCAPE");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.e("On Config Change","PORTRAIT");
        }
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
                tv_emptymsg.setText(R.string.str_notfound);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    private static class SearchSubredditAsyncTaskLoader extends AsyncTaskLoader {
        private String query;
        private Context context;

        public SearchSubredditAsyncTaskLoader(Context context, String query) {
            super(context);
            this.context = context;
            this.query = query;
        }

        @Override
        public Object loadInBackground() {
            try {
                AuthenticationState state = AuthenticationManager.get().checkAuthState();
                if (state == AuthenticationState.NEED_REFRESH) {
                    AuthenticationManager.get().refreshAccessToken(ui_login.CREDENTIALS);
                }

                RedditClient client = AuthenticationManager.get().getRedditClient();
                SubredditSearchPaginator paginator = new SubredditSearchPaginator(client, query);
                Listing<Subreddit> listings = paginator.next();

                Set<String> favSubredditIds = comset_preference.getFavoriteSubreddits(context);
                List<design_subpost> mySubreddits = new ArrayList<>();

                for (Subreddit subreddit : listings) {
                    design_subpost mySubreddit = new design_subpost();
                    mySubreddit.setSubreddit(subreddit);
                    if (favSubredditIds.contains(subreddit.getId())) {
                        mySubreddit.setFavorite(true);
                    } else {
                        mySubreddit.setFavorite(false);
                    }
                    mySubreddits.add(mySubreddit);
                }
                return mySubreddits;
            } catch (Exception e) {
            }
            return null;
        }
    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
