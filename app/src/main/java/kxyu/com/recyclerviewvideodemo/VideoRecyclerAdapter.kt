package kxyu.com.recyclerviewvideodemo

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView

import com.bumptech.glide.Glide

import java.util.ArrayList

/**
 * Created by kxyu on 2019/2/25
 */

class VideoRecyclerAdapter(private var mList: MutableList<VideoBean>?) : RecyclerView.Adapter<VideoRecyclerAdapter.ViewHolder>() {

    private var play = 0
    private var listener: OnClickPlayListener? = null

    fun setListener(listener: OnClickPlayListener) {
        this.listener = listener
    }

    fun addVideoBean(videoBean: VideoBean?) {
        if (videoBean == null) return
        if (mList == null) {
            mList = ArrayList()
        }
        mList!!.add(videoBean)
        notifyDataSetChanged()
    }


    fun setPlay(play: Int) {
        this.play = play
        notifyDataSetChanged()
    }

    fun addAllVideoBean(list: List<VideoBean>?) {
        if (list == null) return
        if (mList == null) {
            mList = ArrayList()
        }
        mList!!.clear()
        mList!!.addAll(list)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val videoBean = mList!![position]
        Log.i("kxyu", videoBean.mImageId)
        Glide.with(holder.itemView.context).load(videoBean.mImageId).into(holder.mImageView)
        holder.mImageViewPlay!!.setOnClickListener{
            if(listener != null){
                listener!!.onPlayClick(holder.mCardView, videoBean.mVideoPath)
            }
        }

        if(play == position){
            listener!!.onPlayClick(holder.mCardView, videoBean.mVideoPath)
        }else{

        }
    }

    override fun getItemCount(): Int {
        return if (mList == null) 0 else mList!!.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var mCardView: CardView
        internal var mVideoRootFl: FrameLayout
        internal var mImageView: ImageView
        internal var mImageViewPlay: ImageView

        init {
            mCardView = itemView.findViewById<View>(R.id.item_cardview) as CardView
            mVideoRootFl = itemView.findViewById<View>(R.id.item_video_root_fl) as FrameLayout
            mImageView = itemView.findViewById<View>(R.id.item_imageview) as ImageView
            mImageViewPlay = itemView.findViewById<View>(R.id.item_image_play) as ImageView
        }
    }

    interface OnClickPlayListener {
        fun onPlayClick(view: View, videoPath: String)
        fun onClose(view: View, videoPath: String)
    }
}

