package com.georgcantor.wallpaperapp.repository

import android.content.Context
import android.os.Build
import com.georgcantor.wallpaperapp.BuildConfig
import com.georgcantor.wallpaperapp.R
import com.georgcantor.wallpaperapp.model.data.CommonPic
import com.georgcantor.wallpaperapp.model.remote.ApiService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ApiRepository(
    private val context: Context,
    private val apiService: ApiService
) {

    fun getPixabayPictures(request: String, index: Int): Observable<ArrayList<CommonPic>> {
        val pictures = ArrayList<CommonPic>()

        return apiService.getPixabayPictures(request, index)
            .flatMap { pic ->
                Observable.fromCallable {
                    pic.hits.map { hit ->
                        pictures.add(
                            CommonPic(
                                hit.webformatURL,
                                hit.imageWidth,
                                hit.imageHeight,
                                hit.likes,
                                hit.favorites,
                                hit.tags,
                                hit.downloads,
                                hit.imageURL,
                                hit.webformatURL,
                                hit.user,
                                hit.id,
                                hit.userImageURL
                            )
                        )
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        pictures.removeIf {
                            it.id == 158703 || it.id == 158704
                        }
                    }
                    pictures.shuffle()
                    pictures
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getUnsplashPictures(query: String, page: Int): Observable<ArrayList<CommonPic>> {
        val pictures = ArrayList<CommonPic>()

        return apiService.getUnsplashPictures(
            BuildConfig.UNSPLASH_URL,
            query,
            page
        )
            .flatMap { response ->
                Observable.fromCallable {
                    response.results?.map {
                        it.urls.takeUnless { urls ->
                            pictures.add(
                                CommonPic(
                                    urls?.small,
                                    it.width ?: 0,
                                    it.height ?: 0,
                                    it.likes ?: 365,
                                    542,
                                    "",
                                    5923,
                                    urls?.full,
                                    urls?.regular,
                                    context.getString(R.string.user_unsplash),
                                    it.hashCode(),
                                    urls?.thumb
                                )
                            )
                        }
                    }
                    pictures.shuffle()
                    pictures
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getAbyssPictures(query: String, page: Int): Observable<ArrayList<CommonPic>> {
        val pictures = ArrayList<CommonPic>()

        return apiService.getAbyssPictures(
            BuildConfig.ABYSS_URL,
            query,
            page
        )
            .flatMap { response ->
                Observable.fromCallable {
                    response.wallpapers?.map {
                        pictures.add(
                            CommonPic(
                                it.urlThumb,
                                it.width?.toInt() ?: 0,
                                it.height?.toInt() ?: 0,
                                481,
                                542,
                                "",
                                4245,
                                it.urlImage,
                                it.urlImage,
                                context.getString(R.string.user_abyss),
                                it.id?.toInt() ?: it.hashCode(),
                                it.urlThumb
                            )
                        )
                    }
                    pictures.shuffle()
                    pictures
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getPexelsPictures(query: String, page: Int): Observable<ArrayList<CommonPic>> {
        val pictures = ArrayList<CommonPic>()

        return apiService.getPexelsPictures(BuildConfig.PEXELS_URL, query, 15, page)
            .flatMap { response ->
                Observable.fromCallable {
                    response.photos?.map {
                        pictures.add(
                            CommonPic(
                                it.src?.medium,
                                it.width,
                                it.height,
                                217,
                                328,
                                "",
                                3846,
                                it.src?.original,
                                it.src?.large,
                                it.photographer,
                                it.id,
                                it.src?.small
                            )
                        )
                    }
                    pictures.shuffle()
                    pictures
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getCategories(request: String): Observable<String> {
        return apiService.getPixabayPictures(request, 1)
            .flatMap {
                Observable.fromCallable {
                    it.hits.first().webformatURL
                }
            }
    }

}