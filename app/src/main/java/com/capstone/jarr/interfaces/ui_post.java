package com.capstone.jarr.interfaces;
import butterknife.BindView;
import butterknife.ButterKnife;
import android.content.Context;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.managers.AccountManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import com.capstone.jarr.R;
import com.capstone.jarr.comset.comset_date;
import com.capstone.jarr.comset.comset_const;
import com.squareup.picasso.Picasso;
import android.content.Intent;
import android.os.Build;
import net.dean.jraw.auth.AuthenticationManager;
/**
 * Created on 09/09/2018.
 */

public class ui_post extends RecyclerView.Adapter<ui_post.PostsAdapterViewHolder> {

    private Context ct_context;
    private List<Submission> ls_posts;
    private OnPostClickListener cl_clicklisten;
    private Boolean bol_liked = null;

    public ui_post(Context ct_context) {
        this.ct_context = ct_context;
    }

    public void setOnPostClickListener(OnPostClickListener cl_clicklisten) {
        this.cl_clicklisten = cl_clicklisten;
    }

    public void setPosts(List<Submission> ls_posts) {
        this.ls_posts = ls_posts;
        notifyDataSetChanged();
    }

    @Override
    public PostsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ct_context).inflate(R.layout.lout_itemlist, parent, false);
        return new PostsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PostsAdapterViewHolder holder, int position) {
        final Submission submission = ls_posts.get(position);
        holder.tvsredname.setText(submission.data(comset_const.str_sredpref)
                .concat("  ").concat(comset_date.convert(submission.getCreated().getTime())));
        holder.tv_title.setText(submission.getTitle());
        holder.tv_comnum.setText(String.valueOf(submission.getCommentCount()));
        holder.tv_uvote.setText(submission.data(comset_const.str_ups));
        holder.tv_downvote.setText(submission.data(comset_const.str_down));

        if (submission.data(comset_const.str_likes) != null) {
            if (Boolean.parseBoolean(submission.data(comset_const.str_likes))) {
                bol_liked = true;
                setDrawableLeft(holder.tv_uvote, R.drawable.ic_like_filled);
            } else {
                bol_liked = false;
                setDrawableLeft(holder.tv_downvote, R.drawable.ic_dislike_filled);
            }
        } else {
            bol_liked = null;
            setDrawableLeft(holder.tv_uvote, R.drawable.ic_like_empty);
            setDrawableLeft(holder.tv_downvote, R.drawable.ic_dislike_empty);
        }

        if (submission.getThumbnailType().equals(Submission.ThumbnailType.URL)) {
            holder.iv_tnail.setVisibility(View.VISIBLE);
            Picasso.with(ct_context).load(submission.getThumbnail()).fit().centerCrop().into(holder.iv_tnail);
        } else {
            holder.iv_tnail.setVisibility(View.GONE);
        }

        holder.tv_uvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bol_liked == null || !bol_liked) {
                    bol_liked = true;
                    votePost(submission, holder, VoteDirection.UPVOTE,
                            R.drawable.ic_like_filled, R.drawable.ic_dislike_empty);
                } else {
                    bol_liked = null;
                    votePost(submission, holder, VoteDirection.NO_VOTE,
                            R.drawable.ic_like_empty, R.drawable.ic_dislike_empty);
                }

            }
        });
        holder.tv_downvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bol_liked == null || bol_liked) {
                    bol_liked = false;
                    votePost(submission, holder, VoteDirection.DOWNVOTE,
                            R.drawable.ic_like_empty, R.drawable.ic_dislike_filled);
                } else {
                    bol_liked = null;
                    votePost(submission, holder, VoteDirection.NO_VOTE,
                            R.drawable.ic_like_empty, R.drawable.ic_dislike_empty);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cl_clicklisten.onPostClicked(submission);
            }
        });

        holder.tv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, ct_context.getString(R.string.str_redweb)
                        .concat(submission.getPermalink()));
                ct_context.startActivity(shareIntent);
                //overridePendingTransition(R.anim.anim_inright,R.anim.anim_outleft);
            }
        });
    }

    private void votePost(Submission submission, PostsAdapterViewHolder holder,
                          VoteDirection voteDirection, int upward_arrow, int downward_arrow) {
        vote(submission, voteDirection);
        setDrawableLeft(holder.tv_uvote, upward_arrow);
        setDrawableLeft(holder.tv_downvote, downward_arrow);
    }

    private void setDrawableLeft(TextView view, int drawableId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setCompoundDrawablesWithIntrinsicBounds(
                    ct_context.getDrawable(drawableId),
                    null, null, null);
        } else {
            view.setCompoundDrawablesWithIntrinsicBounds(
                    ct_context.getResources().getDrawable(drawableId),
                    null, null, null
            );
        }
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

    @Override
    public int getItemCount() {
        if (ls_posts == null)
            return 0;
        return ls_posts.size();
    }

    interface OnPostClickListener {
        void onPostClicked(Submission post);
    }

    public class PostsAdapterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.subreddit)
        TextView tvsredname;
        @BindView(R.id.title)
        TextView tv_title;
        @BindView(R.id.thumbnail)
        ImageView iv_tnail;
        @BindView(R.id.ups)
        TextView tv_uvote;
        @BindView(R.id.downs)
        TextView tv_downvote;
        @BindView(R.id.comments)
        TextView tv_comnum;
        @BindView(R.id.share)
        TextView tv_share;

        public PostsAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
