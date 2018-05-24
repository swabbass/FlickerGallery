package com.wardabbass.flickergallery.common

import com.androidnetworking.AndroidNetworking
import com.rx2androidnetworking.Rx2ANRequest
import com.rx2androidnetworking.Rx2AndroidNetworking
import com.wardabbass.flickergallery.models.FlickerResponse
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject

object DataManager {

    const val PAGE_SIZE=100
    private const val RECENT="flickr.photos.getRecent"
    private const val SEARCH="flickr.photos.search"
    private const val TEMPLATE = "https://api.flickr.com/services/rest/?method={methodName}&extras=url_s&per_page=$PAGE_SIZE&api_key=${FlickerCreds.KEY}&format=json&nojsoncallback=1&page={pageNum}"
    fun getRecent(page: Int): Single<FlickerResponse> {
        AndroidNetworking.forceCancelAll() // force cancel all requests because search and recent updating same view
        return Rx2AndroidNetworking.get(TEMPLATE)
                .addPathParameter("pageNum", "$page")
                .addPathParameter("methodName",RECENT)
                .build()
                .getObjectSingle(FlickerResponse::class.java)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun makeSearchRequest(text:String,page:Int=1): Rx2ANRequest {
        AndroidNetworking.forceCancelAll()
        return Rx2AndroidNetworking.get("$TEMPLATE&text=$text")
                .addPathParameter("pageNum", "$page")
                .addPathParameter("methodName", SEARCH)
                .build()
    }

    fun search(text:String,page:Int=1): Single<FlickerResponse>{

        return makeSearchRequest(text,page)
                .getObjectSingle(FlickerResponse::class.java)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    }
}