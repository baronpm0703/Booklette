package com.example.booklette.model

import com.example.booklette.model.BookObject
import com.google.firebase.Timestamp
import com.google.type.Date
import java.time.LocalDate

class MyShopBookObject {
    var id: String
    var name: String
    var genre: String
    var author: String
    var shopPrice: Long
    var discount: Float
    var remain: Long
    var sold: Long
    var status: String
    var image: String
    var releaseDate: Timestamp
    var description: String
    var type: String

    constructor(
        id: String, 
        name: String,
        genre: String,
        author: String,
        image: String,
        shopPrice: Long,
        discount: Float,
        remain: Long,
        sold: Long,
        status: String,
        releaseDate: Timestamp,
        description: String,
        type: String
    ) {
        this.id = id
        this.name = name
        this.genre = genre
        this.author = author
        this.shopPrice = shopPrice
        this.discount = discount
        this.remain = remain
        this.sold = sold
        this.status = status
        this.image = image
        this.releaseDate = releaseDate
        this.description = description
        this.type = type
    }
}