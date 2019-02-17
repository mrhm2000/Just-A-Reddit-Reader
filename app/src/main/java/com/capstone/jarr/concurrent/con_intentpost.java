package com.capstone.jarr.concurrent;
import android.support.annotation.Nullable;
import android.content.Intent;
import android.app.IntentService;

/**
 * Created on 09/09/2018.
 */

public class con_intentpost extends IntentService {
    public con_intentpost() {
        super(con_intentpost.class.getSimpleName());

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        con_postsync.getPosts(getApplicationContext());
    }
}
