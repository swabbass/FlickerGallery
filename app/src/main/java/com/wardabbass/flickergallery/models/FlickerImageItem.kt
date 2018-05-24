package com.wardabbass.flickergallery.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class FlickerImageItem (@SerializedName("id") val id: String = "",
                             @SerializedName("owner") val owner: String = "",
                             @SerializedName("secret") val secret: String = "",
                             @SerializedName("server") val server: String = "",
                             @SerializedName("farm") val farm: Int = 0,
                             @SerializedName("title") val title: String = "",
                             @SerializedName("ispublic") val ispublic: Int = 0,
                             @SerializedName("isfriend") val isfriend: Int = 0,
                             @SerializedName("isfamily") val isfamily: Int = 0,
                             @SerializedName("url_s") val url: String = "",
                             @SerializedName("height_s") val heightS: String = "",
                             @SerializedName("width_s") val widthS: String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(owner)
        parcel.writeString(secret)
        parcel.writeString(server)
        parcel.writeInt(farm)
        parcel.writeString(title)
        parcel.writeInt(ispublic)
        parcel.writeInt(isfriend)
        parcel.writeInt(isfamily)
        parcel.writeString(url)
        parcel.writeString(heightS)
        parcel.writeString(widthS)
    }


    fun getWebUrl():String{
        return "https://www.flickr.com/photos/$owner/$id"
    }
    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FlickerImageItem> {
        override fun createFromParcel(parcel: Parcel): FlickerImageItem {
            return FlickerImageItem(parcel)
        }

        override fun newArray(size: Int): Array<FlickerImageItem?> {
            return arrayOfNulls(size)
        }
    }
}