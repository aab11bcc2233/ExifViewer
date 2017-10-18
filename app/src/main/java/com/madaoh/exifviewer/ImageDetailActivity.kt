package com.madaoh.exifviewer

import android.Manifest
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.format.DateFormat
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.view.Window
import com.madaoh.StatusBarUtils
import com.madaoh.exifviewer.model.FileItem
import com.madaoh.exifviewer.ui.adapter.ImagesAdapter
import kotlinx.android.synthetic.main.activity_image_detail.*

class ImageDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_image_detail)
        supportActionBar!!.hide()
        StatusBarUtils.setStatusBarTransparent(this)

        imageList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)

    }

    private fun checkPermissionStorage(): Boolean {
        return checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
            val list = imageUris.map {
                getPhotos(it, contentResolver)!![0]
            }.toList()

            imageList.adapter = ImagesAdapter(list)
        }
    }

    private fun handleImage(intent: Intent) {
        val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)

        if (imageUri != null) {
            textTitle.visibility = View.VISIBLE
            val list = getPhotos(imageUri, contentResolver)
            imageList.adapter = ImagesAdapter(list as List<FileItem>)
        }
    }

    private fun getPhotos(imageUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentResolver: ContentResolver): List<FileItem>? {
        val projection = arrayOf(MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DISPLAY_NAME, MediaStore.Images.ImageColumns.SIZE, MediaStore.Images.ImageColumns.DATE_ADDED)
        val cursor = contentResolver.query(imageUri, projection, null, null,
                null) ?: return null

        var list = mutableListOf<FileItem>()

        if (cursor.count !== 0 && cursor.moveToFirst()) {
            do {
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
                val fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                val size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE))
                val date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED))

                ld("uri 文件路径: $path")
                ld("uri 文件名称: $fileName")
                ld("uri 文件大小: " + Formatter.formatFileSize(applicationContext, size))
                ld("uri 文件日期: " + DateFormat.format("yyyy/MM/dd HH:mm:ss", date * 1000))

                val exif = ExifInterface(path)

                val make = exif.getAttribute(ExifInterface.TAG_MAKE)
                ld("设备制造商: $make")

                val model = exif.getAttribute(ExifInterface.TAG_MODEL)
                ld("设备型号: $model")

                val width = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)
                ld("图片宽: $width")

                val length = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)
                ld("图片长: $length")

                val createTime = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                ld("图片创建时间: $createTime")

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

                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val orientationStr = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> {
                        null
                    }
                }


                ld("图片方向: $orientationStr")

                val item = FileItem(path, fileName, size, date, FileItem.Type.Image)
                list.add(item)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    private fun ld(msg: String) {
        Log.d("MainActivity", msg)
    }


    private fun toSetting(requestCode: Int) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", getPackageName(), null)
        intent.data = uri
        startActivityForResult(intent, requestCode)
    }
}
