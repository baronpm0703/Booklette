package com.example.booklette

import com.google.type.DateTime
import java.util.Date

class ProductsObject(
    var bookID: String,
    var name: String,
    var genre: String,
    var author: String,
    var releaseDate: Date,
    var image: String,
    var price: Long,
    var rating: Float
) {
    var quantitySale = 0

}