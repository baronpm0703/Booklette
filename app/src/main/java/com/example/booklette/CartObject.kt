package com.example.booklette

import com.google.type.DateTime
import java.util.Date

class CartObject(
    var itemId: String,
    var storeId: String,
    var storeName: String,
    var bookCover:String,
    var bookName:String,
    var author:String,
    var price: Float,
) {
    var bookQuantity:Number = 0
}