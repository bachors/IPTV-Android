package com.bachors.iptv;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.khizar1556.mkvideoplayer.MKPlayer;

public class PlayerActivity extends AppCompatActivity {

    MKPlayer mkplayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Bundle b = getIntent().getExtras();
        final String name = b.getString("name");
        final String url = b.getString("url");

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mkplayer = new MKPlayer(this);
        mkplayer.play(url);
        mkplayer.setTitle(name);
        mkplayer.setPlayerCallbacks(new MKPlayer.playerCallbacks() {
            @Override
            public void onNextClick() {

            }

            @Override
            public void onPreviousClick() {

            }
        });
        mkplayer.setFullScreenOnly(true);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mkplayer != null) {
            mkplayer.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mkplayer != null) {
            mkplayer.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mkplayer != null) {
            mkplayer.onDestroy();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mkplayer != null) {
            mkplayer.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onBackPressed() {
        if (mkplayer != null && mkplayer.onBackPressed()) {
            return;
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.bachors_apps, null);
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setNegativeButton("Ok", null)
                    .setView(dialogView)
                    .show();
        }else if (id == R.id.action_rate) {
            Uri uri = Uri.parse("https://github.com/bachors/IPTV-Android");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }else if (id == R.id.action_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "https://github.com/bachors/IPTV-Android");
            startActivity(Intent.createChooser(shareIntent, "Share link using"));
        }

        return super.onOptionsItemSelected(item);
        }
    }

}
