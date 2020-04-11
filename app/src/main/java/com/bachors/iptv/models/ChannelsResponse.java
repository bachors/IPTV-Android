package com.bachors.iptv.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by user on 5/17/2017.
 */

public class ChannelsResponse {
    @SerializedName("data")
    @Expose
    private List<ChannelsData> data = null;

    public List<ChannelsData> getData() {
        return data;
    }

    public void setData(List<ChannelsData> data) {
        this.data = data;
    }

}
