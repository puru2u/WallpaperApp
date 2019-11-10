package com.georgcantor.wallpaperapp.viewmodel

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.georgcantor.wallpaperapp.R
import com.georgcantor.wallpaperapp.model.data.CommonPic
import com.georgcantor.wallpaperapp.model.local.DatabaseHelper
import com.georgcantor.wallpaperapp.repository.ApiRepository
import com.georgcantor.wallpaperapp.util.getImageNameFromUrl
import com.georgcantor.wallpaperapp.util.shortToast
import com.georgcantor.wallpaperapp.util.showAnimation
import com.georgcantor.wallpaperapp.util.showSingleAnimation
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class DetailsViewModel(
        private val context: Context,
        private val db: DatabaseHelper,
        private val apiRepository: ApiRepository
) : ViewModel() {

    fun setFavoriteStatus(
            pic: CommonPic,
            menuItem: MenuItem,
            starAnimation: LottieAnimationView,
            unStarAnimation: LottieAnimationView
    ) {
        if (db.containFav(pic.url.toString())) {
            db.deleteFromFavorites(pic.url.toString())
            menuItem.setIcon(R.drawable.ic_star_border)
            unStarAnimation.showSingleAnimation(1.5F)
        } else {
            addToFavorites(pic.url.toString(), pic.imageURL.toString(), pic)
            menuItem.setIcon(R.drawable.ic_star_red_24dp)
            starAnimation.showSingleAnimation(1F)
        }
    }

    private fun addToFavorites(imageUrl: String, hdUrl: String, commonPic: CommonPic) {
        val gson = Gson()
        val toStoreObject = gson.toJson(commonPic)
        db.insertToFavorites(imageUrl, hdUrl, toStoreObject)
    }

    fun getSimilarImages(request: String, index: Int): Observable<ArrayList<CommonPic>> {
        return apiRepository.getPixabayPictures(request, index)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun downloadPicture(
            pic: CommonPic,
            tags: ArrayList<String>,
            animationView: LottieAnimationView
    ): Long {
        animationView.showAnimation()
        val uri = pic.imageURL
        val imageUri = Uri.parse(uri)
        val downloadReference: Long
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        var name = Environment.getExternalStorageDirectory().absolutePath
        name += "/YourDirectoryName/"

        val request = DownloadManager.Request(imageUri)
        try {
            request.setTitle(tags[0] + context.getString(R.string.down))
            request.setDescription(context.getString(R.string.down_wallpapers))
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                request.setDestinationInExternalPublicDir(
                        "/" + context.resources
                                .getString(R.string.app_name), pic.id.toString() + context.resources
                        .getString(R.string.jpg)
                )
            }
        } catch (e: IllegalStateException) {
        } catch (e: IndexOutOfBoundsException) {
        }
        downloadReference = downloadManager.enqueue(request)

        return downloadReference
    }

    fun downloadPictureQ(url: String, animationView: LottieAnimationView) {
        animationView.showAnimation()
        val name = url.getImageNameFromUrl()
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?

        val request = DownloadManager.Request(Uri.parse(url))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)

        downloadManager?.enqueue(request)
    }

    fun getBitmapAsync(pic: CommonPic): Observable<Bitmap?>? {
        return Observable.fromCallable {
            var result: Bitmap? = null
            try {
                result = Glide.with(context.applicationContext)
                    .asBitmap()
                    .load(pic.imageURL)
                    .submit()
                    .get()
            } catch (e: IOException) {
            }
            result
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getImageUri(bitmap: Bitmap): Observable<Uri> {
        return Observable.fromCallable {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(
                    context.contentResolver,
                    bitmap, "Title", null
            )
            Uri.parse(path)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun setBitmapAsync(bitmap: Bitmap, activity: Activity) {
        Single.fromCallable {
            WallpaperManager.getInstance(context)
                .setBitmap(bitmap)
        }
            .doOnSuccess {
                activity.runOnUiThread {
                    context.shortToast(context.getString(R.string.set_wall_complete))
                }
            }
            .onErrorReturn {
                activity.runOnUiThread {
                    context.shortToast(context.getString(R.string.something_went_wrong))
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun checkSavingPermission(permissionCheck: Int, activity: Activity) {
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            val requestCode = 102
            ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    requestCode
            )
        }
    }

}