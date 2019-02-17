package com.capstone.jarr.interfaces;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
//import com.capstone.jarr.data.dat_col;
import com.capstone.jarr.data.dat_contract;
import com.capstone.jarr.frontapp;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.http.oauth.Credentials;
import com.capstone.jarr.R;
//import com.capstone.jarr.data.dat_post;
import com.capstone.jarr.data.dat_provider;
import com.capstone.jarr.concurrent.con_postsync;
import com.capstone.jarr.comset.comset_preference;
import java.util.ArrayList;
import java.util.List;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import butterknife.BindView;
import butterknife.ButterKnife;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.paginators.Paginator;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;
import java.util.Set;
import static com.capstone.jarr.concurrent.con_postsync.int_dataupdate;

/**
 * Created on 09/09/2018.
 */
public class ui_list extends AppCompatActivity implements ui_post.OnPostClickListener,
        LoaderManager.LoaderCallbacks, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int POSTS_LOADER_ID = 11;

    @BindView(R.id.post_list)
    RecyclerView rv_postrv;
    @BindView(R.id.toolbar)
    Toolbar tb_toolbar;
    @BindView(R.id.layoutRefresh)
    SwipeRefreshLayout lo_swiperefresh;
    @BindView(R.id.vwAds)
    AdView av_adview;
    @BindView(R.id.noMsg)
    TextView tv_emptymsg;

    private String str_tag = ui_list.class.getSimpleName();
    private List<Submission> ls_posts = new ArrayList<>();
    private ui_post uip_adapter;
    private SubredditPaginator spag_paginator;
    private SharedPreferences sharep_preference;
    private Tracker tr_tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lout_list);
        ButterKnife.bind(this);

        frontapp application = (frontapp) getApplication();
        tr_tracker = application.getDefaultTracker();

        MobileAds.initialize(getApplicationContext(), getString(R.string.str_testid));
        av_adview.loadAd(new AdRequest.Builder().build());

        sharep_preference = PreferenceManager.getDefaultSharedPreferences(this);

        setSupportActionBar(tb_toolbar);
        tb_toolbar.setTitle(getTitle());

        uip_adapter = new ui_post(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        rv_postrv.setLayoutManager(layoutManager);
        rv_postrv.setAdapter(uip_adapter);
        uip_adapter.setOnPostClickListener(this);
        rv_postrv.addItemDecoration(new DividerItemDecoration(this,
                LinearLayoutManager.VERTICAL));
        rv_postrv.addOnScrollListener(new ui_scroll(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                getSupportLoaderManager().restartLoader(POSTS_LOADER_ID, null,
                        ui_list.this).forceLoad();
            }
        });

        lo_swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPosts();
            }
        });

        con_postsync.initialize(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharep_preference.registerOnSharedPreferenceChangeListener(this);

        tr_tracker.setScreenName(getString(R.string.str_postlist));
        tr_tracker.send(new HitBuilders.ScreenViewBuilder().build());

        checkAuthentication();
    }

    private void checkAuthentication() {
        AuthenticationState state = AuthenticationManager.get().checkAuthState();
        Log.d(str_tag, "AuthenticationState for onResume(): " + state);
        switch (state) {
            case READY:
                if (ls_posts.size() == 0) {
                    loadPosts();
                }
                break;
            case NONE:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.str_login);
                builder.setCancelable(false);
                builder.setMessage(R.string.str_redlog);
                builder.setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        startActivity(new Intent(ui_list.this, ui_login.class));
                        overridePendingTransition(R.anim.anim_inright,R.anim.anim_outleft);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case NEED_REFRESH:
                refreshAccessTokenAsync();
                break;
        }
    }

    private void refreshAccessTokenAsync() {
        new AsyncTask<Credentials, Void, Void>() {
            @Override
            protected Void doInBackground(Credentials... params) {
                try {
                    AuthenticationManager.get().refreshAccessToken(ui_login.CREDENTIALS);
                } catch (Exception e) {
                    Log.e(str_tag, "Could not refresh access token", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                Log.d(str_tag, "Reauthenticated");
                loadPosts();
            }
        }.execute();
    }

    @Override
    public void onPostClicked(Submission post) {
        Intent i = new Intent(this, ui_detail.class);
        i.putExtra(ui_detail.ARG_ITEM_ID, post.getDataNode().toString());
        startActivity(i);
        overridePendingTransition(R.anim.anim_inright,R.anim.anim_outleft);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.men_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        String sortType = sharep_preference.getString(getString(R.string.str_stype),
                getString(R.string.str_shot));
        if (sortType.equals(getString(R.string.str_shot))) {
            menu.findItem(R.id.hot).setChecked(true);
        } else if (sortType.equals(getString(R.string.str_snew))) {
            menu.findItem(R.id.latest).setChecked(true);
        } else if (sortType.equals(getString(R.string.str_stop))) {
            menu.findItem(R.id.top).setChecked(true);
        } else if (sortType.equals(getString(R.string.str_scontro))) {
            menu.findItem(R.id.controversial).setChecked(true);
        } else {
            return super.onPrepareOptionsMenu(menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                startActivity(new Intent(this, ui_search.class));
                overridePendingTransition(R.anim.anim_inright,R.anim.anim_outleft);
                break;
            case R.id.manage_srs:
                startActivity(new Intent(this, ui_manage.class));
                overridePendingTransition(R.anim.anim_inright,R.anim.anim_outleft);
                break;
            case R.id.account:
                startActivity(new Intent(this, ui_profile.class));
                overridePendingTransition(R.anim.anim_inright,R.anim.anim_outleft);
                break;
            case R.id.latest:
                item.setChecked(true);
                saveSortPreference(getString(R.string.str_snew));
                break;
            case R.id.top:
                item.setChecked(true);
                saveSortPreference(getString(R.string.str_stop));
                break;
            case R.id.hot:
                item.setChecked(true);
                saveSortPreference(getString(R.string.str_shot));
                break;
            case R.id.controversial:
                item.setChecked(true);
                saveSortPreference(getString(R.string.str_scontro));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void saveSortPreference(String sortType) {
        sharep_preference.edit().putString(getString(R.string.str_stype), sortType).apply();
    }

    private void loadPosts() {
        lo_swiperefresh.setRefreshing(true);
        this.ls_posts.clear();
        uip_adapter.setPosts(null);
        spag_paginator = new SubredditPaginator(AuthenticationManager.get().getRedditClient());
        spag_paginator.setSorting(getSortType());
        getSupportLoaderManager().restartLoader(POSTS_LOADER_ID, null, this).forceLoad();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new PostsAsyncTaskLoader(this, spag_paginator);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        lo_swiperefresh.setRefreshing(false);
        tv_emptymsg.setVisibility(View.GONE);
        rv_postrv.setVisibility(View.VISIBLE);

        if (data != null) {
            List<Submission> ls_posts = (List<Submission>) data;
            this.ls_posts.addAll(ls_posts);
            uip_adapter.setPosts(this.ls_posts);
        }


        if (uip_adapter.getItemCount() == 0) {
            tv_emptymsg.setVisibility(View.VISIBLE);
            rv_postrv.setVisibility(View.GONE);
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

    public Sorting getSortType() {
        String type = sharep_preference.getString(getString(R.string.str_stype),
                getString(R.string.str_shot));
        if (type.equals(getString(R.string.str_shot))) {
            return Sorting.HOT;
        } else if (type.equals(getString(R.string.str_snew))) {
            return Sorting.NEW;
        } else if (type.equals(getString(R.string.str_stop))) {
            return Sorting.TOP;
        } else if (type.equals(getString(R.string.str_scontro))) {
            return Sorting.CONTROVERSIAL;
        }
        return null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.str_stype))) {
            loadPosts();
        }
    }

    private static class PostsAsyncTaskLoader extends AsyncTaskLoader {
        private Paginator spag_paginator;
        private Context context;

        public PostsAsyncTaskLoader(Context context, Paginator spag_paginator) {
            super(context);
            this.spag_paginator = spag_paginator;
            this.context = context;
        }

        @Override
        public Object loadInBackground() {
            try {
                AuthenticationState state = AuthenticationManager.get().checkAuthState();
                if (state == AuthenticationState.NEED_REFRESH) {
                    AuthenticationManager.get().refreshAccessToken(ui_login.CREDENTIALS);
                }

                List<Submission> favSRPosts = new ArrayList<>();
                List<Submission> notFavSRPosts = new ArrayList<>();
                Set<String> favSubredditIds = comset_preference.getFavoriteSubreddits(context);
                List<Submission> ls_posts = spag_paginator.next();

                if (spag_paginator.getPageIndex() == 1) {
                    ArrayList<ContentValues> postCVs = new ArrayList<>();
                    for (Submission post : ls_posts) {
                        ContentValues postCV = new ContentValues();
                        postCV.put(dat_contract.posts.DATA, post.getDataNode().toString());
                        postCVs.add(postCV);
                    }

                    if (postCVs.size() > 0) {
                        context.getContentResolver().delete(dat_contract.posts.CONTENT_URI, null, null);
                        context.getContentResolver().bulkInsert(dat_contract.posts.CONTENT_URI, postCVs.toArray(
                                new ContentValues[postCVs.size()]
                        ));
                    }
                    Intent dataUpdatedIntent = new Intent(int_dataupdate);
                    context.sendBroadcast(dataUpdatedIntent);
                }

                for (Submission post : ls_posts) {
                    if (favSubredditIds.contains(post.getSubredditId()
                            .replace(context.getString(R.string.str_presub), ""))) {
                        favSRPosts.add(post);
                    } else {
                        notFavSRPosts.add(post);
                    }
                }
                favSRPosts.addAll(notFavSRPosts);
                return favSRPosts;
            } catch (Exception e) {
                return null;
            }
        }
    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sharep_preference.unregisterOnSharedPreferenceChangeListener(this);
    }
}