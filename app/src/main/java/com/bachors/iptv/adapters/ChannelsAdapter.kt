package com.bachors.iptv.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bachors.iptv.PlayerActivity
import com.bachors.iptv.R
import com.bachors.iptv.models.ChannelsData
import com.bachors.iptv.utils.SharedPrefManager
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject

class ChannelsAdapter(private val inContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val allData = mutableListOf<ChannelsData>()
    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v1 = inflater.inflate(R.layout.item_channels, parent, false)
        return ViewHolder(v1)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = allData[position]
        val holder = holder as ViewHolder

        sharedPrefManager = SharedPrefManager(inContext)

        val logo = data.logo
        holder.tvName.text = data.name
        Picasso.get()
            .load(logo)
            .placeholder(ContextCompat.getDrawable(inContext, R.drawable.load)!!)
            .into(holder.tvLogo)

        holder.lnPlay.setOnClickListener {
            val intent = Intent(inContext, PlayerActivity::class.java)
            intent.putExtra("name", data.name)
            intent.putExtra("url", data.url)
            inContext.startActivity(intent)
        }

        holder.btFavorite.setOnClickListener {
            try {
                val ar = JSONArray(sharedPrefManager.getSpFavorites())
                val ob = JSONObject()
                ob.put("name", data.name)
                ob.put("logo", data.logo)
                ob.put("url", data.url)
                ar.put(ob)
                sharedPrefManager.saveSPString(SharedPrefManager.SP_FAVORITES, ar.toString())
                Toast.makeText(inContext, "Saved to Favorites...", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                // Handle exception
            }
        }
    }

    override fun getItemCount(): Int = allData.size

    fun add(r: ChannelsData) {
        allData.add(r)
        notifyItemInserted(allData.size - 1)
    }

    fun addAll(semuaData: List<ChannelsData>) {
        for (data in semuaData) {
            add(data)
        }
    }

    fun remove(r: ChannelsData) {
        val position = allData.indexOf(r)
        if (position > -1) {
            allData.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clear() {
        while (itemCount > 0) {
            remove(getItem(0))
        }
    }

    fun isEmpty(): Boolean = itemCount == 0

    fun getItem(position: Int): ChannelsData = allData[position]

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lnPlay: LinearLayout = itemView.findViewById(R.id.play)
        val tvName: TextView = itemView.findViewById(R.id.name)
        val tvLogo: ImageView = itemView.findViewById(R.id.logo)
        val btFavorite: ImageView = itemView.findViewById(R.id.btn_favorite)
    }
}