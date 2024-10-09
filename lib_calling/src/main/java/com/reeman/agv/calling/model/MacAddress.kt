package com.reeman.agv.calling.model

import android.os.Parcel
import android.os.Parcelable

class MacAddress(val macAddress: String?) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(macAddress)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MacAddress> {
        override fun createFromParcel(parcel: Parcel): MacAddress {
            return MacAddress(parcel)
        }

        override fun newArray(size: Int): Array<MacAddress?> {
            return arrayOfNulls(size)
        }
    }
}
