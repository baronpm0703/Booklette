package com.example.booklette

import com.google.firebase.Timestamp

class VoucherObject {
    var discountFilter: String
    var discountID: String
    var discountName: String
    var discountType: Int
    var endDate: Timestamp
    var percent: Float
    var startDate: Timestamp

    constructor(
        discountFilter: String,
        discountID: String,
        discountName: String,
        discountType: Int,
        endDate: Timestamp,
        percent: Float,
        startDate: Timestamp
    ) {
        this.discountFilter = discountFilter
        this.discountID = discountID
        this.discountName = discountName
        this.discountType = discountType
        this.endDate = endDate
        this.percent = percent
        this.startDate = startDate
    }
}