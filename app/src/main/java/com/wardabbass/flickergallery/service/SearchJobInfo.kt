package com.wardabbass.flickergallery.service

import android.os.Parcel
import android.os.Parcelable

data class SearchJobInfo(var tag:String,var currentId:String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString()) {
    }

    override fun toString(): String {
        return "SearchJobInfo(tag='$tag', currentId='$currentId')"
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(tag)
        parcel.writeString(currentId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SearchJobInfo> {
        override fun createFromParcel(parcel: Parcel): SearchJobInfo {
            return SearchJobInfo(parcel)
        }

        override fun newArray(size: Int): Array<SearchJobInfo?> {
            return arrayOfNulls(size)
        }
    }
}