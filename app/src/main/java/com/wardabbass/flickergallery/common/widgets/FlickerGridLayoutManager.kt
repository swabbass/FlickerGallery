package com.wardabbass.flickergallery.common.widgets

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.wardabbass.flickergallery.adapter.GalleryAdapter.Companion.ROWS_COUNT

class FlickerGridLayoutManager (context: Context,spanCount:Int) : GridLayoutManager (context,spanCount){

    var extraSpace = 3*(context.resources.displayMetrics.heightPixels/ROWS_COUNT)
    init {

    }
    override fun getExtraLayoutSpace(state: RecyclerView.State?): Int {

        return extraSpace
    }
}