package com.wardabbass.flickergallery.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.wardabbass.flickergallery.MainActivity
import com.wardabbass.flickergallery.R
import com.wardabbass.flickergallery.common.DataManager
import com.wardabbass.flickergallery.models.FlickerResponse




class SearchPullService : IntentService(TAG) {


    companion object {

        const val EXTRA_JOB_CONTENTID = "EXTRA_JOB_CONTENTID"
        const val EXTRA_JOB_QUERY = "EXTRA_JOB_QUERY"
        const val TAG = "SearchPullService"
        private const val NOTIFICATION_CHANNEL_ID = "SEARCH_CHANNEL"
         const val NOTIFICATION_ID = 1254
    }


    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "oncreate")
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "onHandleIntent")
        intent?.let {
          var  contentId = it.getStringExtra(EXTRA_JOB_CONTENTID)
          val  query = it.getStringExtra(EXTRA_JOB_QUERY)
            val response = DataManager.makeSearchRequest(query, 1).executeForObject(FlickerResponse::class.java)
            if (response?.isSuccess == true) {

                val flickerResponse = response.result as FlickerResponse
                if (flickerResponse.stat.toLowerCase() == "ok" && flickerResponse.photos.photo[0].id != contentId) {
                    Log.d(TAG, "response success different results")
                    showNotification(query,flickerResponse.photos.photo[0].id)

                } else {
                    Log.d(TAG, "response success same results")

                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")

    }

    fun showNotification(query: String, id: String) {

        val notificationManager = this.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(EXTRA_JOB_QUERY,query)
        intent.putExtra(EXTRA_JOB_CONTENTID,id)
        val pIntent = PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), intent, 0)

        val builder = getBuilder(notificationManager)
        val notification = builder.setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.new_flicker_pics))
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .setContentText(getString(R.string.new_flicker_info)).build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL

        notificationManager.notify(NOTIFICATION_ID, notification)

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG,"onTaskRemoved")
        val args = Bundle()
        val intent = Intent(applicationContext, AlarmPullReceiver::class.java)
        intent.putExtras(args)
        val pIntent = PendingIntent.getBroadcast(this, AlarmPullReceiver.REQUEST_CODE,
                intent,0)
        val alarm = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pIntent)
    }
    private fun getBuilder(notificationManager: NotificationManager): NotificationCompat.Builder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                val channelName = "Pull search results"

                val pullSearchNotificationChannel =
                        NotificationChannel(
                                NOTIFICATION_CHANNEL_ID,
                                channelName,
                                NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(pullSearchNotificationChannel)
            }
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        } else {
            NotificationCompat.Builder(this)
        }
    }
}