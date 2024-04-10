package com.example.booklette

import com.example.booklette.model.BookObject

class HRecommendedBookObject {
    var basicInfo: BookObject
    var rating: Float

    constructor(
        basicInfo: BookObject,
        rating: Float
    ) {
        this.basicInfo = basicInfo
        this.rating = rating
    }
}