package com.bachors.iptv

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bachors.iptv.adapters.PlaylistAdapter
import com.bachors.iptv.databinding.ActivityPlaylistBinding
import com.bachors.iptv.models.PlaylistData
import com.bachors.iptv.utils.HttpHandler
import com.bachors.iptv.utils.RecyclerTouchListener
import com.bachors.iptv.utils.SharedPrefManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup

class PlaylistActivity : AppCompatActivity() {
    companion object {
        private const val EXT_M3U = "#EXTM3U"
        private const val EXT_INF = "#EXTINF:"
        private const val EXT_LOGO = "tvg-logo"
        private const val EXT_HTTP = "http://"
        private const val EXT_HTTPS = "https://"
    }

    private var stream: String? = null
    private var goLink: String? = null
    private var goTitle: String? = null
    private var goJson: String? = null
    private lateinit var loading: AlertDialog
    private lateinit var sharedPrefManager: SharedPrefManager
    private var key: Int = 0
    private var index: Int = 0
    private lateinit var mcon: Context
    private val allData = mutableListOf<PlaylistData>()
    private var searchData: List<PlaylistData>? = null
    private var all = true
    private lateinit var adapter: PlaylistAdapter
    var binding: ActivityPlaylistBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        supportActionBar?.elevation = 0f
        supportActionBar?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mcon = this

        val decorView = window.decorView
        val wic = WindowInsetsControllerCompat(window, decorView)
        wic.isAppearanceLightStatusBars = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            decorView.isForceDarkAllowed = true
        }

        sharedPrefManager = SharedPrefManager(this)

        val builder2 = MaterialAlertDialogBuilder(this, R.style.MyDialogTheme)
        builder2.setCancelable(false)
        builder2.setMessage("Please wait...")
        loading = builder2.create()

        adapter = PlaylistAdapter(this)

        setupAk()
        binding?.playlist?.setOnItemClickListener({ parent, view, position, id ->
            index = position
            adapter.clear()
            jsonTogson()
        })

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val rv = findViewById<RecyclerView>(R.id.rv)
        rv.layoutManager = linearLayoutManager
        rv.itemAnimator = DefaultItemAnimator()
        rv.adapter = adapter
        rv.addOnItemTouchListener(RecyclerTouchListener(this, rv, object : RecyclerTouchListener.ClickListener {
            override fun onClick(view: View, position: Int) {
                goTo(position)
            }

            override fun onLongClick(view: View, position: Int) {}
        }))

        val refresh = findViewById<ImageView>(R.id.btn_load)
        refresh.setOnClickListener {
            loading.show()
            loadPlaylists()
        }

        if (sharedPrefManager.getSpPlaylist().isEmpty() || sharedPrefManager.getSpPlaylist() == "[]") {
            loading.show()
            loadPlaylists()
        } else {
            adapter.clear()
            jsonTogson()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    fun setupAk() {
        val pl = mutableListOf<String>()
        pl.add("Category")
        pl.add("Language")
        val dataAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, pl)
        binding?.playlist?.setAdapter(dataAdapter);
        binding?.playlist?.setText(pl[0], false)
    }

    private fun jsonTogson() {
        try {
            val ar = JSONArray(sharedPrefManager.getSpPlaylist())
            val listType = object : TypeToken<List<PlaylistData>>() {}.type
            val gson = Gson()
            val data: List<PlaylistData> = gson.fromJson(ar.getJSONArray(index).toString(), listType)
            allData.clear()
            allData.addAll(data)
            adapter.addAll(allData)
        } catch (_: Exception) {
            // Handle exception
        }
    }

    private fun filter(text: String) {
        searchData = allData.filter { it.title.lowercase().contains(text.lowercase()) }
        adapter.addAll(searchData!!)
    }

    private fun goTo(id: Int) {
        key = if (all) id else allData.indexOf(searchData!![id])
        goTitle = allData[key].title
        goLink = allData[key].link
        loading.show()
        loadChannels()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        menuInflater.inflate(R.menu.menu_download, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.queryHint = "Search..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean = false

            override fun onQueryTextChange(s: String): Boolean {
                adapter.clear()
                if (s.isEmpty()) {
                    all = true
                    adapter.addAll(allData)
                } else {
                    all = false
                    filter(s)
                }
                return true
            }
        })
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        if (item.itemId == R.id.download) {
            val uri = "https://github.com/bachors/IPTV-Android".toUri()
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadPlaylists() {
        Thread {
            val json = JSONArray()
            try {
                val doc = Jsoup.connect("https://raw.githubusercontent.com/iptv-org/iptv/master/PLAYLISTS.md").get()
                val tables = doc.select("table")
                for (table in tables) {
                    val ar = JSONArray()
                    val trs = table.select("tr")
                    var i = 0
                    for (tr in trs) {
                        if (i > 0) {
                            val ob = JSONObject()
                            ob.put("title", tr.select("td").eq(0).text())
                            ob.put("channel", tr.select("td").eq(1).text())
                            ob.put("link", tr.select("td").eq(2).select("code").eq(0).text())
                            ar.put(ob)
                        }
                        i++
                    }
                    json.put(ar)
                }
            } catch (_: Exception) {}

            runOnUiThread {
                loading.dismiss()
                sharedPrefManager.saveSPString(SharedPrefManager.SP_PLAYLIST, json.toString())
                adapter.clear()
                jsonTogson()
            }
        }.start()
    }

    private fun loadChannels() {
        Thread {
            val sh = HttpHandler()
            val result = sh.makeServiceCall(goLink)
            if (result != null) {
                stream = result.replace("#EXTVLCOPT:(.*)".toRegex(), "")
            }

            runOnUiThread {
                loading.dismiss()
                val linesArray = stream!!.split(EXT_INF)
                val ar = JSONArray()
                for (currLine in linesArray) {
                    if (!currLine.contains(EXT_M3U)) {
                        val ob = JSONObject()
                        val dataArray = currLine.split(",")
                        try {
                            val name: String
                            val url: String
                            if (dataArray[1].contains(EXT_HTTPS)) {
                                name = dataArray[1].substring(0, dataArray[1].indexOf(EXT_HTTPS)).replace("\n", "")
                                url = dataArray[1].substring(dataArray[1].indexOf(EXT_HTTPS)).replace("\n", "").replace("\r", "")
                            } else {
                                name = dataArray[1].substring(0, dataArray[1].indexOf(EXT_HTTP)).replace("\n", "")
                                url = dataArray[1].substring(dataArray[1].indexOf(EXT_HTTP)).replace("\n", "").replace("\r", "")
                            }
                            ob.put("name", name)
                            ob.put("url", url)
                            if (dataArray[0].contains(EXT_LOGO)) {
                                val logo = dataArray[0].substring(dataArray[0].indexOf(EXT_LOGO) + EXT_LOGO.length).replace("=", "").replace("\"", "").replace("\n", "")
                                ob.put("logo", logo)
                            } else {
                                ob.put("logo", "")
                            }
                            ar.put(ob)
                        } catch (fdfd: Exception) {
                            Log.e("Google", "Error: ${fdfd.fillInStackTrace()}")
                        }
                    }
                }

                goJson = ar.toString()
                sharedPrefManager.saveSPString(SharedPrefManager.SP_CHANNELS, goJson!!)
                val intent = Intent(mcon, ChannelsActivity::class.java)
                intent.putExtra("title", allData[key].title)
                startActivity(intent)
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        setupAk()
    }
}