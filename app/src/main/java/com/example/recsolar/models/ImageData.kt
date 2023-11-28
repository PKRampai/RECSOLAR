package com.example.recsolar.models

import android.os.Parcel
import android.os.Parcelable

data class ImageData(val byteArray: ByteArray) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.createByteArray() ?: byteArrayOf())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByteArray(byteArray)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageData> {
        override fun createFromParcel(parcel: Parcel): ImageData {
            return ImageData(parcel)
        }

        override fun newArray(size: Int): Array<ImageData?> {
            return arrayOfNulls(size)
        }
    }
}
