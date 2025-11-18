package com.bachors.iptv

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bachors.iptv.adapters.PlaylistAdapter
import com.bachors.iptv.databinding.ActivityFilesBinding
import com.bachors.iptv.models.PlaylistData
import com.bachors.iptv.utils.RecyclerTouchListener
import com.bachors.iptv.utils.SharedPrefManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.util.regex.Pattern
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FileActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "FileActivity"
        private const val EXT_M3U = "#EXTM3U"
        private const val EXT_INF = "#EXTINF:"
        private const val EXT_LOGO = "tvg-logo"
        private const val EXT_HTTP = "http://"
        private const val EXT_HTTPS = "https://"
    }

    private lateinit var binding: ActivityFilesBinding
    private var goLink: String? = null
    private var goTitle: String? = null
    private var goJson: String? = null
    private lateinit var sharedPrefManager: SharedPrefManager
    private var key: Int = 0
    private lateinit var mcon: Context
    private val allData = mutableListOf<PlaylistData>()
    private var searchData: List<PlaylistData>? = null
    private var all = true
    private lateinit var adapter: PlaylistAdapter
    private lateinit var saveDirectory: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.elevation = 0f
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.subtitle = "M3U File"
        supportActionBar?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        mcon = this

        val decorView = window.decorView
        val wic = WindowInsetsControllerCompat(window, decorView)
        wic.isAppearanceLightStatusBars = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            decorView.isForceDarkAllowed = true
        }

        sharedPrefManager = SharedPrefManager(this)
        adapter = PlaylistAdapter(this)
        saveDirectory = File(getExternalFilesDir(null), "")

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

        binding.add.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/x-mpegurl"
            resData.launch(intent)
        }

        adapter.clear()
        jsonTogson()

        val intent = intent
        val action = intent.action
        val type = intent.type
        if (Intent.ACTION_SEND == action && type != null) {
            if (type.startsWith("audio/x-mpegurl")) {
                handleSendM3u(intent)
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun handleSendM3u(intent: Intent) {
        val m3uUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }
        contentResolver.query(m3uUri!!, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            val filename = cursor.getString(nameIndex).replace("\\s".toRegex(), "")
            if (filename.lowercase().endsWith(".m3u")) {
                if (copyFileFromUri(mcon, m3uUri, filename)) {
                    val m3u = File(saveDirectory, filename)
                    val uri = "file://$m3u".toUri()
                    val txt = usingBufferedReader(uri)
                    val extInfPattern = Pattern.compile(EXT_INF)
                    val matcher = extInfPattern.matcher(txt)
                    var count = 0
                    while (matcher.find()) {
                        count++
                    }
                    val ar = JSONArray(sharedPrefManager.getSpFiles())
                    val ob = JSONObject()
                    ob.put("title", filename)
                    ob.put("channel", count.toString())
                    ob.put("link", uri.toString())
                    ar.put(ob)
                    sharedPrefManager.saveSPString(SharedPrefManager.SP_FILES, ar.toString())
                    all = true
                    adapter.clear()
                    jsonTogson()

                    goTitle = filename
                    goLink = uri.toString()
                    loadChannels()
                } else {
                    Toast.makeText(applicationContext, "Failed...", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(applicationContext, "Failed: wrong file format...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val resData: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    val filename = cursor.getString(nameIndex).replace("\\s".toRegex(), "")
                    if (filename.lowercase().endsWith(".m3u")) {
                        if (copyFileFromUri(mcon, uri, filename)) {
                            val m3u = File(saveDirectory, filename)
                            val fileUri = "file://$m3u".toUri()
                            val txt = usingBufferedReader(fileUri)
                            val extInfPattern = Pattern.compile(EXT_INF)
                            val matcher = extInfPattern.matcher(txt)
                            var count = 0
                            while (matcher.find()) {
                                count++
                            }
                            val ar = JSONArray(sharedPrefManager.getSpFiles())
                            val ob = JSONObject()
                            ob.put("title", filename)
                            ob.put("channel", count.toString())
                            ob.put("link", fileUri.toString())
                            ar.put(ob)
                            sharedPrefManager.saveSPString(SharedPrefManager.SP_FILES, ar.toString())
                            all = true
                            adapter.clear()
                            jsonTogson()
                        } else {
                            Toast.makeText(applicationContext, "Failed...", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, "Failed: wrong file format...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun copyFileFromUri(context: Context, fileUri: Uri, name: String): Boolean {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        return try {
            val content = context.contentResolver
            inputStream = content.openInputStream(fileUri)

            val cfile = File(saveDirectory, name)
            if (cfile.exists()) {
                cfile.delete()
            }

            outputStream = FileOutputStream(File(saveDirectory, name))
            Log.e(TAG, "Output Stream Opened successfully")

            val buffer = ByteArray(1000)
            var bytesRead: Int
            while (inputStream!!.read(buffer, 0, buffer.size).also { bytesRead = it } >= 0) {
                outputStream.write(buffer, 0, buffer.size)
            }
            File(saveDirectory, name).exists()
        } catch (e: Exception) {
            Log.e(TAG, "Exception occurred ${e.message}")
            false
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    private fun usingBufferedReader(filePath: Uri): String {
        val contentBuilder = StringBuilder()
        try {
            contentResolver.openInputStream(filePath)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { br ->
                    var sCurrentLine: String?
                    while (br.readLine().also { sCurrentLine = it } != null) {
                        contentBuilder.append(sCurrentLine).append("\n")
                    }
                }
            }
        } catch (_: Exception) {
            // Handle exception
        }
        return contentBuilder.toString()
    }

    private fun jsonTogson() {
        try {
            val ar = JSONArray(sharedPrefManager.getSpFiles())
            val listType = object : TypeToken<List<PlaylistData>>() {}.type
            val gson = Gson()
            val data: List<PlaylistData> = gson.fromJson(ar.toString(), listType)
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
        loadChannels()
    }

    private fun goDel(id: Int) {
        key = if (all) id else allData.indexOf(searchData!![id])

        MaterialAlertDialogBuilder(mcon)
            .setTitle("Delete ?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                try {
                    val arr = JSONArray(sharedPrefManager.getSpFiles())
                    arr.remove(key)
                    sharedPrefManager.saveSPString(SharedPrefManager.SP_FILES, arr.toString())
                    val rm = File(saveDirectory, allData[key].title)
                    if (rm.exists()) {
                        rm.delete()
                    }
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

    private fun loadChannels() {
        val uri = goLink!!.toUri()
        val stream = usingBufferedReader(uri).replace("#EXTVLCOPT:(.*)".toRegex(), "")

        val linesArray = stream.split(EXT_INF)
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
}