package com.bachors.iptv.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bachors.iptv.R
import com.bachors.iptv.models.ChannelsData
import com.squareup.picasso.Picasso

class FavoritesAdapter(private val inContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val allData = mutableListOf<ChannelsData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v1 = inflater.inflate(R.layout.item_favorites, parent, false)
        return ViewHolder(v1)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = allData[position]
        val holder = holder as ViewHolder

        val logo = data.logo
        holder.tvName.text = data.name
        Picasso.get()
            .load(logo)
            .placeholder(ContextCompat.getDrawable(inContext, R.drawable.load)!!)
            .into(holder.tvLogo)
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
        val tvName: TextView = itemView.findViewById(R.id.name)
        val tvLogo: ImageView = itemView.findViewById(R.id.logo)
    }
}