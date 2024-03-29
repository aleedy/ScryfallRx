package com.glutenmage.scryfallrx.models

import com.google.gson.annotations.SerializedName

data class CardModel(val id: String,
                     @SerializedName("image_uris") val imageUris: ImageUriModel)

data class ImageUriModel(val large: String)