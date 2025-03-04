package com.izertis.automotive.myplaces.domain

import com.izertis.automotive.myplaces.adapter.memory.PlacePortAdapter

class PortFactory {

    companion object {
        val instance = PortFactory()
    }

    fun createPlacePort(): PlacePort {
        return PlacePortAdapter()
    }
}