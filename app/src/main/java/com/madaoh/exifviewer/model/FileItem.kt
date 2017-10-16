package com.madaoh.exifviewer.model

/**
 * Created by tianching on 2017/10/16.
 */

data class FileItem(val path: String, val name: String, val size: Long, val date: String, val type: Type) : Comparable<FileItem> {

    override fun compareTo(other: FileItem): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    enum class Type(code: Int) {
        Image(0), Video(1);
    }
}
