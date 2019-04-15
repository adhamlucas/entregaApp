package com.examp

import com.example.entregaapp.R

le.entregaapp

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.compat.ui.PlacePicker

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import java.io.IOException


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    private var lastSelectedMarker: Marker? = null


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
            }
        }

        createLocationRequest()

        //Barra de pesquisa
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            loadPlacePicker()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlacePicker.getPlace(this@MapsActivity, data)
                var addressText = place.name.toString()
                addressText += "\n" + place.address.toString()

                placeMarkerOnMap(place.latLng)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener (this)

        setUpMap()

        createMarkeInMap()

    }


    override fun onMarkerClick(marker: Marker?): Boolean {
        marker!!.zIndex += 1.0f

        lastSelectedMarker = marker
    }


    private fun setUpMap (){
        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)

            return
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient?.lastLocation!!.addOnSuccessListener (this) { location ->
            if(location!= null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
//                placeMarkerOnMap(currentLatLng)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))

            }
        }
    }



    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)


        val titleStr = getEndereco(location)
        markerOptions.title(titleStr)
//        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_local_shipping_black_24dp))

        mMap.addMarker(markerOptions)
    }

    private fun getEndereco(latLng: LatLng): String {
        // 1
        val geocoder = Geocoder(this)
        var enderecos: List<Address>? = null
        val endereco: Address?
        var textoEndereco = ""

        try {
            enderecos = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (null != enderecos) {

                endereco = enderecos[0]
                textoEndereco = endereco.getAddressLine(0)
            }

        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }


        return textoEndereco
    }

//    Adiconar localizao atual do usaurio em tempo real
    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }



    private fun loadPlacePicker() {
        val builder = PlacePicker.IntentBuilder()
//        startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST)
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
            Log.e("PlacePicker", e.message)
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
            Log.e("PlacePicker", e.message)
        }
    }

    //adicionar marcadores escolhidos no mapa
    private fun createMarkeInMap () {
        val oceanLatLng = LatLng(-3.092573, -60.018508) // Coordenadas do Ocean
        val tceLatLng = LatLng (-3.0875468, -60.005322) //Coordenadas do TCE AM
        Log.d("MapsActivity", tceLatLng.longitude.toString())
        placeMarkerOnMap(oceanLatLng)
        placeMarkerOnMap(tceLatLng)
    }

//
//    private fun addRoute (){
//        val path: MutableList<List<LatLng>> = ArrayList()
//        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=10.3181466,123.9029382&destination=10.311795,123.915864&key=<AIzaSyAcFHzCk-d11uSeNONtR38UFDOth9jvffc>"
//        val directionsRequest = object : StringRequest(Request.Method.GET, urlDirections, Response.Listener<String> {
//                response ->
//            val jsonResponse = JSONObject(response)
//            // Get routes
//            val routes = jsonResponse.getJSONArray("routes")
//            val legs = routes.getJSONObject(0).getJSONArray("legs")
//            val steps = legs.getJSONObject(0).getJSONArray("steps")
//            for (i in 0 until steps.length()) {
//                val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
//                path.add(PolyUtil.decode(points))
//            }
//            for (i in 0 until path.size) {
//                this.mMap.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
//            }
//        }, Response.ErrorListener {
//                _ ->
//        }){}
//        val requestQueue = Volley.newRequestQueue(this)
//        requestQueue.add(directionsRequest)
//    }
}
