package com.bachors.iptv

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import com.khizar1556.mkvideoplayer.MKPlayer
import androidx.core.net.toUri

class PlayerActivity : AppCompatActivity() {
    private lateinit var con: Context
    private var mkplayer: MKPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        supportActionBar?.elevation = 0f
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        con = this

        val decorView = window.decorView
        val wic = WindowInsetsControllerCompat(window, decorView)
        wic.isAppearanceLightStatusBars = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            decorView.isForceDarkAllowed = true
        }

        val b = intent.extras
        val name = b?.getString("name")
        val url = b?.getString("url")

        mkplayer = MKPlayer(this)
        mkplayer?.play(url)
        mkplayer?.setTitle(name)
        mkplayer?.setPlayerCallbacks(object : MKPlayer.playerCallbacks {
            override fun onNextClick() {}
            override fun onPreviousClick() {}
        })
        mkplayer?.setFullScreenOnly(true)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        mkplayer?.onPause()
    }

    override fun onResume() {
        super.onResume()
        mkplayer?.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mkplayer?.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_download, menu)
        return true
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