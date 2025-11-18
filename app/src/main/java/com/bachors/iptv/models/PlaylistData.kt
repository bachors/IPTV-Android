package com.bachors.iptv.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PlaylistData(
    @SerializedName("title")
    @Expose
    var title: String = "",

    @SerializedName("link")
    @Expose
    var link: String = "",

    @SerializedName("channel")
    @Expose
    var channel: String = ""
)