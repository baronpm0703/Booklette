package com.example.booklette.model
import android.graphics.Bitmap

class Photo {
    var id: Int? = 0
    var image: Bitmap? = null
    var nameFile: String? = null
    var isSelected: Boolean = false
    companion object {
        var totalID : Int? = 0
        lateinit var photoGallery : ArrayList<Photo>
        fun updateTotalID(ID: Int) {
            totalID = ID
        }
    }
}