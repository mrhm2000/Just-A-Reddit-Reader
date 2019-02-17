package com.capstone.jarr.interfaces;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;



/**
 * Created on 09/09/2018.
 */

public abstract class ui_scroll extends RecyclerView.OnScrollListener {
    RecyclerView.LayoutManager rv_lmanager;
    // Minimum item variable
    private int int_visthres = 5;
    // Index data load
    private int int_cpage = 0;
    // Last number of item
    private int int_previtemnum = 0;
    // Set True for more data
    private boolean bol_load = true;
    // Start index
    private int int_startpage = 0;

    public ui_scroll(LinearLayoutManager layoutManager) {
        this.rv_lmanager = layoutManager;
    }

    public ui_scroll(GridLayoutManager layoutManager) {
        this.rv_lmanager = layoutManager;
        int_visthres = int_visthres * layoutManager.getSpanCount();
    }

    public ui_scroll(StaggeredGridLayoutManager layoutManager) {
        this.rv_lmanager = layoutManager;
        int_visthres = int_visthres * layoutManager.getSpanCount();
    }

    public int getLastVisibleItem(int[] lastVisibleItemPositions) {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i];
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i];
            }
        }
        return maxSize;
    }


    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        int lastVisibleItemPosition = 0;
        int totalItemCount = rv_lmanager.getItemCount();

        if (rv_lmanager instanceof StaggeredGridLayoutManager) {
            int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) rv_lmanager).findLastVisibleItemPositions(null);
            lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);
        } else if (rv_lmanager instanceof GridLayoutManager) {
            lastVisibleItemPosition = ((GridLayoutManager) rv_lmanager).findLastVisibleItemPosition();
        } else if (rv_lmanager instanceof LinearLayoutManager) {
            lastVisibleItemPosition = ((LinearLayoutManager) rv_lmanager).findLastVisibleItemPosition();
        }


        if (totalItemCount < int_previtemnum) {
            this.int_cpage = this.int_startpage;
            this.int_previtemnum = totalItemCount;
            if (totalItemCount == 0) {
                this.bol_load = true;
            }
        }

        if (bol_load && (totalItemCount > int_previtemnum)) {
            bol_load = false;
            int_previtemnum = totalItemCount;
        }

        if (!bol_load && (lastVisibleItemPosition + int_visthres) > totalItemCount) {
            int_cpage++;
            onLoadMore(int_cpage, totalItemCount, view);
            bol_load = true;
        }
    }


    public void resetState() {
        this.int_cpage = this.int_startpage;
        this.int_previtemnum = 0;
        this.bol_load = true;
    }


    public abstract void onLoadMore(int page, int totalItemsCount, RecyclerView view);

}