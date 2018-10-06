package apextechies.mybuddycab.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import apextechies.mybuddycab.R
import apextechies.mybuddycab.Utilz.PreferenceName
import apextechies.mybuddycab.andridservice.AddressResolverService
import apextechies.mybuddycab.common.ClsGeneral
import apextechies.mybuddycab.map.SearchAddressGooglePlacesActivity
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, GoogleMap.OnMapClickListener, PlaceSelectionListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraChangeListener {
    private var mMap: GoogleMap? = null
    private var mCurrentLocationSource: LatLng? = null
    private var mCurrentLocationDest: LatLng? = null
    private var mResultReceiver: AddressResultReceiver? = null
    private var addressText: String? = ""
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationSource: Location? = null
    private var mLocationDest: Location? = null
    private var forsource = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(apextechies.mybuddycab.R.layout.activity_main)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationSource = Location("")
        mLocationDest = Location("")
        initWidget()
        initMap()

    }

    private fun initMap() {
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        mResultReceiver = AddressResultReceiver(Handler())
    }

    private fun initWidget() {
        setSupportActionBar(toolbar)

        fab_current_loc.setOnClickListener {
            if (!checkPermissions()) {
                requestPermissions()
            } else {
                getLastLocation()
            }
        }
        btn_save.setOnClickListener {
            getLocation()
        }
        source!!.setOnClickListener {
            forsource = true
            val addressIntent = Intent(this@MainActivity, SearchAddressGooglePlacesActivity::class.java)
            addressIntent.putExtra("curlat", mLocationSource!!.latitude)
            addressIntent.putExtra("curlong", mLocationSource!!.latitude)
            startActivityForResult(addressIntent, 20)
            this.overridePendingTransition(R.anim.anim_two, R.anim.anim_one)
        }
        destination!!.setOnClickListener {
            forsource = false
            val dest = Intent(this@MainActivity, SearchAddressGooglePlacesActivity::class.java)
            dest.putExtra("curlat", mLocationDest!!.latitude)
            dest.putExtra("curlong", mLocationDest!!.latitude)
            startActivityForResult(dest, 20)
            this.overridePendingTransition(R.anim.anim_two, R.anim.anim_one)
        }
    }


    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.setOnCameraChangeListener(this)
        googleMap.setOnMapClickListener(this)

        if (!checkPermissions()) {
            requestPermissions()
        } else {
            getLastLocation()
        }
    }


    override fun onLocationChanged(location: Location) {
        onMapClick(LatLng(location.latitude, location.longitude))
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {   }

    override fun onProviderEnabled(s: String) {  }

    override fun onProviderDisabled(s: String) { }

    override fun onMapClick(latLng: LatLng) {

        mMap!!.clear()
        if (forsource) {
            mCurrentLocationSource = latLng
            mLocationSource!!.latitude = latLng.latitude
            mLocationSource!!.longitude = latLng.longitude
           getLocationShowMap(mCurrentLocationSource, mLocationSource!!)
        } else {
            mCurrentLocationDest = latLng
            mLocationDest!!.latitude = latLng.latitude
            mLocationDest!!.longitude = latLng.longitude
            getLocationShowMap(mCurrentLocationSource, mLocationSource!!)
        }
    }

    private fun getLocationShowMap(mCurrentLocationsource: LatLng?, mLocationsource: Location) {
        resetMap(mCurrentLocationsource!!.latitude, mCurrentLocationsource!!.longitude)
        startIntentService(mLocationsource)




}


    private fun getLocation() {

        val intent = Intent(this@MainActivity, MapWithAllCalculation::class.java)
        intent.putExtra("currentLatSource", mCurrentLocationSource!!.latitude)
        intent.putExtra("currentLonSource", mCurrentLocationSource!!.longitude)
        intent.putExtra("currentLatDest", mCurrentLocationDest!!.latitude)
        intent.putExtra("currentLonDest", mCurrentLocationDest!!.longitude)
        intent.putExtra("source", source!!.text.toString())
        intent.putExtra("destination", destination!!.text.toString())
        startActivity(intent)
    }


    override fun onPlaceSelected(place: Place) {
        Log.i(TAG, "Place: " + place.name)
        onMapClick(place.latLng)
    }

    override fun onError(status: Status) {

    }

    override fun onCameraMove() {

    }

    override fun onCameraChange(cameraPosition: CameraPosition) {
      // if (forsource) {
            mCurrentLocationSource = cameraPosition.target
            mMap!!.clear()
            try {
                mLocationSource!!.latitude = mCurrentLocationSource!!.latitude
                mLocationSource!!.longitude = mCurrentLocationSource!!.longitude
                resetMap(mCurrentLocationSource!!.latitude, mCurrentLocationSource!!.longitude)
                startIntentService(mLocationSource)
            } catch (e: Exception) {
                Log.e(TAG, e.message)
            }

//        } else {
//            mCurrentLocationDest = cameraPosition.target
//            mMap!!.clear()
//            try {
//                mLocationDest!!.latitude = mCurrentLocationDest!!.latitude
//                mLocationDest!!.longitude = mCurrentLocationDest!!.longitude
//                startIntentService(mLocationDest)
//            } catch (e: Exception) {
//                Log.e(TAG, e.message)
//            }
//
//        }

    }

    protected fun startIntentService(mLocation: Location?) {
        val intent = Intent(this, AddressResolverService::class.java)
        intent.putExtra(AddressResolverService.RECEIVER, mResultReceiver)
        intent.putExtra(AddressResolverService.LOCATION_DATA_EXTRA, mLocation)

        startService(intent)
    }

    private inner class AddressResultReceiver internal constructor(handler: Handler) : ResultReceiver(handler) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            val address = resultData.getString(AddressResolverService.LOCATION_DATA_STREET)
            try {
                if (forsource) {
                    source.setText(address)
                    mCurrentLocationSource = LatLng(java.lang.Double.parseDouble(resultData.getString(AddressResolverService.LAT)), java.lang.Double.parseDouble(resultData.getString(AddressResolverService.LON)))
                    resetMap(mCurrentLocationSource!!.latitude, mCurrentLocationSource!!.longitude)

                } else {
                    destination.setText(address)
                    mCurrentLocationDest = LatLng(java.lang.Double.parseDouble(resultData.getString(AddressResolverService.LAT)), java.lang.Double.parseDouble(resultData.getString(AddressResolverService.LON)))
                    resetMap(mCurrentLocationDest!!.latitude, mCurrentLocationDest!!.longitude)

                }
                addressText = address
            } catch (e: NullPointerException) {
                Log.e(TAG, e.message)
            }

        }
    }


    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        mFusedLocationClient!!.lastLocation
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful && task.result != null) {
                        if (forsource) {
                            mLocationSource = task.result
                            mCurrentLocationSource = LatLng(mLocationSource!!.latitude, mLocationSource!!.longitude)
                            startIntentService(mLocationSource)
                            resetMap(mCurrentLocationSource!!.latitude, mCurrentLocationSource!!.longitude)

                        } else {
                            mLocationDest = task.result
                            mCurrentLocationDest = LatLng(mLocationDest!!.latitude, mLocationDest!!.longitude)
                            startIntentService(mLocationDest)
                           resetMap(mCurrentLocationDest!!.latitude, mCurrentLocationDest!!.longitude)

                        }

                    } else {
                        Log.w(TAG, "getLastLocation:exception", task.exception)
                    }
                }
    }

     fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

     fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE)
    }

     fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")

        } else {
            Log.i(TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 20 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val isCurrentLocation = data.getBooleanExtra("CURRENT", false)
                if (isCurrentLocation)
                //if user selects current location
                {
                    if (forsource) {
                        val latitudeString = data.getStringExtra("LATITUDE_SEARCH")
                        val logitudeString = data.getStringExtra("LONGITUDE_SEARCH")

                        ClsGeneral.setPreferences(this@MainActivity, PreferenceName.LATITUTESOURCE, "" + latitudeString)
                        ClsGeneral.setPreferences(this@MainActivity, PreferenceName.LONGITUTESOURCE, "" + logitudeString)

                        mLocationSource!!.latitude = java.lang.Double.parseDouble(latitudeString)
                        mLocationSource!!.longitude = java.lang.Double.parseDouble(logitudeString)
                        resetMap(mLocationSource!!.latitude, mLocationSource!!.longitude)
                        startIntentService(mLocationSource)
                    } else {
                        val latitudeString = data.getStringExtra("LATITUDE_SEARCH")
                        val logitudeString = data.getStringExtra("LONGITUDE_SEARCH")

                        ClsGeneral.setPreferences(this@MainActivity, PreferenceName.LATITUTEDEST, "" + latitudeString)
                        ClsGeneral.setPreferences(this@MainActivity, PreferenceName.LONGITUTEDEST, "" + logitudeString)

                        mLocationDest!!.latitude = java.lang.Double.parseDouble(latitudeString)
                        mLocationDest!!.longitude = java.lang.Double.parseDouble(logitudeString)
                        resetMap(mLocationSource!!.latitude, mLocationSource!!.longitude)
                        startIntentService(mLocationDest)
                    }

                } else {

                    val location = data.getStringExtra("SearchAddress")
                    if (location != null) {


                        val parts = location.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

                        if (parts.size > 0) {
                            try {
                                if (forsource) {
                                    val latitudeString = data.getStringExtra("LATITUDE_SEARCH")
                                    val logitudeString = data.getStringExtra("LONGITUDE_SEARCH")
                                    ClsGeneral.setPreferences(this@MainActivity, PreferenceName.LATITUTESOURCE, "" + latitudeString)
                                    ClsGeneral.setPreferences(this@MainActivity, PreferenceName.LONGITUTESOURCE, "" + logitudeString)

                                    mLocationSource!!.latitude = java.lang.Double.parseDouble(latitudeString)
                                    mLocationSource!!.longitude = java.lang.Double.parseDouble(logitudeString)
                                    resetMap(mLocationSource!!.latitude, mLocationSource!!.longitude)
                                    startIntentService(mLocationSource)
                                } else {
                                    val latitudeString = data.getStringExtra("LATITUDE_SEARCH")
                                    val logitudeString = data.getStringExtra("LONGITUDE_SEARCH")
                                    ClsGeneral.setPreferences(this@MainActivity, PreferenceName.LATITUTEDEST, "" + latitudeString)
                                    ClsGeneral.setPreferences(this@MainActivity, PreferenceName.LONGITUTEDEST, "" + logitudeString)

                                    mLocationDest!!.latitude = java.lang.Double.parseDouble(latitudeString)
                                    mLocationDest!!.longitude = java.lang.Double.parseDouble(logitudeString)
                                    resetMap(mLocationSource!!.latitude, mLocationSource!!.longitude)
                                    startIntentService(mLocationDest)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, e.message)
                            }

                        }
                    } else {

                        Toast.makeText(this@MainActivity, "Error, please try again", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun resetMap(lat: Double, lng: Double) {
        val latlng = LatLng(lat, lng)
        if (forsource) {
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15.0f))
        }else{
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15.0f))
        }
    }



companion object {

        private val TAG = "MapActivity"
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }

}



