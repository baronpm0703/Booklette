package com.example.booklette

class BookObject {
    var bookID: String
    var name: String
    var genre: String
    var author: String
    var releaseDate: String
    var image: String
    var price: Float
    var description: String

    constructor(
        bookID: String,
        name: String,
        genre: String,
        author: String,
        releaseDate: String,
        image: String,
        price: Float,
        description: String
    ) {
        this.bookID = bookID
        this.name = name
        this.genre = genre
        this.author = author
        this.releaseDate = releaseDate
        this.image = image
        this.price = price
        this.description = description
    }
}