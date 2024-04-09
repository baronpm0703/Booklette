package com.example.booklette.model

import com.example.booklette.model.BookObject

class MyShopBookObject {
    var basicInfo: BookObject
    var shopPrice: Long

    constructor(
        basicInfo: BookObject,
        shopPrice: Long
    ) {
        this.basicInfo = basicInfo
        this.shopPrice = shopPrice
    }
}