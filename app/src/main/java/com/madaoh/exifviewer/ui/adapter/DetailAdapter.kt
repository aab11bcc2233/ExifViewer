package com.madaoh.exifviewer.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.madaoh.exifviewer.R
import com.madaoh.exifviewer.model.FileItem
import org.jetbrains.anko.find

/**
 * Created by tianching on 2017/10/16.
 */

class DetailAdapter(var fileItem: FileItem, var details: List<Map<String, String>>) : RecyclerView.Adapter<DetailAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        for ((k, v) in details[position]){
            holder!!.textViewKey!!.text = k
            holder!!.textViewValue!!.text = v
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.detail_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return details.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textViewKey: TextView? = null
        var textViewValue: TextView? = null

        init {
            textViewKey = view.find(R.id.textViewKey)
            textViewValue = view.find(R.id.textViewValue)
        }
    }
}