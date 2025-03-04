package com.izertis.automotive.myplaces.domain

data class Place(
    val id: Int,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)
