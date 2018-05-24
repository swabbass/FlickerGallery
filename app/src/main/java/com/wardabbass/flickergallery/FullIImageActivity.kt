package com.wardabbass.flickergallery

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v4.view.ViewCompat
import android.support.v7.widget.Toolbar
import android.transition.*
import android.view.MenuItem
import android.widget.ImageView
import androidx.core.transition.doOnEnd
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestOptions
import com.wardabbass.flickergallery.models.FlickerImageItem
import com.bumptech.glide.request.RequestListener
import com.wardabbass.flickergallery.glide.GlideApp


class FullIImageActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IMAGE = "EXTRA_IMAGE"
    }

    lateinit var imageView: ImageView
    lateinit var flickerImageItem: FlickerImageItem
    var error = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_iimage)
        supportPostponeEnterTransition()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        intent?.let {
            flickerImageItem = it.getParcelableExtra(EXTRA_IMAGE) as FlickerImageItem
        }
        savedInstanceState?.let {
            flickerImageItem = it.getParcelable(EXTRA_IMAGE) as FlickerImageItem
        }
        imageView = findViewById(R.id.imageView)
        ViewCompat.setTransitionName(imageView, flickerImageItem.id)
        GlideApp.with(this).load(flickerImageItem.url)
                .dontAnimate().dontTransform().onlyRetrieveFromCache(true)
                .placeholder(R.drawable.ic_image_placeholder).listener(object : RequestListener<Drawable> {
                    override fun onResourceReady(resource: Drawable?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        supportStartPostponedEnterTransition()
                        return false
                    }

                    override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, isFirstResource: Boolean): Boolean {

                        supportStartPostponedEnterTransition()
                        error = true

                        return false

                    }

                }).into(imageView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.sharedElementEnterTransition = TransitionSet()
                    .addTransition(ChangeImageTransform())
                    .addTransition(ChangeBounds())
                    .addTransition(ChangeTransform())
                    .addTransition(Explode())

                    .apply {

                        doOnEnd {
                            if (error) {
                                loadImage()
                            }
                        }

                    }
        } else {
            loadImage()
        }
        // actionBar.setDisplayHomeAsUpEnabled(true)
    }

    private fun loadImage() {
        GlideApp.with(this@FullIImageActivity).load(flickerImageItem.url)
                .into(imageView)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelable(EXTRA_IMAGE, flickerImageItem)

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Respond to the action bar's Up/Home button
                supportFinishAfterTransition();

                // NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}
