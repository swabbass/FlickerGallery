package com.wardabbass.flickergallery

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.DefaultItemAnimator
import android.view.Menu
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.MenuItem
import android.view.View
import com.wardabbass.flickergallery.adapter.GalleryAdapter
import com.wardabbass.flickergallery.adapter.GalleryItemClickListener
import com.wardabbass.flickergallery.common.DataManager
import com.wardabbass.flickergallery.common.widgets.PullToLoadView
import com.wardabbass.flickergallery.models.FlickerImageItem
import org.jetbrains.anko.toast
import android.support.v4.view.ViewCompat
import android.support.v4.app.ActivityOptionsCompat
import com.androidnetworking.AndroidNetworking
import com.wardabbass.flickergallery.models.FlickerResponse
import com.wardabbass.flickergallery.service.AlarmPullReceiver
import com.wardabbass.flickergallery.service.SearchJobInfo
import com.wardabbass.flickergallery.service.SearchPullService.Companion.EXTRA_JOB_CONTENTID
import com.wardabbass.flickergallery.service.SearchPullService.Companion.EXTRA_JOB_QUERY
import io.reactivex.disposables.CompositeDisposable
import android.support.design.widget.AppBarLayout
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.thefinestartist.finestwebview.FinestWebView
import com.wardabbass.flickergallery.common.widgets.FlickerGridLayoutManager
import com.wardabbass.flickergallery.service.SearchPullService
import org.jetbrains.anko.dip


class MainActivity : AppCompatActivity() {


    companion object {
        const val PULLING_INTERVAL_SEC = 10
    }

    var spanCount = 3
    var currentPage = 1
    var lastPage = -1
    var runningJobTag = SearchJobInfo("", "")
    var searchView: SearchView? = null
    lateinit var stopPollingAction: MenuItem
    lateinit var startPollingAction: MenuItem
    lateinit var webUrlOrNormalCheckAction: MenuItem
    var searchMenuItem: MenuItem? = null
    lateinit var clearSearch: MenuItem
    lateinit var pullToLoadView: PullToLoadView
    lateinit var galleryAdapter: GalleryAdapter

    val compositeDisposable = CompositeDisposable()

    val usingAlarManager=false
    val handler = Handler()


    val pullRunnable= Runnable {

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPullToLoadView()

        pullToLoadView.initLoading()


    }

    private fun initPullToLoadView() {
        pullToLoadView = findViewById(R.id.pulltoloadView)
        pullToLoadView.layoutManager = FlickerGridLayoutManager(baseContext, spanCount) as LinearLayoutManager
        galleryAdapter = GalleryAdapter(spanCount)
        pullToLoadView.recyclerView.itemAnimator = DefaultItemAnimator().apply {
            supportsChangeAnimations = false
        }
        pullToLoadView.pageSize = DataManager.PAGE_SIZE
        pullToLoadView.recyclerView.adapter = galleryAdapter
        pullToLoadView.onRefresh = {
            handleRefresh()
        }
        pullToLoadView.onLoadMore = { itemsCount, pageSize ->
            handleLoadMore(itemsCount, pageSize)
        }
        galleryAdapter.clickListener = object : GalleryItemClickListener {
            override fun onItemClicked(item: FlickerImageItem, position: Int, view: View) {
                if (webUrlOrNormalCheckAction.isChecked) {
                    handleOpenWebUrl(item)
                } else {
                    handleOpenSharedTransitionImage(item, view)
                }

            }
        }
    }

