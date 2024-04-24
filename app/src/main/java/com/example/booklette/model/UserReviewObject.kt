package com.example.booklette.model

class UserReviewObject {
    var userID: String
    var userName: String
    var avatar: String
    var ratings: Float
    var description: String

    constructor(
        userID: String,
        userName: String,
        avatar: String,
        ratings: Float,
        description: String
    ) {
        this.userID = userID
        this.userName = userName
        this.avatar = avatar
        this.ratings = ratings
        this.description = description
    }
}