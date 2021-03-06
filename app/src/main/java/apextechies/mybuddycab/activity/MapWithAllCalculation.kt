package apextechies.mybuddycab.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import apextechies.mybuddycab.R
import apextechies.mybuddycab.Utilz.PreferenceName
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.errors.ApiException
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import kotlinx.android.synthetic.main.mapwithallcalculation.*
import org.joda.time.DateTime
import java.io.IOException
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit

class MapWithAllCalculation : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    var directionApi = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mapwithallcalculation)
        initMap()

    }

    @Synchronized
    private fun initMap() {
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)


    }

    private fun getIntentData() {
        textsource!!.text = intent.getStringExtra("source")
        textdest!!.text = intent.getStringExtra("destination")
        val currentLatSource = intent.getDoubleExtra("currentLatSource", 0.0)
        val currentLonSource = intent.getDoubleExtra("currentLonSource", 0.0)
        val currentLatDest = intent.getDoubleExtra("currentLatDest", 0.0)
        val currentLonDest = intent.getDoubleExtra("currentLonDest", 0.0)
        val  mCurrentLocationSource = LatLng(currentLatSource, currentLonSource)
        val mCurrentLocationDest = LatLng(currentLatDest, currentLonDest)



        var result =  getDirectionsDetails(intent.getStringExtra("source"),intent.getStringExtra("destination"),TravelMode.DRIVING);
        if (result != null) {
            getEndLocationTitle(result)
            addPolyline(result)
            addMarkersToMap(result)
        }

        createMarker(mCurrentLocationSource, intent.getStringExtra("source"), BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        createMarker(mCurrentLocationDest, intent.getStringExtra("destination"), BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))



    }

    @Synchronized
    private fun createMarker(currentLatSource: LatLng, stringExtra: String?, defaultMarker: BitmapDescriptor?) {

            mMap?.addMarker(MarkerOptions()
                    .position(currentLatSource)
                    .anchor(0.5f, 0.5f)
                    .title(stringExtra)
                    .icon(defaultMarker))

            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatSource, 15.0f))

    }



    private fun getDirectionsDetails(origin: String, destination: String, mode: TravelMode): DirectionsResult? {
            val now = DateTime()
            try {
                directionApi = "https://maps.googleapis.com/maps/api/directions/json?origin="+origin+"&destination="+destination+"&"+PreferenceName.SERVERKEY
                val geoApiContext = GeoApiContext()
                 geoApiContext.setQueryRateLimit(3)
                        .setApiKey(directionApi)
                .setConnectTimeout(1, TimeUnit.SECONDS).setReadTimeout(1, TimeUnit.SECONDS).setWriteTimeout(1, TimeUnit.SECONDS)

                return DirectionsApi.newRequest(geoApiContext)
                        .mode(mode)
                        .origin(origin)
                        .destination(destination)
                        .departureTime(now)
                        .await()
            } catch (e: ApiException) {
                e.printStackTrace()
                return null
            } catch (e: InterruptedException) {
                e.printStackTrace()
                return null
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
            catch (e: IllegalStateException) {
                e.printStackTrace()
                return null
            }


    }


    fun addMarkersToMap(results: DirectionsResult) {
        mMap!!.addMarker(MarkerOptions().position(LatLng(results.routes[0].legs[0].startLocation.lat, results.routes[0].legs[0].startLocation.lng)).title(results.routes[0].legs[0].startAddress));
        mMap!!.addMarker(MarkerOptions().position(LatLng(results.routes[0].legs[0].endLocation.lat, results.routes[0].legs[0].endLocation.lng)).title(results.routes[0].legs[0].startAddress).snippet(getEndLocationTitle(results)));
    }

    private fun getEndLocationTitle(results: DirectionsResult): String {
        return "Time :" + results.routes[0].legs[0].duration.humanReadable + " Distance :" + results.routes[0].legs[0].distance.humanReadable
    }

    private fun addPolyline(results: DirectionsResult) {
        var decodedPath = PolyUtil.decode(results.routes[0].overviewPolyline.getEncodedPath());
        mMap!!.addPolyline(PolylineOptions().addAll(decodedPath));
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getIntentData()

    }


}
