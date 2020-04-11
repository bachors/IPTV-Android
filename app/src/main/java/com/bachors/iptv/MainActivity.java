package com.bachors.iptv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bachors.iptv.util.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefManager = new SharedPrefManager(this);
        if(sharedPrefManager.getSpPlaylist().equals("") || sharedPrefManager.getSpPlaylist().equals("[]")) {
            loadMain();
        }else{
            goTo();
        }
    }

    private void loadMain() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final JSONArray json = new JSONArray();

                try {
                    Document doc = Jsoup.connect("https://raw.githubusercontent.com/iptv-org/iptv/master/README.md").get();

                    Elements tables = doc.select("table");

                    for (Element table : tables) {
                        JSONArray ar = new JSONArray();
                        Elements trs = table.select("tr");
                        int i = 0;
                        for (Element tr : trs) {
                            if(i > 0) {
                                JSONObject ob = new JSONObject();
                                ob.put("title", tr.select("td").eq(0).text());
                                ob.put("link", tr.select("td").eq(2).select("code").eq(0).text());
                                ar.put(ob);
                            }
                            i++;
                        }
                        json.put( ar);
                    }
                } catch (IOException ignored) {

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sharedPrefManager.saveSPString(SharedPrefManager.SP_PLAYLIST, json.toString());
                        goTo();
                    }
                });
            }
        }).start();
    }

    private void goTo(){
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        Intent launchNextActivity;
                        launchNextActivity = new Intent(getApplicationContext(), PlaylistActivity.class);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(launchNextActivity);
                        overridePendingTransition(0, 0);
                    }
                },
                5000);
    }

}