    private fun handleOpenSharedTransitionImage(item: FlickerImageItem, view: View) {
        val args = Bundle()
        args.putParcelable(FullIImageActivity.EXTRA_IMAGE, item)
        val intent = Intent(this@MainActivity, FullIImageActivity::class.java)
        intent.putExtras(args)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@MainActivity,
                view,
                ViewCompat.getTransitionName(view))
        startActivity(intent, options.toBundle())
    }

    private fun handleOpenWebUrl(item: FlickerImageItem) {
        FinestWebView.Builder(this@MainActivity)
                .titleDefault(getString(R.string.app_name))
                .toolbarScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS)
                .gradientDivider(false)
                .toolbarColorRes(R.color.colorPrimary)
                .titleColor(Color.WHITE)
                .disableIconBack(true)
                .disableIconMenu(true)
                .disableIconForward(true)

                .dividerColorRes(R.color.colorPrimaryDark)
                .iconDefaultColorRes(R.color.colorAccent)
                .progressBarHeight(dip(4))
                .progressBarColorRes(R.color.colorAccent)
                .backPressToClose(true)
                .setCustomAnimations(R.anim.activity_open_enter, R.anim.activity_open_exit, R.anim.activity_close_enter, R.anim.activity_close_exit)
                .show(item.getWebUrl())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu);
        searchMenuItem = menu.findItem(R.id.action_search)
        stopPollingAction = menu.findItem(R.id.action_stop_polling)
        startPollingAction = menu.findItem(R.id.action_start_polling)
        clearSearch = menu.findItem(R.id.action_clear_search)
        webUrlOrNormalCheckAction = menu.findItem(R.id.action_web_normal)


        searchMenuItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                handleSearchMenuExpanded()
                return true

            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                handleSearchMenuCollapsed()
                return true
            }

        })
        searchView = searchMenuItem?.actionView as SearchView
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                handleQuerySubmitted(query)
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {
            R.id.action_stop_polling -> {
                handleStopPolling()
                true
            }
            R.id.action_start_polling -> {
                handleStartPolling()
                true
            }
            R.id.action_clear_search -> {
                searchMenuItem?.collapseActionView()
                true
            }
            R.id.action_web_normal -> {
                webUrlOrNormalCheckAction.isChecked = !webUrlOrNormalCheckAction.isChecked
                true
            }

            else -> {
                super.onOptionsItemSelected(item)

            }
        }

    }

    private fun handleLoadMore(itemsCount: Int, pageSize: Int) {
        if (currentPage + 1 <= lastPage) {
            ++currentPage
            if (searchMenuItem == null || searchMenuItem?.isActionViewExpanded == false || runningJobTag.tag.isBlank())
                fetchRecent(currentPage)
            else
                searchView?.query?.let { fetchSearch(it, currentPage) }
        }

    }

    private fun handleRefresh() {
        //  toast("refreshing")
        currentPage = 1
        if (searchMenuItem == null || searchMenuItem?.isActionViewExpanded == false || runningJobTag.tag.isBlank())
            fetchRecent(currentPage)
        else
            searchView?.query?.let { fetchSearch(it, currentPage) }

    }

    private fun fetchRecent(page: Int) {
        compositeDisposable.add(DataManager.getRecent(page).subscribe({ result, err ->
            handleFlickerResponse(err, result)
        }
        ))

    }

    private fun fetchSearch(query: CharSequence, currentPage: Int) {
        compositeDisposable.add(DataManager.search(query.toString(), currentPage).subscribe({ result, err ->
            if (searchMenuItem?.isActionViewExpanded == true) { //if we have search opened
                handleFlickerResponse(err, result)
                result?.let {
                    if (it.photos.page == 1) {
                        clearSearch.isVisible = true
                        if (!stopPollingAction.isVisible) {
                            startPollingAction.isVisible = true
                            stopPollingAction.isVisible = false
                        }
                    }

                }
            } else {
                pullToLoadView.completeLoading()
                clearSearch.isVisible = false
                startPollingAction.isVisible = false
                stopPollingAction.isVisible = false
            }
        }
        ))

    }

    private fun handleFlickerResponse(err: Throwable?, result: FlickerResponse?) {
        pullToLoadView.completeLoading()

        err?.let {
            it.printStackTrace()
            return
        }

        result?.let {
            if (it.photos.page == 1) {
                galleryAdapter.items = it.photos.photo.toMutableList()
            } else {
                galleryAdapter.addItems(it.photos.photo)
            }

            lastPage = it.photos.pages

            pullToLoadView.isLastPage = it.photos.page == lastPage

            pullToLoadView.layoutManager.scrollToPosition(0)
//            toast("successs roma ")
        }
    }

    private fun handleQuerySubmitted(query: String) {
        if (query.isNotBlank()) {
            Log.d(SearchPullService.TAG, "handleQuerySubmitted ")

            cancelPullJobIfExists()
            runningJobTag.tag = query
            startPollingAction.isVisible = false
            stopPollingAction.isVisible = false
            pullToLoadView.initLoading()
        }
    }

    private fun handleSearchMenuCollapsed() {

        if (clearSearch.isVisible) {
            compositeDisposable.clear()
            searchView?.setQuery("", false) //empty search field
            AndroidNetworking.forceCancelAll() //stop all requests

            pullToLoadView.completeLoading() // if loading set complete loading\
            Log.d(SearchPullService.TAG, "handleSearchMenuCollapsed ")

            cancelPullJobIfExists() // cancel pull job if exists
            runningJobTag.currentId = "" //reset running pull job
            runningJobTag.tag = "" //reset running pull job
            clearSearch.isVisible = false
            startPollingAction.isVisible = false
            stopPollingAction.isVisible = false
            pullToLoadView.initLoading()
        }
    }

    private fun handleSearchMenuExpanded() {
        clearSearch.isVisible = false
        startPollingAction.isVisible = false
        stopPollingAction.isVisible = false
    }

    private fun cancelPullJobIfExists() {
        //  toast("cancel job $runningJobTag")
        Log.d(SearchPullService.TAG, "cancelPullJobIfExists ")

        NotificationManagerCompat.from(this).cancel(SearchPullService.NOTIFICATION_ID)

        cancelAlarm()
        // request?.id?.let { WorkManager.getInstance().cancelWorkById(it) }
        //its one job and cancel all will kill one job at each time there will be max 1 job
        // dispatcher?.cancelAll()

    }

    private fun handleStartPolling() {
        Log.d(SearchPullService.TAG, "handleStartPolling ")

        startPollingAction.isVisible = false
        stopPollingAction.isVisible = true
        runningJobTag.currentId = if (galleryAdapter.itemCount > 0) galleryAdapter.get(0).id else ""

        val args = Bundle()
        args.putString(EXTRA_JOB_CONTENTID, runningJobTag.currentId)
        args.putString(EXTRA_JOB_QUERY, runningJobTag.tag)
        val intent = Intent(applicationContext, AlarmPullReceiver::class.java)
        intent.putExtras(args)
        val pIntent = PendingIntent.getBroadcast(this, AlarmPullReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val firstMillis = System.currentTimeMillis()
        val alarm = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                (PULLING_INTERVAL_SEC * 1000).toLong(), pIntent);
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(SearchPullService.TAG, "onNewIntent ")

        //   cancelPullJobIfExists() // clear last alarm
        //runningJobTag.currentId = intent?.getStringExtra(EXTRA_JOB_CONTENTID) ?: runningJobTag.currentId
        // handleStartPolling()

        // runningJobTag.tag= intent?.getStringExtra(EXTRA_JOB_QUERY)?:runningJobTag.currentId
        //assume we have the same query did not change otherwise we have to set the query in the search view and load the state
        //   cancelPullJobIfExists()
        pullToLoadView.initLoading()
    }

    private fun cancelAlarm() {
        Log.d(SearchPullService.TAG, "cancelAlarm ")

        val args = Bundle()
        val intent = Intent(applicationContext, AlarmPullReceiver::class.java)
        intent.putExtras(args)
        val pIntent = PendingIntent.getBroadcast(this, AlarmPullReceiver.REQUEST_CODE,
                intent, 0)
        val alarm = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pIntent)
    }

    private fun handleStopPolling() {
        Log.d(SearchPullService.TAG, "handleStopPolling ")
        // toast("stop polling $runningJobTag")
        startPollingAction.isVisible = true
        stopPollingAction.isVisible = false
        cancelPullJobIfExists()

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(SearchPullService.TAG, "onDestroy ")

        cancelPullJobIfExists()
    }
}
