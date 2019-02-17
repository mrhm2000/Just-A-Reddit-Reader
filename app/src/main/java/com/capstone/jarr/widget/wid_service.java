package com.capstone.jarr.widget;
import com.capstone.jarr.data.dat_provider;
import com.capstone.jarr.interfaces.ui_detail;
import com.capstone.jarr.comset.comset_const;
import android.content.Context;
import java.io.IOException;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.content.Intent;
import android.database.Cursor;
import com.capstone.jarr.R;
import com.capstone.jarr.data.dat_contract;
import net.dean.jraw.models.Submission;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Created on 09/09/2018.
 */


public class wid_service extends RemoteViewsService {
    private Context ct_context;

    public wid_service() {
        this.ct_context = this;
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {

            private Cursor data = null;
            private Cursor data1 = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }


                final long identityToken = Binder.clearCallingIdentity();

                 data = getContentResolver().query(dat_contract.posts.CONTENT_URI,
                                 null, null, null, null);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position))
                    return null;

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.lout_postlist);

                try {
                    Submission post = new Submission(new ObjectMapper()
                            .readTree(data.getString(data.getColumnIndex(dat_contract.posts.DATA))));

                    views.setTextViewText(R.id.title, post.getTitle());
                         views.setTextViewText(R.id.subreddit, post.data(comset_const.str_sredpref));

                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent fillIntent = new Intent();
                fillIntent.putExtra(ui_detail.ARG_ITEM_ID,
                        data.getString(data.getColumnIndex(dat_contract.posts.DATA)));
                views.setOnClickFillInIntent(R.id.list_item, fillIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.lout_postlist);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int i) {
                if (data.moveToPosition(i))
                       return data.getLong(data.getColumnIndex(dat_contract.posts._ID));
                return i;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
