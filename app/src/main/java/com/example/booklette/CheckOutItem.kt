package com.example.booklette

data class CheckoutItem(
    val shopName: String,
    val bookTitle: String,
    val bookOwner: String,
    val bookPrice: String,
    val tvCartItemCount: String,
    // Add other properties as needed
)