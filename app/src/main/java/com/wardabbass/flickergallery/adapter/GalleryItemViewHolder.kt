package com.wardabbass.flickergallery.adapter

import android.graphics.Color
import android.support.v4.view.ViewCompat
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.wardabbass.flickergallery.R
import com.wardabbass.flickergallery.common.adapters.BaseViewHolder
import com.wardabbass.flickergallery.glide.GlideApp
import com.wardabbass.flickergallery.models.FlickerImageItem
import org.jetbrains.anko.backgroundColor

class GalleryItemViewHolder(view: View) : BaseViewHolder<FlickerImageItem, GalleryItemClickListener>(view) {

     var imageView: ImageView = itemView as ImageView

    init {
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
    }

    override fun onBind(item: FlickerImageItem, listener: GalleryItemClickListener?) {
        GlideApp.with(imageView)
                .load(item.url)
                .centerCrop()
                .dontAnimate()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_broken_image_black_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
        ViewCompat.setTransitionName(imageView,item.id)
        itemView.setOnClickListener {
            listener?.onItemClicked(item, adapterPosition,imageView)
        }
    }



}