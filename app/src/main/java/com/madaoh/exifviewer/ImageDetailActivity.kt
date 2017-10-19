package com.madaoh.exifviewer

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.text.format.DateFormat
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.view.Window
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager
import com.madaoh.StatusBarUtils
import com.madaoh.exifviewer.model.FileItem
import com.madaoh.exifviewer.model.ItemKey
import com.madaoh.exifviewer.ui.adapter.DetailAdapter
import com.madaoh.exifviewer.ui.adapter.ImagesAdapter
import kotlinx.android.synthetic.main.activity_image_detail.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class ImageDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_image_detail)
        StatusBarUtils.setStatusBarTransparent(this)

        imageList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        if (checkPermissionStorage()) {
            handlerIntent(intent)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        }

    }

    private fun checkPermissionStorage(): Boolean {
        return checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) && checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun checkPermission(permission: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, permission)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (checkPermissionStorage()) {
            handlerIntent(intent)
        } else {
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.permission_grant_faild))
                    .setMessage(getString(R.string.why_need_permissions))
                    .setNegativeButton(getString(R.string.go_setting), DialogInterface.OnClickListener { dialogInterface, i -> toSetting(400) })
                    .setOnDismissListener {
                        finish()
                    }
                    .show()
        }
    }


    private fun handlerIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND, Intent.ACTION_VIEW -> if (intent?.type.startsWith("image/")) handleImage(intent)
            Intent.ACTION_SEND_MULTIPLE -> if (intent?.type.startsWith("image/")) handleImages(intent)
            else -> Log.d(javaClass.simpleName, "not have intent")
        }
    }

    private fun handleImages(intent: Intent) {
        var imageUris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)

        if (imageUris.isNotEmpty()) {
            textTitle.visibility = View.VISIBLE

            doAsync {
                val list = imageUris.map {
                    getPhotos(it, contentResolver)!![0]
                }.toList()

                var details: List<List<Map<String, String>>>? = null

                if (list!!.isNotEmpty()) {
                    details = toDetails(list!!)
                }

                uiThread {
                    imageList.adapter = ImagesAdapter(list)
                    if (details != null) {
                        recyclerView.adapter = DetailAdapter(list[0], details!![0])

                        imageList.addOnPageChangedListener(object: RecyclerViewPager.OnPageChangedListener {
                            override fun OnPageChanged(p0: Int, p1: Int) {
                                if (recyclerView.adapter is DetailAdapter) {
                                    (recyclerView.adapter as DetailAdapter).details = details!![p1]
                                    recyclerView.adapter.notifyDataSetChanged()
                                }
                            }
                        })
                    }
                }
            }

        } else {
            setLightStatusBar()
            alphaAnimateStart()
        }
    }

    private fun setLightStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun handleImage(intent: Intent) {
        var imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)

        if (imageUri == null) {
            imageUri = intent.data
        }

        if (imageUri != null) {
            textTitle.visibility = View.VISIBLE
            doAsync {
                val list = getPhotos(imageUri, contentResolver)

                var details: List<List<Map<String, String>>>? = null

                if (list!!.isNotEmpty()) {
                    details = toDetails(list!!)
                }

                uiThread {
                    imageList.adapter = ImagesAdapter(list!!)

                    if (details != null) {
                        recyclerView.adapter = DetailAdapter(list[0], details!![0])
                    }

                }
            }

        } else {
            setLightStatusBar()
            alphaAnimateStart()
        }
    }

    private fun getPhotos(imageUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentResolver: ContentResolver): List<FileItem>? {
        val projection = arrayOf(MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DISPLAY_NAME, MediaStore.Images.ImageColumns.SIZE, MediaStore.Images.ImageColumns.DATE_ADDED)
        val cursor = contentResolver.query(imageUri, projection, null, null,
                null) ?: return null

        var list = arrayListOf<FileItem>()

        try {
            if (cursor.count !== 0 && cursor.moveToFirst()) {
                do {
                    var path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
                    val fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                    val size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE))
                    val date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED))

                    if (TextUtils.isEmpty(path)) {
                        path = UriUtils.getPath(applicationContext, imageUri)
                    }

                    ld("uri 文件路径: $path")
                    ld("uri 文件名称: $fileName")
                    ld("uri 文件大小: " + Formatter.formatFileSize(applicationContext, size))
                    ld("uri 文件日期: " + DateFormat.format("yyyy/MM/dd HH:mm:ss", date * 1000))

                    var exif: ExifInterface? = null
                    try {
                        exif = ExifInterface(path)
                    } catch(e: Exception) {
                        continue
                    }

                    var item = FileItem()

                    item.path = path
                    item.name = fileName
                    item.size = size

                    val make = exif.getAttribute(ExifInterface.TAG_MAKE)
                    ld("设备制造商: $make")
                    item.make = make

                    val model = exif.getAttribute(ExifInterface.TAG_MODEL)
                    ld("设备型号: $model")
                    item.model = model

                    val width = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)
                    ld("图片宽: $width")
                    item.width = width.toInt()

                    val length = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)
                    ld("图片长: $length")
                    item.height = length.toInt()

                    val createTime = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    ld("图片创建时间: $createTime")
                    item.createTime = createTime

                    val time = exif.getAttribute(ExifInterface.TAG_DATETIME)
                    ld("图片时间: $time")

                    val digitizedTime = exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED)
                    ld("图片数字化时间: $digitizedTime")

                    val longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
                    ld("图片经度 度分秒表示: $longitude")

                    val latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
                    ld("图片纬度 度分秒表示: $latitude")

                    var latLng = FloatArray(2)
                    exif.getLatLong(latLng)
                    ld("图片经度 float 表示: ${latLng[1]}")
                    ld("图片纬度 float 表示: ${latLng[0]}")


                    val longitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
                    ld("图片经度参考: $longitudeRef")

                    val latitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
                    ld("图片纬度参考: $latitudeRef")

                    if (longitude != null) {
                        item.longitude = latLng[1]
                        item.latitude = latLng[0]
                        item.longitudeRef = longitudeRef
                        item.latitudeRef = latitudeRef
                    }

                    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    val orientationStr = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> {
                            null
                        }
                    }

                    item.orientation = orientationStr


                    ld("图片方向: $orientationStr")

                    list.add(item)
                } while (cursor.moveToNext())
            }
        } finally {
            cursor.close()
        }

        return list
    }

    private fun ld(msg: String) {
        Log.d("MainActivity", msg)
    }

    private fun toDetails(list: List<FileItem>): List<List<Map<String, String>>> {

        var l = arrayListOf<List<Map<String, String>>>()

        list.forEach {
            var listMap = ArrayList<Map<String, String>>()

            if (it.path != null) {
                addMap(listMap, ItemKey.PATH, it.path!!)
            }

            if (it.name != null) {
                addMap(listMap, ItemKey.NAME, it.name!!)
            }

            if (it.size != null) {
                addMap(listMap, ItemKey.SIZE, Formatter.formatFileSize(applicationContext, it.size!!))
            }

            if (it.createTime != null) {
                addMap(listMap, ItemKey.CREATE_TIME, it.createTime!!)
            }

            if (it.make != null) {
                addMap(listMap, ItemKey.MAKE, it.make!!)
            }

            if (it.model != null) {
                addMap(listMap, ItemKey.MODEL, it.model!!)
            }

            if (it.width != null) {
                addMap(listMap, ItemKey.RESOLUTION, "${it.width} x ${it.height}")
            }

            if (it.longitude != null) {
                addMap(listMap, ItemKey.LONGITUDE_LATITUDE, "${it.longitude} ${it.longitudeRef}, ${it.latitude} ${it.latitudeRef}")
            }

            if (it.orientation != null) {
                addMap(listMap, ItemKey.ORIENTATION, "${it.orientation} °")
            }

            l.add(listMap)
        }

        return l
    }

    private fun addMap(list: ArrayList<Map<String, String>>, k: String, v: String) {
        var map = mutableMapOf<String, String>()
        map.put(k, v)
        list.add(map)
    }


    private fun toSetting(requestCode: Int) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", getPackageName(), null)
        intent.data = uri
        startActivityForResult(intent, requestCode)
    }

    private fun alphaAnimateStart() {
        textEmpty.animate()
                .alpha(1f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        super.onAnimationStart(animation)
                        textEmpty.visibility = View.VISIBLE
                    }
                })
                .start()
    }
}
