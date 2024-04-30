package com.example.booklette.model

class UserReviewObject(
    var userID: String,
    var userName: String,
    var avatar: String,
    var ratings: Float,
    var description: String
) {
    var wishListObject: ArrayList<String> = ArrayList()
    var reviewPhotos: ArrayList<Photo> = ArrayList()
}