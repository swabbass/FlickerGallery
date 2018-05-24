package com.wardabbass.flickergallery.common.widgets

import android.content.Context
import android.os.Build
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.wardabbass.flickergallery.R
import com.wardabbass.flickergallery.common.DataManager
import com.wardabbass.flickergallery.models.FlickerImageItem
import org.jetbrains.anko.dip

class PullToLoadView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, theme: Int = -1) : FrameLayout(context, attributeSet, theme) {

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
        const val PROGRESS_BAR_SIZE_DP = 32
    }

    var isLoading = false
        set(value) {
            field = value
            swipeRefreshLayout.isEnabled = !field
        }

    var isLastPage = false

    var pageSize = DEFAULT_PAGE_SIZE
    /**
     * callback whenever refresh called
     */
    var onRefresh: () -> Unit = {}

    var onLoadMore: (currentItemCount:Int,pageSize: Int) -> Unit = {
        _ ,_ ->
    }

    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    lateinit var progressView: ProgressBar


    var layoutManager: LinearLayoutManager = LinearLayoutManager(context)
        set(value) {
            field = value
            value.isItemPrefetchEnabled=false
            recyclerView.layoutManager = value
        }

    lateinit var recyclerView: RecyclerView



    private val pagingScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
        }

        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val visibleItems = layoutManager.childCount
            val itemsCount = layoutManager.itemCount

            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

            if (!isLoading && !isLastPage) {

                if ((visibleItems + firstVisibleItemPosition) >= itemsCount && firstVisibleItemPosition >= 0
                        && itemsCount >= pageSize) {
                    isLoading = true
                    onLoadMore(itemsCount,pageSize)
                    showProgressBar()
                }
            }

        }
    }

    private fun showProgressBar() {
     /*   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        TransitionManager.beginDelayedTransition(this, AutoTransition())
        }   */

        progressView.visibility = View.VISIBLE
    }

    init {
        initViews()
        initListeners()
    }

    private fun initViews() {
        View.inflate(context, R.layout.pull_to_load_view, this)
        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerView)
        progressView = findViewById(R.id.progressBar)

    }


    private fun initListeners() {
        swipeRefreshLayout.setOnRefreshListener {
            isLoading = true
            onRefresh()
        }

        recyclerView.addOnScrollListener(pagingScrollListener)
    }

    /**
     * makes first loading ,sets islastpage to false user responsible for updating this value
     */
    fun initLoading() {
        isLastPage = false
        isLoading = true
        onRefresh()
        swipeRefreshLayout.isRefreshing = true
    }

    fun completeLoading() {
        isLoading = false
        swipeRefreshLayout.isRefreshing = false
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(this, AutoTransition())
        }*/
        progressView.visibility=GONE
    }

}