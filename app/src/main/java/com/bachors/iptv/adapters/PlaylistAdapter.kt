package com.bachors.iptv.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bachors.iptv.R
import com.bachors.iptv.models.PlaylistData

class PlaylistAdapter(private val inContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val allData = mutableListOf<PlaylistData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v1 = inflater.inflate(R.layout.item_playlist, parent, false)
        return ViewHolder(v1)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = allData[position]
        val holder = holder as ViewHolder

        (position + 1).toString().also { holder.tvNo.text = it }
        holder.tvTitle.text = data.title
        "${data.channel} channels".also { holder.tvChannel.text = it }
    }

    override fun getItemCount(): Int = allData.size

    fun add(r: PlaylistData) {
        allData.add(r)
        notifyItemInserted(allData.size - 1)
    }

    fun addAll(semuaData: List<PlaylistData>) {
        for (data in semuaData) {
            add(data)
        }
    }

    fun remove(r: PlaylistData) {
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

    fun getItem(position: Int): PlaylistData = allData[position]

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNo: TextView = itemView.findViewById(R.id.no)
        val tvTitle: TextView = itemView.findViewById(R.id.title)
        val tvChannel: TextView = itemView.findViewById(R.id.channel)
    }
}