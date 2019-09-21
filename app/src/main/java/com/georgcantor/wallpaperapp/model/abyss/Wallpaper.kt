package com.georgcantor.wallpaperapp.model.abyss

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Wallpaper {

    @SerializedName("id")
    @Expose
    var id: String? = null
    @SerializedName("width")
    @Expose
    var width: String? = null
    @SerializedName("height")
    @Expose
    var height: String? = null
    @SerializedName("file_type")
    @Expose
    var fileType: String? = null
    @SerializedName("file_size")
    @Expose
    var fileSize: String? = null
    @SerializedName("url_image")
    @Expose
    var urlImage: String? = null
    @SerializedName("url_thumb")
    @Expose
    var urlThumb: String? = null
    @SerializedName("url_page")
    @Expose
    var urlPage: String? = null

}