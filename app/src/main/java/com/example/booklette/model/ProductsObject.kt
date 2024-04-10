package com.example.booklette.model

import java.util.Date

class ProductsObject(
    var bookID: String,
    var name: String,
    var genre: String,
    var author: String,
    var releaseDate: Date,
    var image: String,
    var price: Float,
    var rating: Float,
    var typeCover: String
) {
    var quantitySale = 0

}