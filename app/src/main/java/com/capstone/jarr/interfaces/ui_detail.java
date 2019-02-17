package com.capstone.jarr.interfaces;
import android.net.Uri;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import com.capstone.jarr.frontapp;
import android.os.Build;
import android.os.Bundle;
import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import com.capstone.jarr.comset.comset_const;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.capstone.jarr.comset.comset_date;
import android.support.v7.widget.LinearLayoutManager;
import com.capstone.jarr.R;
import com.squareup.picasso.Picasso;
import android.support.customtabs.CustomTabsIntent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;

/**
 * Created on 09/09/2018.
 */
public class ui_detail extends AppCompatActivity implements LoaderManager.LoaderCallbacks {


    public static final String ARG_ITEM_ID = "item_id";
    public static final int COMMENTS_LOADER_ID = 12;

    @BindView(R.id.subreddit)
    TextView tv_subred;
    @BindView(R.id.title)
    TextView tv_title;
    @BindView(R.id.idpage)
    TextView tv_body;
    @BindView(R.id.id_pic)
    ImageView tv_photo;
    @BindView(R.id.commentsList)
    RecyclerView rv_comment;
    @BindView(R.id.ups)
    TextView tv_voteup;
    @BindView(R.id.downs)
    TextView tv_votedown;
    @BindView(R.id.comments)
    TextView tv_ncomment;
    @BindView(R.id.progressbar)
    ProgressBar tv_pbar;

