package com.bachors.iptv.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    public static final String SP_SS_APP = "spIPTV";

    public static final String SP_PLAYLIST = "spPlaylist";
    public static final String SP_CHANNELS = "spChannels";

    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    public SharedPrefManager(Context context){
        sp = context.getSharedPreferences(SP_SS_APP, Context.MODE_PRIVATE);
        spEditor = sp.edit();
    }

    public void saveSPString(String keySP, String value){
        spEditor.putString(keySP, value);
        spEditor.commit();
    }

    public String getSpPlaylist(){
        return sp.getString(SP_PLAYLIST, "[]");
    }
    public String getSpChannels(){
        return sp.getString(SP_CHANNELS, "{}");
    }

}
