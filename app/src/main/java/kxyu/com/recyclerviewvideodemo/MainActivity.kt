package kxyu.com.recyclerviewvideodemo

import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import java.util.ArrayList

/**
 * Created by kxyu on 2019/2/25
 */
class MainActivity : AppCompatActivity() {

    private var recyclerView: RecyclerView? = null

    private var mAdapter: VideoRecyclerAdapter? = null

    private var playPosition = 0
    private var layoutManager: LinearLayoutManager? = null
    private  var currentVideoPath = ""

    private var videoView: MyVideoView? = null

    private var fullScreen: FrameLayout? = null

    private var lastView: View? = null

    private var videoPosition = -1

    private val videoBeanList = ArrayList<VideoBean>()

    internal var onScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            recyclerView.baseline
            if (layoutManager != null) {
                val firstVisibleItem = layoutManager!!.findFirstVisibleItemPosition()//得到显示屏内的第一个list的位置数position
                val firstView = layoutManager!!.findViewByPosition(firstVisibleItem)
                if (dy > 0) {

                    if (firstView!!.height + firstView.top <= firstView.height / 3) {
                        //video stop or play second
                        if (layoutManager!!.childCount < 2) {
                            return
                        }
                        if (playPosition == firstVisibleItem + 1) {
                            return
                        }
                        playPosition = firstVisibleItem + 1
                        mAdapter!!.setPlay(playPosition)
                    } else {
                        if (playPosition == firstVisibleItem) {
                            return
                        }
                        playPosition = firstVisibleItem
                        mAdapter!!.setPlay(playPosition)
                    }

                    Log.i("kxyu", "dy > 0 playPosition   : $playPosition")
                } else if (dy < 0) {
                    if (firstView!!.height + firstView.top >= firstView.height * 2 / 3) {
                        //video stop or play second
                        if (layoutManager!!.childCount < 2) {
                            return
                        }
                        if (playPosition == firstVisibleItem) {
                            return
                        }
                        playPosition = firstVisibleItem
                        mAdapter!!.setPlay(playPosition)
                    } else {
                        if (playPosition == firstVisibleItem + 1) {
                            return
                        }
                        playPosition = firstVisibleItem + 1
                        mAdapter!!.setPlay(playPosition)
                    }

                    Log.i("kxyu", "dy < 0 playPosition   : $playPosition")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initData()
        initView()
        initEvent()
    }

    private fun initView() {
        recyclerView = findViewById(R.id.recyclerView) as RecyclerView
        //        videoRootViewFl = (FrameLayout) findViewById(R.id.video_root_fl);
        fullScreen = findViewById(R.id.video_full_screen) as FrameLayout
        mAdapter = VideoRecyclerAdapter(videoBeanList)
        layoutManager = LinearLayoutManager(this)
        recyclerView!!.setLayoutManager(layoutManager)
        recyclerView!!.adapter = mAdapter
    }

    private fun showVideo(view: View, videoPath: String) {
        var v: View?
        removeVideoView()

        if (videoView == null) {
            videoView = MyVideoView(this@MainActivity)

            videoView!!.setListener(object : MyVideoView.IFullScreenListener {
                override fun onClickFull(isFull: Boolean) {
                    if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                        fullScreen!!.visibility = View.VISIBLE
                        removeVideoView()
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        fullScreen!!.addView(videoView, ViewGroup.LayoutParams(-1, -1))
                        videoView!!.setVideoPath(currentVideoPath)
                        videoView!!.start()
                    } else {
                        fullScreen!!.removeAllViews()
                        fullScreen!!.visibility = View.GONE
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        if (lastView is ViewGroup) {
                            (lastView as ViewGroup).addView(videoView)
                        }
                        videoView!!.setVideoPath(currentVideoPath)
                        videoView!!.start()
                    }

                }
            })
        }
        videoView!!.stop()
        v = view.findViewById(R.id.item_imageview)
        if (v != null) v.visibility = View.INVISIBLE
        v = view.findViewById(R.id.item_image_play)
        if (v != null) v.visibility = View.INVISIBLE
        v = view.findViewById(R.id.item_video_root_fl)
        if (v != null) {
            v.visibility = View.VISIBLE
            val fl = v as FrameLayout?
            fl!!.removeAllViews()
            fl.addView(videoView, ViewGroup.LayoutParams(-1, -1))
            currentVideoPath = videoPath
            videoView!!.setVideoPath(videoPath)
            videoView!!.start()
        }
        lastView = view
    }

    private fun removeVideoView() {
        var v: View?
        if (lastView != null) {
            v = lastView!!.findViewById(R.id.item_imageview)
            if (v != null) v.visibility = View.VISIBLE
            v = lastView!!.findViewById(R.id.item_image_play)
            if (v != null) v.visibility = View.VISIBLE
            v = lastView!!.findViewById(R.id.item_video_root_fl)
            if (v != null) {
                val ll = v as FrameLayout?
                ll!!.removeAllViews()
                v.visibility = View.GONE
            }
        }
    }

    private fun initData() {
        videoBeanList.addAll(DataUtil.videoListData)
    }

    private fun initEvent() {
        mAdapter!!.setListener(object : VideoRecyclerAdapter.OnClickPlayListener {
            override fun onPlayClick(view: View, videoPath: String) {
                showVideo(view, videoPath)
            }

            override fun onClose(view: View, videoPath: String) {

            }
        })

        recyclerView!!.addOnScrollListener(onScrollListener)
    }

    public override fun onPause() {
        super.onPause()

        if (videoView != null) {
            videoPosition = videoView!!.position
            videoView!!.stop()
        }
    }

    override fun onResume() {
        super.onResume()

        if (videoView != null) {
            videoView!!.seekTo(videoPosition)
            videoView!!.start()
        }
    }

    override fun onDestroy() {
        if (videoView != null) {
            videoView!!.stop()
        }
        super.onDestroy()
    }


}
