package com.example.booklette.model

class UserWishListObject(var userID: String, var userName: String) {
    lateinit var wishList: ArrayList<BookObject>

}