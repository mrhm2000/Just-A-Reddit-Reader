package com.capstone.jarr.concurrent;
import android.app.job.JobService;
import android.app.job.JobParameters;
import android.content.Intent;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


/**
 * Created on 09/09/2018.
 */

public class con_jobpost extends JobService {
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Intent nowIntent = new Intent(getApplicationContext(), con_intentpost.class);
        getApplicationContext().startService(nowIntent);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
