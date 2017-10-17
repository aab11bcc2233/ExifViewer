package com.madaoh.exifviewer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.madaoh.exifviewer.ui.fragment.ImagesFragment
import okhttp3.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.IOException
import okio.Buffer
import java.nio.charset.Charset


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, ImagesFragment())
                .commit()

    }

}
