package com.example.booklette.model

import com.google.firebase.Timestamp

class ManageShopNewBookObject {
	var name: String
	var genre: String
	var author: String
	var releaseDate: Timestamp
	var image: ArrayList<Photo>
	var price: Float
	var description: String
	var topBook: String
	var type: String
	var quantity: Long

	constructor(
		name: String,
		genre: String,
		author: String,
		releaseDate: Timestamp,
		image: ArrayList<Photo>,
		price: Float,
		description: String,
		topBook: String,
		type: String,
		quantity: Long
	) {
		this.name = name
		this.genre = genre
		this.author = author
		this.releaseDate = releaseDate
		this.image = image
		this.price = price
		this.description = description
		this.topBook = topBook
		this.type = type
		this.quantity = quantity
	}
}