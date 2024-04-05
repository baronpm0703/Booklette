package com.example.booklette

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