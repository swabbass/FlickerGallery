package com.wardabbass.flickergallery

import android.app.Application
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.wardabbass.flickergallery.service.SearchPullService


class MainApp :Application(){

    override fun onCreate() {
        super.onCreate()
        AndroidNetworking.initialize(this)

    }

    override fun onTerminate() {
        Log.d(SearchPullService.TAG,"onTerminate")
        super.onTerminate()
    }
}