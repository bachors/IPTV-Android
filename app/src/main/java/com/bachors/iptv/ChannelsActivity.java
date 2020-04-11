package com.bachors.iptv;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.bachors.iptv.adapter.ChannelsAdapter;
import com.bachors.iptv.models.ChannelsData;
import com.bachors.iptv.models.ChannelsResponse;
import com.bachors.iptv.util.RecyclerTouchListener;
import com.bachors.iptv.util.SharedPrefManager;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChannelsActivity extends AppCompatActivity {

    ProgressDialog loading;

    SharedPrefManager sharedPrefManager;
    int key;

    Context mcon;

    List<ChannelsData> allData = new ArrayList<>();
    List<ChannelsData> searchData;
    boolean all = true;
    ChannelsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channels);

        Bundle b = getIntent().getExtras();
        getSupportActionBar().setSubtitle(Html.fromHtml("<small>"+b.getString("title")+"</small>"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mcon = this;

        sharedPrefManager = new SharedPrefManager(this);

        adapter = new ChannelsAdapter(this);

        RecyclerView rv = (RecyclerView) findViewById(R.id.rv);
        GridLayoutManager gl = new GridLayoutManager(this, 2);
        rv.setLayoutManager(gl);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(adapter);
        rv.addOnItemTouchListener(new RecyclerTouchListener(this, rv, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                goTo(position);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        adapter.clear();
        jsonTogson();

    }

    private void jsonTogson() {
        try {
            JSONArray ar = new JSONArray(sharedPrefManager.getSpChannels());
            JSONObject ob = new JSONObject();
            ob.put("data", ar);
            Gson gson = new Gson();
            ChannelsResponse channels = gson.fromJson(ob.toString(), ChannelsResponse.class);
            allData = channels.getData();
            adapter.addAll(allData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void filter(String text) {
        //new array list that will hold the filtered data
        searchData = new ArrayList<>();

        //looping through existing elements
        for (ChannelsData s : allData) {
            //if the existing elements contains the search input
            if (s.getName().toLowerCase().contains(text.toLowerCase())) {
                //adding the element to filtered list
                searchData.add(s);
            }
        }

        //calling a method of the adapter class and passing the filtered list
        adapter.addAll(searchData);
    }

    private void goTo(int id){
        if(all){
            key = id;
        }else{
            key = allData.indexOf(searchData.get(id));
        }

        Intent intent = new Intent(mcon, PlayerActivity.class);
        intent.putExtra("name", allData.get(key).getName());
        intent.putExtra("url", allData.get(key).getUrl());
        startActivity(intent);

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        getMenuInflater().inflate(R.menu.menu_search, menu);
        getMenuInflater().inflate(R.menu.menu_load, menu);

        MenuItem mSearch = menu.findItem(R.id.action_search);

        SearchView mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setQueryHint("Search...");

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.clear();
                //after the change calling the method and passing the search input
                if(s.equals("")){
                    all = true;
                    adapter.addAll(allData);
                }else {
                    all = false;
                    filter(s);
                }
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem actionViewItem2 = menu.findItem(R.id.load);
        // Retrieve the action-view from menu
        View v = MenuItemCompat.getActionView(actionViewItem2);
        Button x = (Button) v.findViewById(R.id.btn_load);
        // Handle button click here
        x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading = ProgressDialog.show(mcon, null, "Update data ...", true, false);
                loadMain();
            }
        });
        return super.onPrepareOptionsMenu(menu);
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
                        loading.dismiss();

                        sharedPrefManager.saveSPString(SharedPrefManager.SP_PLAYLIST, json.toString());

                        Intent launchNextActivity;
                        launchNextActivity = new Intent(getApplicationContext(), ChannelsActivity.class);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(launchNextActivity);
                        overridePendingTransition(0, 0);
                    }
                });
            }
        }).start();
    }

}
