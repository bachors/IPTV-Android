package com.bachors.iptv

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bachors.iptv.adapters.FavoritesAdapter
import com.bachors.iptv.models.ChannelsData
import com.bachors.iptv.utils.RecyclerTouchListener
import com.bachors.iptv.utils.SharedPrefManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FavoritesActivity : AppCompatActivity() {
    private lateinit var sharedPrefManager: SharedPrefManager
    private var key: Int = 0
    private lateinit var mcon: Context
    private val allData = mutableListOf<ChannelsData>()
    private var searchData: List<ChannelsData>? = null
    private var all = true
    private lateinit var adapter: FavoritesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channels)
        supportActionBar?.elevation = 0f
        supportActionBar?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        supportActionBar?.subtitle = "Favorites"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mcon = this

        val decorView = window.decorView
        val wic = WindowInsetsControllerCompat(window, decorView)
        wic.isAppearanceLightStatusBars = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            decorView.isForceDarkAllowed = true
        }

        sharedPrefManager = SharedPrefManager(this)
        adapter = FavoritesAdapter(this)

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val rv = findViewById<RecyclerView>(R.id.rv)
        rv.layoutManager = linearLayoutManager
        rv.itemAnimator = DefaultItemAnimator()
        rv.adapter = adapter
        rv.addOnItemTouchListener(RecyclerTouchListener(this, rv, object : RecyclerTouchListener.ClickListener {
            override fun onClick(view: View, position: Int) {
                goTo(position)
            }

            override fun onLongClick(view: View, position: Int) {
                goDel(position)
            }
        }))
        adapter.clear()
        jsonTogson()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun jsonTogson() {
        val listType = object : TypeToken<List<ChannelsData>>() {}.type
        val gson = Gson()
        val data: List<ChannelsData> = gson.fromJson(sharedPrefManager.getSpFavorites(), listType)
        allData.clear()
        allData.addAll(data)
        adapter.addAll(allData)
    }

    private fun filter(text: String) {
        searchData = allData.filter { it.name.lowercase().contains(text.lowercase()) }
        adapter.addAll(searchData!!)
    }

    private fun goTo(id: Int) {
        key = if (all) id else allData.indexOf(searchData!![id])
        val intent = Intent(mcon, PlayerActivity::class.java)
        intent.putExtra("name", allData[key].name)
        intent.putExtra("url", allData[key].url)
        startActivity(intent)
    }

    private fun goDel(id: Int) {
        key = if (all) id else allData.indexOf(searchData!![id])

        MaterialAlertDialogBuilder(mcon)
            .setTitle("Delete ?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                try {
                    val arr = JSONArray(sharedPrefManager.getSpFavorites())
                    arr.remove(key)
                    sharedPrefManager.saveSPString(SharedPrefManager.SP_FAVORITES, arr.toString())
                    adapter.clear()
                    all = true
                    jsonTogson()
                } catch (_: Exception) {
                    // Handle exception
                }
            }
            .setNegativeButton("No", null)
            .show()
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
}