package com.wardabbass.flickergallery.models

import com.google.gson.annotations.SerializedName

data class FlickerResponse(
    @SerializedName("photos") val photos: Photos = Photos(),
    @SerializedName("stat") val stat: String = ""
)