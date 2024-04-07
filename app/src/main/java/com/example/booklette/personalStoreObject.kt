package com.example.booklette

class personalStoreObject {
    var id: String
    var followers: Int
    var following: Int
    var ratings: ArrayList<Any>
    var booksFromStoreList: ArrayList<Any>

    constructor(
        id: String,
        followers: Int,
        following: Int,
        ratings: ArrayList<Any>,
        booksFromStoreList: ArrayList<Any>
    ) {
        this.id = id
        this.followers = followers
        this.following = following
        this.ratings = ratings
        this.booksFromStoreList = booksFromStoreList
    }
}