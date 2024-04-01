package com.example.booklette

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