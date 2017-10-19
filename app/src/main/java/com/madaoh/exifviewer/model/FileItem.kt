package com.madaoh.exifviewer.model

/**
 * Created by tianching on 2017/10/16.
 */
class FileItem() {

    var path: String? = null
    var name: String? = null
    var size: Long? = null
    var createTime: String? = null
    var make: String? = null
    var model: String? = null
    var width: Int? = null
    var height: Int? = null
    var longitude: Float? = null
    var latitude: Float? = null
    var longitudeRef: String? = null
    var latitudeRef: String? = null
    var orientation: Int? = null

    constructor(
            path: String?,
            name: String?,
            size: Long?,
            createTime: String?,
            make: String?,
            model: String?,
            width: Int?,
            height: Int?,
            longitude: Float?,
            latitude: Float?,
            longitudeRef: String?,
            latitudeRef: String?,
            orientation: Int?
    ) : this() {
        this.path = path
        this.name = name
        this.size = size
        this.createTime = createTime
        this.make = make
        this.model = model
        this.width = width
        this.height = height
        this.longitude = longitude
        this.latitude = latitude
        this.longitudeRef = longitudeRef
        this.latitudeRef = latitudeRef
        this.orientation = orientation
    }
}
