package com.bachors.iptv.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bachors.iptv.R;
import com.bachors.iptv.models.ChannelsData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChannelsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChannelsData> allData;
    private Context inContext;

    public ChannelsAdapter(Context context) {
        this.inContext = context;
        allData = new ArrayList<>();
    }

    public List<ChannelsData> getData() {
        return allData;
    }

    public void setData(List<ChannelsData> allData) {
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
        View v1 = inflater.inflate(R.layout.item_channels, parent, false);
        viewHolder = new NisnVH(v1);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        ChannelsData data = allData.get(position);

        final NisnVH nisnVH = (NisnVH) holder;

        String logo = data.getLogo();
        if(logo.equals("")){
            nisnVH.tvLogo.setImageDrawable(inContext.getResources().getDrawable(R.drawable.load));
        }else {
            nisnVH.tvName.setText(data.getName());
            Picasso.with(inContext)
                    .load(logo)
                    .placeholder(inContext.getResources().getDrawable(R.drawable.load))
                    .into(nisnVH.tvLogo);
        }

    }

    @Override
    public int getItemCount() {
        return allData == null ? 0 : allData.size();
    }

    /*
   Helpers
   _________________________________________________________________________________________________
    */


    public void add(ChannelsData r) {
        allData.add(r);
        notifyItemInserted(allData.size() - 1);
    }

    public void addAll(List<ChannelsData> semuaData) {
        for (ChannelsData data : semuaData) {
            add(data);
        }
    }

    public void remove(ChannelsData r) {
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

    public ChannelsData getItem(int position) {
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
        private TextView tvName;
        private ImageView tvLogo;

        public NisnVH(View itemView) {
            super(itemView);

            tvName = (TextView) itemView.findViewById(R.id.name);
            tvLogo = (ImageView) itemView.findViewById(R.id.logo);

        }
    }

}