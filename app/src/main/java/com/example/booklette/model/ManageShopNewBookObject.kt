package com.example.booklette.model

class ManageShopNewBookObject {
	var bookID: String
	var name: String
	var genre: String
	var author: String
	var releaseDate: String
	var image: String
	var price: Float
	var description: String
	var topBook: String
	var type: String
	var quantity: Long

	constructor(
		bookID: String,
		name: String,
		genre: String,
		author: String,
		releaseDate: String,
		image: String,
		price: Float,
		description: String,
		topBook: String,
		type: String,
		quantity: Long
	) {
		this.bookID = bookID
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