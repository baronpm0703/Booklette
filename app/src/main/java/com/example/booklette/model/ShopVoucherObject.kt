package com.example.booklette.model

import com.google.firebase.Timestamp

class ShopVoucherObject {
	var discountName: String
	var discountIntroduction: String
	var startDate: Timestamp
	var endDate: Timestamp
	var minimumOrder: Long
	var orderLimit: Long
	var percent: Long

	constructor(
		discountName: String,
		discountIntroduction: String,
		startDate: Timestamp,
		endDate: Timestamp,
		minimumOrder: Long,
		orderLimit: Long,
		percent: Long
	) {
		this.discountName = discountName
		this.discountIntroduction = discountIntroduction
		this.startDate = startDate
		this.endDate = endDate
		this.minimumOrder = minimumOrder
		this.orderLimit = orderLimit
		this.percent = percent
	}
}