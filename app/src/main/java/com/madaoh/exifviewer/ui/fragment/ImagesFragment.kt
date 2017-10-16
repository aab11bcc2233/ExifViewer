package com.madaoh.exifviewer.ui.fragment

import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.madaoh.exifviewer.R
import com.madaoh.exifviewer.model.FileItem
import com.madaoh.exifviewer.ui.adapter.ImagesAdapter
import kotlinx.android.synthetic.main.fragment_images.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


/**
 * Created by tianching on 2017/10/16.
 */
class ImagesFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_images, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        doAsync {
            val photos = getPhotos()

            uiThread {
                recyclerView.adapter = ImagesAdapter(photos as List<FileItem>)
            }
        }

    }



    private fun getPhotos(): List<FileItem>? {
        val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val contentResolver = context.contentResolver
        val projection = arrayOf(MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DISPLAY_NAME, MediaStore.Images.ImageColumns.SIZE, MediaStore.Images.ImageColumns.DATE_ADDED)
        val cursor = contentResolver.query(imageUri, projection, null, null,
                MediaStore.Images.Media.DATE_ADDED + " desc") ?: return null

        var list = mutableListOf<FileItem>()

        if (cursor.count !== 0 && cursor.moveToFirst()) {
                while (cursor.moveToNext()) {
                    val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
                    val fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                    val size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE))
                    val date = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED))

                    val item = FileItem(path, fileName, size, date, FileItem.Type.Image)
                    list.add(item)
                }
        }

        cursor.close()
        return list
    }

}