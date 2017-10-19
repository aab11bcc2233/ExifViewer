package com.madaoh.exifviewer.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.madaoh.exifviewer.R
import com.madaoh.exifviewer.model.FileItem
import org.jetbrains.anko.find

/**
 * Created by tianching on 2017/10/16.
 */

class ImagesAdapter(val images: List<FileItem>) : RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {

        Glide.with(holder!!.itemView.context)
                .load(images[position].path)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .into(holder!!.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        val size = images?.size
        return size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView? = null

        init {
            imageView = view.find<ImageView>(R.id.imageView)
        }
    }
}