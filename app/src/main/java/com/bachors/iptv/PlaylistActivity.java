package com.bachors.iptv;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.bachors.iptv.adapter.PlaylistAdapter;
import com.bachors.iptv.models.PlaylistData;
import com.bachors.iptv.models.PlaylistResponse;
import com.bachors.iptv.util.HttpHandler;
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

public class PlaylistActivity extends AppCompatActivity {

    private static final String EXT_M3U = "#EXTM3U";
    private static final String EXT_INF = "#EXTINF:";
    private static final String EXT_LOGO = "tvg-logo";
    private static final String EXT_URL = "http://";

    String stream;
    String goLink;
    String goTitle;
    String goJson;

    ProgressDialog loading;

    SharedPrefManager sharedPrefManager;
    int key;
    int index = 0;

    Context mcon;

    List<PlaylistData> allData = new ArrayList<>();
    List<PlaylistData> searchData;
    boolean all = true;
    PlaylistAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        mcon = this;

        sharedPrefManager = new SharedPrefManager(this);

        adapter = new PlaylistAdapter(this);

        Spinner spinner = (Spinner) findViewById(R.id.playlist);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                index = position;
                adapter.clear();
                jsonTogson();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        List<String> pl = new ArrayList<String>();
        pl.add("Category");
        pl.add("Language");
        pl.add("Country");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, pl);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

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
            JSONArray ar = new JSONArray(sharedPrefManager.getSpPlaylist());
            JSONObject ob = new JSONObject();
            ob.put("data", ar.getJSONArray(index));
            Gson gson = new Gson();
            PlaylistResponse playlist = gson.fromJson(ob.toString(), PlaylistResponse.class);
            allData = playlist.getData();
            adapter.addAll(allData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void filter(String text) {
        //new array list that will hold the filtered data
        searchData = new ArrayList<>();

        //looping through existing elements
        for (PlaylistData s : allData) {
            //if the existing elements contains the search input
            if (s.getTitle().toLowerCase().contains(text.toLowerCase())) {
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

        goTitle = allData.get(key).getTitle();
        goLink = allData.get(key).getLink();

        loading = ProgressDialog.show(mcon, null, "Loading ...", true, false);
        new m3uParse().execute();

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
                        launchNextActivity = new Intent(getApplicationContext(), PlaylistActivity.class);
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

    /**
     * Async task class to get json by making HTTP call
     */
    @SuppressLint("StaticFieldLeak")
    private class m3uParse extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String result = sh.makeServiceCall(goLink);

            if (result != null) {
                stream = result;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            loading.dismiss();

            String[] linesArray = stream.split(EXT_INF);
                JSONArray ar = new JSONArray();
                for (String currLine : linesArray) {
                    if (!currLine.contains(EXT_M3U)) {
                        JSONObject ob = new JSONObject();
                        String[] dataArray = currLine.split(",");
                        try {
                            String name = dataArray[1].substring(0, dataArray[1].indexOf(EXT_URL)).replace("\n", "");
                            String url = dataArray[1].substring(dataArray[1].indexOf(EXT_URL)).replace("\n", "").replace("\r", "");
                            ob.put("name", name);
                            ob.put("url", url);
                            if (dataArray[0].contains(EXT_LOGO)) {
                                String logo = dataArray[0].substring(dataArray[0].indexOf(EXT_LOGO) + EXT_LOGO.length()).replace("=", "").replace("\"", "").replace("\n", "");
                                ob.put("logo", logo);
                            } else {
                                ob.put("logo", "");
                            }

                            ar.put(ob);
                        } catch (Exception fdfd) {
                            Log.e("Google", "Error: " + fdfd.fillInStackTrace());
                        }
                    }
                }

                goJson = ar.toString();

                sharedPrefManager.saveSPString(SharedPrefManager.SP_CHANNELS, goJson);
                Intent intent = new Intent(mcon, ChannelsActivity.class);
                intent.putExtra("title", allData.get(key).getTitle());
                startActivity(intent);
        }

    }

}
