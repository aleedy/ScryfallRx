package com.glutenmage.scryfallrx.models

import com.google.gson.annotations.SerializedName

data class CardModel(@SerializedName("image_uris") val imageUris: ImageUriModel)

data class ImageUriModel(val png: String)