package com.wardabbass.flickergallery.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.util.FixedPreloadSizeProvider
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.wardabbass.flickergallery.R
import com.wardabbass.flickergallery.common.DataManager
import com.wardabbass.flickergallery.common.adapters.BaseAdapter
import com.wardabbass.flickergallery.glide.GlideApp
import com.wardabbass.flickergallery.glide.GlideRequests
import com.wardabbass.flickergallery.models.FlickerImageItem


class GalleryAdapter(val spanCount: Int = 3) : BaseAdapter<FlickerImageItem, GalleryItemClickListener, GalleryItemViewHolder>(), ListPreloader.PreloadModelProvider<FlickerImageItem> {


    companion object {
        const val ROWS_COUNT = 5
    }


    lateinit var glide:GlideRequests
    val preloadSizeProvider = ViewPreloadSizeProvider<FlickerImageItem>()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        glide=GlideApp.with(recyclerView)
        var preloader: RecyclerViewPreloader<FlickerImageItem> = RecyclerViewPreloader(glide, this,
                preloadSizeProvider, DataManager.PAGE_SIZE)

        recyclerView.addOnScrollListener(preloader)
    }
    init {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryItemViewHolder {
        val view = ImageView(parent.context).apply {
            val height = parent.measuredHeight / ROWS_COUNT
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        }
        preloadSizeProvider.setView(view)
        val galleryItemViewHolder = GalleryItemViewHolder(view)
        return galleryItemViewHolder
    }



    override fun onViewRecycled(holder: GalleryItemViewHolder) {
        super.onViewRecycled(holder)
        GlideApp.with(holder.imageView).clear(holder.imageView)
        holder.imageView.setImageBitmap(null)
    }

    override fun getPreloadItems(position: Int): MutableList<FlickerImageItem> {
            return items.subList(position,position+1)
    }

    override fun getPreloadRequestBuilder(item: FlickerImageItem): RequestBuilder<*>? {
        return glide.load(item.url)
                .centerCrop()
                .dontAnimate()
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_broken_image_black_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
    }
}

