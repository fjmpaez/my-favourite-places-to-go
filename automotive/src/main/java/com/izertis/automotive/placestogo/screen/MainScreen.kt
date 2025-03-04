package com.izertis.automotive.placestogo.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.HandlerThread
import android.text.Spannable
import android.text.SpannableString
import androidx.car.app.CarAppPermission
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.core.location.LocationListenerCompat
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.izertis.automotive.myplaces.domain.PortFactory

private const val LOCATION_UPDATE_MIN_INTERVAL_MILLIS = 1000
private const val LOCATION_UPDATE_MIN_DISTANCE_METER = 1

class MainScreen(carContext: CarContext) : Screen(carContext) {

    private val placePort by lazy { PortFactory.instance.createPlacePort() }
    private var currentLocation: Location? = null

    private val locationListener = LocationListenerCompat { location ->
        currentLocation = location
        invalidate()
    }
    private val locationUpdateHandlerThread = HandlerThread("LocationThread").apply { start() }

    init {
        requestPermissions()
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                startLocationUpdates()
            }

            override fun onPause(owner: LifecycleOwner) {
                stopLocationUpdates()
            }
        })
    }

    override fun onGetTemplate(): Template {
        val itemListBuilder = ItemList.Builder()
        placePort.getPlaces().forEach { place ->
            val distanceInKilometers = calculateDistanceFromCurrentLocation(place)
            itemListBuilder.addItem(
                createRow(place, distanceInKilometers)
            )
        }

        return PlaceListMapTemplate.Builder().setItemList(itemListBuilder.build())
            .setTitle("My favourite places in the world")
            .build()
    }

    private fun createRow(
        place: com.izertis.automotive.myplaces.domain.Place,
        distanceInKilometers: Double
    ) = Row.Builder()
        .setTitle(place.name)
        .addText(
            SpannableString(" ").apply {
                setSpan(
                    DistanceSpan.create(
                        Distance.create(
                            distanceInKilometers,
                            Distance.UNIT_KILOMETERS
                        )
                    ), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
        )
        .setOnClickListener {
            carContext.startCarApp(toNavigationIntent(place))
        }
        .setMetadata(
            createMetadata(place)
        )
        .build()

    private fun createMetadata(place: com.izertis.automotive.myplaces.domain.Place) =
        Metadata.Builder()
            .setPlace(
                Place.Builder(CarLocation.create(place.latitude, place.longitude))
                    .setMarker(
                        PlaceMarker.Builder().setColor(
                            CarColor.DEFAULT
                        ).setLabel(place.id.toString())
                            .build()
                    )
                    .build()
            )
            .build()

    private fun toNavigationIntent(place: com.izertis.automotive.myplaces.domain.Place): Intent {
        val uri = "geo:${place.latitude},${place.longitude}".toUri()
        return Intent(CarContext.ACTION_NAVIGATE, uri)
    }

    private fun calculateDistanceFromCurrentLocation(place: com.izertis.automotive.myplaces.domain.Place): Double =
        (currentLocation?.let { currentLocation ->
            val locationPlace = Location("").apply {
                latitude = place.latitude
                longitude = place.longitude
            }
            currentLocation.distanceTo(locationPlace) / 1000
        } ?: 0.0).toDouble()

    private fun requestPermissions() {
        val permissions = carContext.packageManager.getPackageInfo(
            carContext.packageName,
            PackageManager.GET_PERMISSIONS
        ).requestedPermissions?.filterNot {
            it.startsWith("androidx.car.app") || hasPermission(it)
        } ?: emptyList()

        if (permissions.isNotEmpty()) {
            carContext.requestPermissions(permissions) { _, _ -> }
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return try {
            CarAppPermission.checkHasPermission(carContext, permission)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    private fun startLocationUpdates() {
        val locationManager = carContext.getSystemService(LocationManager::class.java)
        if (carContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            carContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.FUSED_PROVIDER,
                LOCATION_UPDATE_MIN_INTERVAL_MILLIS.toLong(),
                LOCATION_UPDATE_MIN_DISTANCE_METER.toFloat(),
                locationListener,
                locationUpdateHandlerThread.looper
            )
        }
    }

    private fun stopLocationUpdates() {
        val locationManager = carContext.getSystemService(LocationManager::class.java)
        locationManager.removeUpdates(locationListener)
    }
}

