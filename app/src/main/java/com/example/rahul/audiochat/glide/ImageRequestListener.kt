package com.example.rahul.audiochat.glide

import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

/**
 * Created by rahul on 29/11/18.
 */
class ImageRequestListener(private val callback: Callback?=null) : RequestListener<Drawable> {

    interface Callback{
        fun onFailure(message:String?)
        fun onSuccess(dataSource: String)
    }

    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
        callback?.onFailure(e?.message)
        return false
    }

    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
        callback?.onSuccess(dataSource.toString())
        Log.d("failed","on resource ready")
        return true
    }
}