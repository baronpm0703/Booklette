import android.os.Parcel
import android.os.Parcelable

data class ShipAddressObject(
    val receiverName: String?,
    val receiverPhone: String?,
    val province: String?,
    val city: String?,
    val ward: String?,
    val addressNumber: String?,
    val shipLabel: String?,
    var isDefault: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(receiverName)
        parcel.writeString(receiverPhone)
        parcel.writeString(province)
        parcel.writeString(city)
        parcel.writeString(ward)
        parcel.writeString(addressNumber)
        parcel.writeString(shipLabel)
        parcel.writeByte(if (isDefault) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }


    companion object CREATOR : Parcelable.Creator<ShipAddressObject> {
        override fun createFromParcel(parcel: Parcel): ShipAddressObject {
            return ShipAddressObject(parcel)
        }

        override fun newArray(size: Int): Array<ShipAddressObject?> {
            return arrayOfNulls(size)
        }
    }
}
