package com.example.booklette.model

import com.google.firebase.Timestamp

class DiscountProgramObject {
	var productID: String
	var discountName: String
	var discountIntroduction: String
	var startDate: Timestamp
	var endDate: Timestamp
	var orderLimit: Long
	var percent: Long

	constructor(
		productID: String,
		discountName: String,
		discountIntroduction: String,
		startDate: Timestamp,
		endDate: Timestamp,
		orderLimit: Long,
		percent: Long
	) {
		this.productID = productID
		this.discountName = discountName
		this.discountIntroduction = discountIntroduction
		this.startDate = startDate
		this.endDate = endDate
		this.orderLimit = orderLimit
		this.percent = percent
	}
}