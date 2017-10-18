package com.madaoh.exifviewer

import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setText()
    }

    private fun setText() {
        val str = getString(R.string.instructions_for_use)
        var span = SpannableStringBuilder(str)

        val album = getString(R.string.album)
        val albumIndex = str.indexOf(album)
        span.setSpan(StyleSpan(Typeface.BOLD), albumIndex, albumIndex + album.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)


        val share = getString(R.string.share)
        val shareIndex = str.indexOf(share)
        span.setSpan(StyleSpan(Typeface.BOLD), shareIndex, shareIndex + share.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        val file = getString(R.string.my_file)
        val fileIndex = str.indexOf(file)
        span.setSpan(StyleSpan(Typeface.BOLD), fileIndex, fileIndex + file.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        val open = getString(R.string.open)
        val openIndex = str.indexOf(open)
        span.setSpan(StyleSpan(Typeface.BOLD), openIndex, openIndex + open.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        textView.text = span
    }


}
