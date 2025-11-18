package com.bachors.iptv.utils

import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.URL

class HttpHandler {
    companion object {
        private val TAG = HttpHandler::class.java.simpleName
    }

    fun makeServiceCall(reqUrl: String?): String? {
        var response: String? = null
        try {
            val url = URL(reqUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            val inputStream = BufferedInputStream(conn.inputStream)
            response = convertStreamToString(inputStream)
        } catch (e: MalformedURLException) {
            Log.e(TAG, "MalformedURLException: ${e.message}")
        } catch (e: ProtocolException) {
            Log.e(TAG, "ProtocolException: ${e.message}")
        } catch (e: IOException) {
            Log.e(TAG, "IOException: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
        }
        return response
    }

    private fun convertStreamToString(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()

        try {
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append('\n')
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return sb.toString()
    }
}