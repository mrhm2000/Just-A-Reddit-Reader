package com.capstone.jarr.interfaces;
import com.capstone.jarr.R;
import com.capstone.jarr.design.design_subpost;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.managers.AccountManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.widget.CheckBox;
import butterknife.BindView;
import butterknife.ButterKnife;
import android.widget.TextView;
import android.widget.Toast;
import com.capstone.jarr.comset.comset_preference;
import net.dean.jraw.models.Subreddit;
import java.util.List;



/**
 * Created on 09/09/2018.
 */

public class ui_subpost extends RecyclerView.Adapter<ui_subpost.SubredditAdapterViewHolder> {
    private Context ct_context;
    private List<design_subpost> ls_sred;

    public ui_subpost(Context ct_context) {
        this.ct_context = ct_context;
    }

    public void setData(List<design_subpost> ls_sred) {
        this.ls_sred = ls_sred;
        notifyDataSetChanged();
    }

    @Override
    public SubredditAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ct_context).inflate(R.layout.lout_subpost, parent, false);
        return new SubredditAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SubredditAdapterViewHolder holder, int position) {
        final design_subpost mySubreddit = ls_sred.get(position);
        final Subreddit subreddit = mySubreddit.getSubreddit();
        holder.tv_title.setText(subreddit.getDisplayName());
        holder.iv_subnum.setText(subreddit.getSubscriberCount() + ct_context.getString(R.string.str_subscriber));
        try {
            if (subreddit.isUserSubscriber()) {
                holder.cb_subscribe.setChecked(true);
            } else {
                holder.cb_subscribe.setChecked(false);
            }
        } catch (Exception e) {
        }

        if (mySubreddit.isFavorite()) {
            holder.iv_fav.setImageResource(R.drawable.ic_mostfav);
        } else {
            holder.iv_fav.setImageResource(R.drawable.ic_leastfav);
        }

        holder.iv_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mySubreddit.setFavorite(!mySubreddit.isFavorite());
                if (mySubreddit.isFavorite()) {
                    holder.iv_fav.setImageResource(R.drawable.ic_mostfav);
                    comset_preference.addFavoriteSubreddit(ct_context, subreddit.getId());
                    Toast.makeText(ct_context,
                            subreddit.getDisplayName() +ct_context.getString(R.string.str_favmsg),
                            Toast.LENGTH_LONG).show();
                } else {
                    holder.iv_fav.setImageResource(R.drawable.ic_leastfav);
                    comset_preference.removeFavoriteSubreddit(ct_context, subreddit.getId());
                    Toast.makeText(ct_context,
                            subreddit.getDisplayName() + ct_context.getString(R.string.str_dislikemsg),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        holder.cb_subscribe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                manageSubscription(subreddit, b);
            }
        });
    }

    private void manageSubscription(final Subreddit subreddit, final boolean subscribe) {
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected void onPreExecute() {
                if (subscribe) {
                    Toast.makeText(ct_context, R.string.str_subscribingmsg, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ct_context, R.string.str_unsubscribingmsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected Object doInBackground(Void... voids) {
                try {
                    AccountManager manager = new AccountManager(AuthenticationManager.get()
                            .getRedditClient());
                    if (subscribe) {
                        manager.subscribe(subreddit);
                    } else {
                        manager.unsubscribe(subreddit);
                    }
                } catch (Exception e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object object) {
                if (object == null) {
                    if (subscribe) {
                        Toast.makeText(ct_context, R.string.str_subscribemsg, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ct_context, R.string.str_unsubscriber, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ct_context, ((Exception)object).getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    @Override
    public int getItemCount() {
        if (ls_sred == null)
            return 0;
        return ls_sred.size();
    }

    public class SubredditAdapterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.subscribe)
        CheckBox cb_subscribe;
        @BindView(R.id.favorite)
        ImageView iv_fav;
        @BindView(R.id.no_subscribers)
        TextView iv_subnum;
        @BindView(R.id.title)
        TextView tv_title;

        public SubredditAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}