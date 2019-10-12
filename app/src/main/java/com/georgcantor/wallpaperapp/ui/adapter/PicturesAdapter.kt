package com.georgcantor.wallpaperapp.ui.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.georgcantor.wallpaperapp.R
import com.georgcantor.wallpaperapp.model.data.CommonPic
import com.georgcantor.wallpaperapp.ui.DetailsActivity
import com.georgcantor.wallpaperapp.ui.adapter.holder.PictureViewHolder
import com.georgcantor.wallpaperapp.util.longToast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit

class PicturesAdapter(private val context: Context) : RecyclerView.Adapter<PictureViewHolder>() {

    private val commonPics: MutableList<CommonPic>?

    init {
        this.commonPics = ArrayList()
    }

    fun setPicList(strings: MutableList<CommonPic>) {
        this.commonPics?.addAll(strings)
        notifyDataSetChanged()
    }

    fun clearPicList() {
        val size = commonPics?.size
        commonPics?.clear()
        size?.let { notifyItemRangeRemoved(0, it) }
    }

    @SuppressLint("CheckResult")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.picture_item, null)
        val wallpViewHolder = PictureViewHolder(itemView)

        val publishSubject = PublishSubject.create<Int>()
        publishSubject
            .throttleFirst(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val activity = context as Activity
                val position = wallpViewHolder.adapterPosition
                val intent = Intent(context, DetailsActivity::class.java)
                try {
                    intent.putExtra(DetailsActivity.EXTRA_PIC, commonPics?.get(position)?.heght?.let { height ->
                        CommonPic(
                                url = commonPics[position].url,
                                width = commonPics[position].width,
                                heght = height,
                                likes = commonPics[position].likes,
                                favorites = commonPics[position].favorites,
                                tags = commonPics[position].tags,
                                downloads = commonPics[position].downloads,
                                imageURL = commonPics[position].imageURL,
                                fullHDURL = commonPics[position].fullHDURL,
                                user = commonPics[position].user,
                                userImageURL = commonPics[position].userImageURL
                        )
                    })
                } catch (e: ArrayIndexOutOfBoundsException) {
                    context.longToast(context.getString(R.string.something_went_wrong))
                }
                context.startActivity(intent)
                activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
            }

        itemView.setOnClickListener {
            publishSubject.onNext(0)
        }

        return wallpViewHolder
    }

    override fun onBindViewHolder(holder: PictureViewHolder, position: Int) {
        this.commonPics.let {
            val layoutParams = holder.imageView.layoutParams as RelativeLayout.LayoutParams
            val height = it?.get(position)?.heght?.toFloat()
            val width = it?.get(position)?.width?.toFloat()
            val ratio = width?.let { widthFloat -> height?.div(widthFloat) } ?: 0F
            layoutParams.height = (layoutParams.width * ratio).toInt()
            holder.imageView.layoutParams = layoutParams
            holder.imageView.setRatio(ratio)

            Glide.with(context)
                .load(it?.get(position)?.url)
                .placeholder(R.drawable.plh)
                .thumbnail(0.1f)
                .into(holder.imageView)
        }
    }

    override fun getItemCount(): Int = commonPics?.size ?: 0

}