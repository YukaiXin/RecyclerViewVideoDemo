package kxyu.com.recyclerviewvideodemo

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Message
import android.support.annotation.RequiresApi
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.VideoView

/**
 * Created by kxyu on 2019/2/25
 */
class MyVideoView : RelativeLayout {
    private var rootLayout: View? = null

    private var videoView: VideoView? = null

    private var mClickView: View? = null

    private var playOrPauseCenterIv: ImageView? = null

    private var playControlLl: LinearLayout? = null

    private var playOrPauseIv: ImageView? = null

    private var seekBar: SeekBar? = null

    private var fullIv: ImageView? = null

    private var isTrackingTouch = false

    private var isShowControl = false

    /**
     * 是否处于暂停状态
     *
     * @return
     */
    var isPause = false
        private set

    private var isFull = false

    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                SHOW_CONTROL -> showControLl()
                HIDE_CONTROL -> hideControLl()
                UPDATE_POSITION -> updatePosition()
                else -> {
                }
            }
        }
    }

    private var listener: IFullScreenListener? = null

    /**
     * 是否正在播放
     *
     * @return
     */
    val isPlaying: Boolean
        get() = videoView!!.isPlaying

    val position: Int
        get() = videoView!!.currentPosition

    fun setListener(listener: IFullScreenListener) {
        this.listener = listener
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        rootLayout = LayoutInflater.from(context).inflate(R.layout.video_view, this, true)
        mClickView = findViewById(R.id.my_video_click)
        videoView = findViewById(R.id.my_videoview) as VideoView
        playOrPauseCenterIv = findViewById(R.id.my_video_center_playpause_iv) as ImageView
        playControlLl = findViewById(R.id.my_video_play_control_ll) as LinearLayout
        playOrPauseIv = findViewById(R.id.my_video_play_pause_iv) as ImageView
        seekBar = findViewById(R.id.my_video_seekbar) as SeekBar
        fullIv = findViewById(R.id.my_video_full_iv) as ImageView
        initSetting()
        initEvent()
    }

    private fun initSetting() {
        playOrPauseCenterIv!!.setImageResource(R.mipmap.ic_play_circle_outline_white_48dp)
        showControLl()
        playOrPauseIv!!.setImageResource(R.mipmap.ic_play_circle_outline_white_24dp)
        seekBar!!.progress = 0
        fullIv!!.setImageResource(R.mipmap.ic_fullscreen_white_24dp)
        seekBar!!.isEnabled = false
    }


    private fun initEvent() {
        videoView!!.setOnCompletionListener { stop() }
        videoView!!.setOnErrorListener { mp, what, extra ->
            //TODO
            false
        }
        videoView!!.setOnPreparedListener { mp ->
            playOrPauseIv!!.setImageResource(R.mipmap.ic_pause_circle_outline_white_24dp)
            playOrPauseCenterIv!!.setImageResource(R.mipmap.ic_pause_circle_outline_white_48dp)
            seekBar!!.max = mp.duration
            seekBar!!.progress = mp.currentPosition
            seekBar!!.isEnabled = true
            handler.sendEmptyMessage(UPDATE_POSITION)
        }
        seekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isTrackingTouch = true
                handler.removeMessages(HIDE_CONTROL)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isTrackingTouch = false
                handler.sendEmptyMessageDelayed(HIDE_CONTROL, 3000)
                val position = seekBar.progress
                if (videoView!!.isPlaying) {
                    videoView!!.seekTo(position)
                }
            }
        })
        mClickView!!.setOnClickListener {
            if (isShowControl) {
                hideControLl()
            } else {
                showControLl()
            }
        }
        playOrPauseIv!!.setOnClickListener {
            if (videoView!!.isPlaying) {
                pause()
            } else {
                resume()
            }
        }
        playOrPauseCenterIv!!.setOnClickListener {
            if (videoView!!.isPlaying) {
                pause()
            } else {
                resume()
            }
        }
        fullIv!!.setOnClickListener {
            isFull = !isFull
            setFullScreen(isFull)
            if (listener != null) {
                listener!!.onClickFull(isFull)
            }
        }
    }


    /**
     * 更新进度
     */
    private fun updatePosition() {
        handler.removeMessages(UPDATE_POSITION)
        if (videoView!!.isPlaying) {
            val currentPosition = videoView!!.currentPosition
            if (!isTrackingTouch) {
                seekBar!!.progress = currentPosition
            }
            handler.sendEmptyMessageDelayed(UPDATE_POSITION, 500)
        }
    }

    /**
     * 隐藏控制条
     */
    fun hideControLl() {
        isShowControl = false
        handler.removeMessages(HIDE_CONTROL)
        playOrPauseCenterIv!!.visibility = View.GONE
        playControlLl!!.clearAnimation()
        playControlLl!!.animate().translationY(playControlLl!!.height.toFloat()).setDuration(500).start()
    }


    /**
     * 显示控制条
     */
    fun showControLl() {
        isShowControl = true
        handler.sendEmptyMessageDelayed(HIDE_CONTROL, 3000)
        playOrPauseCenterIv!!.visibility = View.VISIBLE
        playControlLl!!.clearAnimation()
        playControlLl!!.animate().translationY(0f).setDuration(500).start()
    }

    /**
     * 设置播放地址
     *
     * @param path
     */
    fun setVideoPath(path: String) {
        videoView!!.setVideoPath(path)
    }

    /**
     * 开始播放
     */
    fun start() {
        isPause = false
        videoView!!.start()
        showControLl()
    }

    /**
     * 暂停
     */
    fun pause() {
        isPause = true
        handler.removeMessages(UPDATE_POSITION)
        videoView!!.pause()
        playOrPauseCenterIv!!.setImageResource(R.mipmap.ic_play_circle_outline_white_48dp)
        playOrPauseIv!!.setImageResource(R.mipmap.ic_play_circle_outline_white_24dp)
    }

    /**
     * 继续
     */
    fun resume() {
        isPause = false
        handler.sendEmptyMessageDelayed(UPDATE_POSITION, 500)
        videoView!!.start()
        playOrPauseCenterIv!!.setImageResource(R.mipmap.ic_pause_circle_outline_white_48dp)
        playOrPauseIv!!.setImageResource(R.mipmap.ic_pause_circle_outline_white_24dp)
    }

    fun seekTo(position: Int) {
        videoView!!.seekTo(position)
    }

    /**
     * 停止
     */
    fun stop() {
        initSetting()
        handler.removeCallbacksAndMessages(null)
        videoView!!.stopPlayback()
    }

    private fun setFullScreen(fullScreen: Boolean) {
        if (context != null && context is AppCompatActivity) {
            val supportActionBar = (context as AppCompatActivity).supportActionBar
            if (supportActionBar != null) {
                if (fullScreen) {
                    supportActionBar.hide()
                } else {
                    supportActionBar.show()
                }
            }
            //            WindowManager.LayoutParams attrs = ((Activity) getContext()).getWindow().getAttributes();
            //            if (fullScreen) {
            //                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            //                ((Activity) getContext()).getWindow().setAttributes(attrs);
            //                ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            //            } else {
            //                attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //                ((Activity) getContext()).getWindow().setAttributes(attrs);
            //                ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            //            }
        }
    }

    interface IFullScreenListener {
        fun onClickFull(isFull: Boolean)
    }

    companion object {

        private val SHOW_CONTROL = 0x0001

        private val HIDE_CONTROL = 0x0002

        private val UPDATE_POSITION = 0x0003
    }
}
