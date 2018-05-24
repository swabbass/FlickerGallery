package com.wardabbass.flickergallery.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class AlarmPullReceiver : BroadcastReceiver() {
companion object {
   const val REQUEST_CODE = 9988

}
    override fun onReceive(context: Context?, intent: Intent?) {

        val i = Intent(context, SearchPullService::class.java)
        i.putExtras(intent)
        context?.startService(i);

    }

}