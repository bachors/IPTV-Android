package com.bachors.iptv.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bachors.iptv.R;
import com.bachors.iptv.models.PlaylistData;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<PlaylistData> allData;
    private Context inContext;

    public PlaylistAdapter(Context context) {
        this.inContext = context;
        allData = new ArrayList<>();
    }

    public List<PlaylistData> getData() {
        return allData;
    }

    public void setData(List<PlaylistData> allData) {
        this.allData = allData;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        viewHolder = getViewHolder(parent, inflater);
        return viewHolder;
    }

    @NonNull
    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        RecyclerView.ViewHolder viewHolder;
        View v1 = inflater.inflate(R.layout.item_playlist, parent, false);
        viewHolder = new NisnVH(v1);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        PlaylistData data = allData.get(position);

        final NisnVH nisnVH = (NisnVH) holder;

        nisnVH.tvTitle.setText(data.getTitle());

    }

    @Override
    public int getItemCount() {
        return allData == null ? 0 : allData.size();
    }

    /*
   Helpers
   _________________________________________________________________________________________________
    */


    public void add(PlaylistData r) {
        allData.add(r);
        notifyItemInserted(allData.size() - 1);
    }

    public void addAll(List<PlaylistData> semuaData) {
        for (PlaylistData data : semuaData) {
            add(data);
        }
    }

    public void remove(PlaylistData r) {
        int position = allData.indexOf(r);
        if (position > -1) {
            allData.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public PlaylistData getItem(int position) {
        return allData.get(position);
    }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Main list's content ViewHolder
     */
    protected class NisnVH extends RecyclerView.ViewHolder {
        private TextView tvTitle;

        public NisnVH(View itemView) {
            super(itemView);

            tvTitle = (TextView) itemView.findViewById(R.id.title);

        }
    }

}