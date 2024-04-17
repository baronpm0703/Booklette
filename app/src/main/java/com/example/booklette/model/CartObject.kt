package com.example.booklette.model

import android.os.Parcel
import android.os.Parcelable

data class CartObject(
    val bookID: String?,
    val storeID: String?,
    val storeName: String?,
    val bookCover: String?,
    val bookName: String?,
    val author: String?,
    val price: Float,
    var bookQuantity: Int,
    var voucherShopDiscount: Float,
    var discountList: ArrayList<VoucherObject> = ArrayList(),
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readFloat(),
        ArrayList<VoucherObject>().apply {
            parcel.readList(this, VoucherObject::class.java.classLoader)
        }

        )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(bookID)
        parcel.writeString(storeID)
        parcel.writeString(storeName)
        parcel.writeString(bookCover)
        parcel.writeString(bookName)
        parcel.writeString(author)
        parcel.writeFloat(price)
        parcel.writeInt(bookQuantity)
        parcel.writeFloat(voucherShopDiscount)
        parcel.writeList(discountList)

    }
    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CartObject> {
        override fun createFromParcel(parcel: Parcel): CartObject {
            return CartObject(parcel)
        }

        override fun newArray(size: Int): Array<CartObject?> {
            return arrayOfNulls(size)
        }
    }
}