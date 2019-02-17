package com.capstone.jarr.design;

import net.dean.jraw.models.Subreddit;

/**
 * Created on 09/09/2018.
 */

public class design_subpost {
    private boolean isFavorite;
    private Subreddit subreddit;
    public boolean isFavorite() {
        return isFavorite;
    }
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
    public Subreddit getSubreddit() {
        return subreddit;
    }
    public void setSubreddit(Subreddit subreddit) {
        this.subreddit = subreddit;
    }
}
