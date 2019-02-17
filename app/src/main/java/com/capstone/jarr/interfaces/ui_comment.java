package com.capstone.jarr.interfaces;
import android.view.LayoutInflater;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import com.capstone.jarr.comset.comset_date;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import net.dean.jraw.models.CommentNode;
import com.capstone.jarr.R;
import butterknife.ButterKnife;
import com.google.common.collect.FluentIterable;
import butterknife.BindView;
import net.dean.jraw.models.Comment;

/**
 * Created on 09/09/2018.
 */

public class ui_comment extends RecyclerView.Adapter<ui_comment.CommentsAdapterViewHolder> {
    private Context ct_context;
    private FluentIterable<CommentNode> commentNodes;

    public ui_comment(Context ct_context) {
        this.ct_context = ct_context;
    }

    @Override
    public CommentsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ct_context).inflate(R.layout.lout_comment, parent, false);
        return new CommentsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentsAdapterViewHolder holder, int position) {
        CommentNode commentNode = commentNodes.get(position);
        Comment comment = commentNode.getComment();
        holder.mBody.setText(comment.getBody());
        holder.mAuthor.setText(ct_context.getString(R.string.str_preusr) + comment.getAuthor() + "   "
                + comset_date.convert(comment.getCreated().getTime()));
        holder.itemView.setPadding(makeIndent(commentNode.getDepth()), 0, 0, 0);
    }

    public void setCommentNodes(FluentIterable<CommentNode> commentNodes) {
        this.commentNodes = commentNodes;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (commentNodes == null)
            return 0;
        return commentNodes.size();
    }

    public class CommentsAdapterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.idpage)
        TextView mBody;
        @BindView(R.id.idcreator)
        TextView mAuthor;

        public CommentsAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private int makeIndent(int depth) {
        float scale = ct_context.getResources().getDisplayMetrics().density;
        return (int) (10 * depth * scale + 0.5f);
    }
}