package com.bachors.iptv.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by user on 5/17/2017.
 */

public class PlaylistResponse {
    @SerializedName("data")
    @Expose
    private List<PlaylistData> data = null;

    public List<PlaylistData> getData() {
        return data;
    }

    public void setData(List<PlaylistData> data) {
        this.data = data;
    }

}
