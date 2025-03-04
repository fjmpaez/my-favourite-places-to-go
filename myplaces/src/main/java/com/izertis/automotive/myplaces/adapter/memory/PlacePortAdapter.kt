package com.izertis.automotive.myplaces.adapter.memory

import com.izertis.automotive.myplaces.domain.Place
import com.izertis.automotive.myplaces.domain.PlacePort


val PLACES = listOf(
    Place(1, "Madrid", "Capital de España", 40.4167, -3.70325),
    Place(2, "Villar de Huergo", "Pueblo de Asturias", 43.3667, -5.23333),
    Place(3, "Sabiote", "Pueblo de Jaén", 38.1167, -3.28333),
)

class PlacePortAdapter : PlacePort {
    override fun getPlaces(): List<Place> {
        return PLACES
    }
}
