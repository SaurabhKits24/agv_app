package com.reeman.agv.calling.model

import com.google.gson.annotations.SerializedName

import android.os.Parcel
import android.os.Parcelable

class PhoneHeartBeat() : Parcelable {
    @SerializedName("macAddress")
    var macAddress: String? = null

    constructor(parcel: Parcel) : this() {
        macAddress = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(macAddress)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PhoneHeartBeat> {
        override fun createFromParcel(parcel: Parcel): PhoneHeartBeat {
            return PhoneHeartBeat(parcel)
        }

        override fun newArray(size: Int): Array<PhoneHeartBeat?> {
            return arrayOfNulls(size)
        }
    }
}
