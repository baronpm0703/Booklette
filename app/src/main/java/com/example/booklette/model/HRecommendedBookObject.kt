package com.example.booklette.model

import com.example.booklette.model.BookObject

class HRecommendedBookObject {
    var id: String
    var img: String
    var rating: Float
    var ratingCnt: Long
    var name: String
    var price: Long

    constructor(
        id: String,
        img: String,
        rating: Float,
        ratingCnt: Long,
        name: String,
        price: Long
    ) {
        this.id = id
        this.img = img
        this.rating = rating
        this.ratingCnt = ratingCnt
        this.name = name
        this.price = price
    }
}