    private Submission subm_post = null;
    private ui_comment uic_adapter;
    private Boolean bol_liked = null;
    private Tracker tr_tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lout_details);
        ButterKnife.bind(this);

        if (getIntent().getExtras().containsKey(ARG_ITEM_ID)) {
            String postJson = getIntent().getExtras().getString(ARG_ITEM_ID);
            try {
                subm_post = new Submission(new ObjectMapper().readTree(postJson));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        getSupportActionBar().setTitle(subm_post.data(comset_const.str_sredpref));

        frontapp application = (frontapp) getApplication();
        tr_tracker = application.getDefaultTracker();


        tv_subred.setText(subm_post.data(comset_const.str_sredpref)
                .concat("   u/").concat(subm_post.getAuthor())
                .concat("   ").concat(comset_date.convert(subm_post.getCreated().getTime())));
        tv_title.setText(subm_post.getTitle());

        uic_adapter = new ui_comment(this);
        LinearLayoutManager manager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        rv_comment.setLayoutManager(manager);
        rv_comment.setAdapter(uic_adapter);

        if (subm_post.data(comset_const.str_likes) != null) {
            if (Boolean.parseBoolean(subm_post.data(comset_const.str_likes))) {
                bol_liked = true;
                setDrawableLeft(tv_voteup, R.drawable.ic_like_filled);
            } else {
                bol_liked = false;
                setDrawableLeft(tv_votedown, R.drawable.ic_dislike_filled);
            }
        } else {
            bol_liked = null;
            setDrawableLeft(tv_voteup, R.drawable.ic_like_empty);
            setDrawableLeft(tv_votedown, R.drawable.ic_dislike_empty);
        }

        tv_voteup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bol_liked == null || !bol_liked) {
                    bol_liked = true;
                    votePost(subm_post, VoteDirection.UPVOTE,
                            R.drawable.ic_like_filled, R.drawable.ic_dislike_empty);
                } else {
                    bol_liked = null;
                    votePost(subm_post, VoteDirection.NO_VOTE,
                            R.drawable.ic_like_empty, R.drawable.ic_dislike_empty);
                }
            }
        });

        tv_voteup.setText(subm_post.data(comset_const.str_ups));
        tv_votedown.setText(subm_post.data(comset_const.str_down));
        tv_votedown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bol_liked == null || bol_liked) {
                    bol_liked = false;
                    votePost(subm_post, VoteDirection.DOWNVOTE,
                            R.drawable.ic_like_empty, R.drawable.ic_dislike_filled);
                } else {
                    bol_liked = null;
                    votePost(subm_post, VoteDirection.NO_VOTE,
                            R.drawable.ic_like_empty, R.drawable.ic_dislike_empty);
                }
            }
        });
        tv_ncomment.setText(String.valueOf(subm_post.getCommentCount()));

        switch (subm_post.getPostHint()) {
            case SELF:
                try {
                    tv_body.setVisibility(View.VISIBLE);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        tv_body.setText(Html.fromHtml(StringEscapeUtils
                                        .unescapeHtml4(subm_post.data(comset_const.str_htmltxt)),
                                Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        tv_body.setText(Html.fromHtml(StringEscapeUtils
                                .unescapeHtml4(subm_post.data(comset_const.str_htmltxt))));
                    }

                    tv_body.setMovementMethod(LinkMovementMethod.getInstance());
                } catch (Exception e) {
                }
                break;
            case LINK:
            case VIDEO:
                try {
                    tv_body.setVisibility(View.VISIBLE);
                    tv_body.setText(subm_post.getUrl());
                    tv_body.setTextColor(Color.BLUE);
                    tv_body.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
                            customTabsIntent.launchUrl(ui_detail.this, Uri.parse(subm_post.getUrl()));
                        }
                    });
                } catch (Exception e) {
                }
                break;
            case IMAGE:
                tv_photo.setVisibility(View.VISIBLE);

                String imageRosolutions = subm_post.getDataNode().get(comset_const.str_prev)
                        .get(comset_const.str_images).get(0)
                        .get(comset_const.str_res).toString();
                try {
                    JSONArray resolutionsArr = new JSONArray(imageRosolutions);
                    for (int i = 0; i < resolutionsArr.length(); i++) {
                        JSONObject imageObject = resolutionsArr.getJSONObject(i);
                        if (imageObject.getInt(comset_const.str_width) == 216) {
                            String imgUrl = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                imgUrl = Html.fromHtml(StringEscapeUtils
                                                .unescapeHtml4(imageObject.getString(comset_const.str_url)),
                                        Html.FROM_HTML_MODE_LEGACY).toString();
                            } else {
                                imgUrl = Html.fromHtml(StringEscapeUtils
                                        .unescapeHtml4(imageObject.getString(comset_const.str_url))).toString();
                            }

                            Picasso.with(this).load(imgUrl).into(tv_photo);
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }
                break;
            case UNKNOWN:
                try {
                    tv_body.setVisibility(View.VISIBLE);
                    tv_body.setText(Html.fromHtml(StringEscapeUtils
                            .unescapeHtml4(subm_post.data(comset_const.str_htmltxt))));
                    tv_body.setMovementMethod(LinkMovementMethod.getInstance());
                } catch (Exception e) {
                }
                break;
        }

        getSupportLoaderManager().initLoader(COMMENTS_LOADER_ID, null, this).forceLoad();
    }

    @Override
    public void onResume() {
        super.onResume();
        tr_tracker.setScreenName(getString(R.string.str_postdetail));
        tr_tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new CommentsAsyncTaskLoader(this, subm_post.getId());
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        tv_pbar.setVisibility(View.GONE);
        if (data != null) {
            if (data instanceof CommentNode) {
                CommentNode root = (CommentNode) data;
                uic_adapter.setCommentNodes(root.walkTree());
                rv_comment.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    private static class CommentsAsyncTaskLoader extends AsyncTaskLoader {
        private String postId;

        public CommentsAsyncTaskLoader(Context context, String postId) {
            super(context);
            this.postId = postId;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
        }

        @Override
        public Object loadInBackground() {
            try {
                AuthenticationState state = AuthenticationManager.get().checkAuthState();
                if (state == AuthenticationState.NEED_REFRESH) {
                    AuthenticationManager.get().refreshAccessToken(ui_login.CREDENTIALS);
                }
                RedditClient client = AuthenticationManager.get().getRedditClient();
                Submission subm_post = client.getSubmission(postId);
                return subm_post.getComments();
            } catch (Exception e) {
                return null;
            }
        }
    }

    @OnClick(R.id.share)
    public void sharePost() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.str_redweb)
                .concat(subm_post.getPermalink()));
        startActivity(shareIntent);
        //overridePendingTransition(R.anim.anim_inright,R.anim.anim_outleft);
    }

    private void votePost(Submission submission,
                          VoteDirection voteDirection, int upward_arrow, int downward_arrow) {
        vote(submission, voteDirection);
        setDrawableLeft(tv_voteup, upward_arrow);
        setDrawableLeft(tv_votedown, downward_arrow);
    }

    private void vote(final Submission submission, final VoteDirection voteDirection) {
        final AccountManager manager = new AccountManager(AuthenticationManager.get().getRedditClient());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AuthenticationState state = AuthenticationManager.get().checkAuthState();
                    if (state == AuthenticationState.NEED_REFRESH) {
                        AuthenticationManager.get().refreshAccessToken(ui_login.CREDENTIALS);
                    }
                    manager.vote(submission, voteDirection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setDrawableLeft(TextView view, int drawableId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setCompoundDrawablesWithIntrinsicBounds(
                    getDrawable(drawableId),
                    null, null, null);
        } else {
            view.setCompoundDrawablesWithIntrinsicBounds(
                    getResources().getDrawable(drawableId),
                    null, null, null
            );
        }
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
}
