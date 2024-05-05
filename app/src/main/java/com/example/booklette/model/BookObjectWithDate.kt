package com.example.booklette.model

import java.util.Date

class BookObjectWithDate(
    var bookID: String,
    var name: String,
    var genre: String,
    var author: String,
    var releaseDate: Date,
    var image: String,
    var price: Float,
    var description: String
) {
    var index = 0
}