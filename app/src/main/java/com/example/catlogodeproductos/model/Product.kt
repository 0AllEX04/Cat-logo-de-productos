package com.example.catlogodeproductos.model

data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val rating: Double,
    val stock: Int,
    val thumbnail: String
)