import android.os.Parcel
import android.os.Parcelable

data class DetailInvoiceItem(
    val name: String, val quantity: Number, val price: Float, val totalSum: Float)
