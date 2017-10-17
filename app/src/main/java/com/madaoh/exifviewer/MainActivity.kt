package com.madaoh.exifviewer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.madaoh.exifviewer.ui.fragment.ImagesFragment


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, ImagesFragment())
                .commit()

    }

}
