package com.bachors.iptv.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ChannelsData(
    @SerializedName("name")
    @Expose
    var name: String = "",

    @SerializedName("logo")
    @Expose
    var logo: String = "",

    @SerializedName("url")
    @Expose
    var url: String = ""
)