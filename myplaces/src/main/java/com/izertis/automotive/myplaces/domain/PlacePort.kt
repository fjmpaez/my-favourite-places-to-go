package com.izertis.automotive.myplaces.domain

interface PlacePort {
    fun getPlaces(): List<Place>
}