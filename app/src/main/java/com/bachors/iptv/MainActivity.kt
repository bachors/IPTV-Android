package com.bachors.iptv

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowInsetsControllerCompat
import com.bachors.iptv.databinding.ActivityMainBinding
import androidx.core.net.toUri

class MainActivity : AppCompatActivity() {
    private lateinit var con: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.elevation = 0f
        supportActionBar?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        con = this

        supportActionBar?.subtitle = ContextCompat.getString(con, R.string.app_v)

        val decorView = window.decorView
        val wic = WindowInsetsControllerCompat(window, decorView)
        wic.isAppearanceLightStatusBars = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            decorView.isForceDarkAllowed = true
        }

        binding.iptvorg.setOnClickListener {
            startActivity(Intent(con, PlaylistActivity::class.java))
        }

        binding.m3ufile.setOnClickListener {
            startActivity(Intent(con, FileActivity::class.java))
        }

        binding.favorite.setOnClickListener {
            startActivity(Intent(con, FavoritesActivity::class.java))
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_download, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.download) {
            val uri = "https://github.com/bachors/IPTV-Android".toUri()
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
        return super.onOptionsItemSelected(item)
    }
}