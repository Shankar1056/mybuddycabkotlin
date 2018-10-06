package apextechies.mybuddycab.map

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.crashlytics.android.Crashlytics

import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.ArrayList
import java.util.HashMap

import apextechies.mybuddycab.R
import apextechies.mybuddycab.Utilz.PlaceDetailsJSONParser
import apextechies.mybuddycab.Utilz.PlaceJSONParser
import apextechies.mybuddycab.Utilz.PreferenceName
import io.fabric.sdk.android.Fabric

import android.content.pm.PackageManager.PERMISSION_GRANTED
import kotlinx.android.synthetic.main.activity_main_search.*


class SearchAddressGooglePlacesActivity : Activity(), LocationListener {
     var placesTask: PlacesTask? = null
     var parserTask: ParserTask?= null
     var currentLatitude: Double = 0.0
     var currentLongitude: Double = 0.0
     val reference_id_list = ArrayList<String>()
     val address_list = ArrayList<String>()
     var placeDetailsParserTask: ParserTask?= null
     var placesParserTask: ParserTask?= null
     val PLACES = 0
     val PLACES_DETAILS = 1
     var placeDetailsDownloadTask: DownloadTask?= null

    protected var locationManager: LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        Fabric.with(this@SearchAddressGooglePlacesActivity, Crashlytics())

        setContentView(R.layout.activity_main_search)
        atv_places.inputType = InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
        currentLatitude = intent.getDoubleExtra("curlat", 0.0)
        currentLongitude = intent.getDoubleExtra("curlong", 0.0)

        cureentlocationtv!!.visibility = View.VISIBLE

        cureentlocationtv.setOnClickListener { CurrentLocation() }

        cancel_search.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.anim_three, R.anim.anim_four)
        }

        atv_places.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                placesTask = PlacesTask()
                placesTask!!.execute(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // TODO Auto-generated method stub
            }

            override fun afterTextChanged(s: Editable) {
                // TODO Auto-generated method stub
            }
        })

        search_address_listview!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            clicked_index = arg2
            placeDetailsDownloadTask = DownloadTask(PLACES_DETAILS)

            if (reference_id_list.size > 0 && reference_id_list[arg2] != null) {
                val url = getPlaceDetailsUrl(reference_id_list[arg2])
                placeDetailsDownloadTask!!.execute(url)
                return@OnItemClickListener
            }
        }
    }


    inner class DownloadTask(type: Int) : AsyncTask<String, Void, String>() {

        private var downloadType = 0

        init {
            this.downloadType = type
        }

        override fun doInBackground(vararg url: String): String {

            var data = ""

            try {
                data = downloadUrl(url[0])
            } catch (e: Exception) {
            }

            return data
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            when (downloadType) {
                PLACES -> {
                    placesParserTask = ParserTask(PLACES)

                    placesParserTask!!.execute(result)
                }

                PLACES_DETAILS -> {
                    placeDetailsParserTask = ParserTask(PLACES_DETAILS)

                    placeDetailsParserTask!!.execute(result)
                }
            }
        }
    }

    private fun getPlaceDetailsUrl(ref: String): String {
        var key = ""

        key = PreferenceName.SERVERKEY

        val reference = "reference=$ref"

        val sensor = "sensor=false"

        val parameters = "$reference&$sensor&$key"

        val output = "json"

        val url = "https://maps.googleapis.com/maps/api/place/details/$output?$parameters"
        Log.i("url", url)

        return url
    }

    override fun onResume() {
        // TODO Auto-generated method stub
        super.onResume()
    }

    private inner class AddressAdapterNew(internal var activity: Activity, internal var objects: List<String>)// TODO Auto-generated constructor stub
        : ArrayAdapter<String>(activity, R.layout.activity_main_search, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            // TODO Auto-generated method stub
            if (convertView == null) {
                convertView = activity.layoutInflater.inflate(R.layout.address_item, null)
            }

            val locationName = convertView!!.findViewById<View>(R.id.location_name) as TextView
            val addressTextview = convertView.findViewById<View>(R.id.address_textview) as TextView
            val timingiv = convertView.findViewById<View>(R.id.timingiv) as ImageView
            val total_addressStrings = objects[position].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            timingiv.visibility = View.GONE

            if (total_addressStrings.size > 0) {
                val first_name = total_addressStrings[0]

                var last_name = ""
                for (i in 1 until total_addressStrings.size) {
                    last_name = last_name + total_addressStrings[i]
                }
                locationName.text = first_name
                addressTextview.text = last_name
            }
            return convertView
        }
    }


    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)

            urlConnection = url.openConnection() as HttpURLConnection

            urlConnection.connect()

            iStream = urlConnection.inputStream

            val br = BufferedReader(InputStreamReader(iStream!!))

            val sb = StringBuffer()

            do {
               var line = br.readLine()

                if (line == null)

                    break

                sb.append(line)

            } while (true)

            data = sb.toString()

            br.close()

        } catch (e: Exception) {
        } finally {
            iStream!!.close()
            urlConnection!!.disconnect()
        }
        return data
    }

    // Fetches all places from GooglePlaces AutoComplete Web Service
    inner class PlacesTask : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()

            progressBar!!.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg place: String): String {
            // For storing data from web service
            var data = ""


            var key = ""

            key = PreferenceName.SERVERKEY

            var input = ""

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8")
            } catch (e1: UnsupportedEncodingException) {
                e1.printStackTrace()
            }

            // place type to be searched
            val types = "types=geocode"

            // Sensor enabled
            val sensor = "sensor=false"
            // Building the parameters to the web service
            val parameters = "$input&$types&$sensor&$key"

            // Output format
            val output = "json"

            // Building the url to the web service
            val url = "https://maps.googleapis.com/maps/api/place/autocomplete/$output?$parameters"

            try {
                // Fetching the data from web service in background
                data = downloadUrl(url)
            } catch (e: Exception) {
                // Log.d("Background Task",e.toString());
            }

            return data
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            // Creating ParserTask
            parserTask = ParserTask(PLACES)

            // Starting Parsing the JSON string returned by Web Service
            parserTask!!.execute(result)
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    inner class ParserTask(type: Int) : AsyncTask<String, Int, List<HashMap<String, String>>>() {
        internal var jObject: JSONObject? = null

        internal var parserType = 0

        init {
            this.parserType = type
        }

        override fun doInBackground(vararg jsonData: String): List<HashMap<String, String>>? {
            var places: List<HashMap<String, String>>? = null

            try {

                if (jsonData[0] != null) {
                    jObject = JSONObject(jsonData[0])
                    when (parserType) {
                        PLACES -> {
                            val placeJsonParser = PlaceJSONParser()
                            // Getting the parsed data as a List construct
                            places = placeJsonParser.parse(jObject!!)
                        }
                        PLACES_DETAILS -> {
                            val placeDetailsJsonParser = PlaceDetailsJSONParser()
                            // Getting the parsed data as a List construct
                            places = placeDetailsJsonParser.parse(jObject!!)
                        }
                    }

                }
            } catch (e: JSONException) {
                // TODO Auto-generated catch block
                //Utilities.printLog("ParserTask Exception: "+e.toString());
            }

            return places
        }

        override fun onPostExecute(result: List<HashMap<String, String>>?) {
            progressBar!!.visibility = View.GONE

            when (parserType) {
                PLACES ->

                    if (result != null) {

                        reference_id_list.clear()
                        address_list.clear()

                        for (i in result.indices) {
                            result[i]["description"]?.let { address_list.add(it) }
                            result[i]["reference"]?.let { reference_id_list.add(it) }
                        }


                        val adapter = AddressAdapterNew(this@SearchAddressGooglePlacesActivity, address_list)
                        search_address_listview!!.adapter = adapter
                    }
                PLACES_DETAILS ->

                    if (result != null && result.size > 0) {
                        val latitude = java.lang.Double.parseDouble(result[0]["lat"])
                        val longitude = java.lang.Double.parseDouble(result[0]["lng"])


                        val returnIntent = Intent()
                        returnIntent.putExtra("SearchAddress", address_list[clicked_index])
                        returnIntent.putExtra("ADDRESS_NAME", "")
                        returnIntent.putExtra("LATITUDE_SEARCH", "" + latitude)
                        returnIntent.putExtra("LONGITUDE_SEARCH", "" + longitude)
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    }
            }
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.anim_three, R.anim.anim_four)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()

        if (locationManager != null)
            locationManager!!.removeUpdates(this)
    }

    private fun CurrentLocation() {
        val pDialog = ProgressDialog(this)
        pDialog.setMessage(resources.getString(R.string.loading))
        pDialog.setCancelable(true)
        pDialog.show()

        Thread(Runnable {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            runOnUiThread {
                var currentLatitude = 0.0
                var currentLongitude = 0.0
                val gpsLocation = getCurrentLocation(LocationManager.GPS_PROVIDER)
                if (gpsLocation != null) {
                    currentLatitude = gpsLocation.latitude
                    currentLongitude = gpsLocation.longitude
                } else {
                    val nwLocation = getCurrentLocation(LocationManager.NETWORK_PROVIDER)
                    if (nwLocation != null) {
                        currentLatitude = nwLocation.latitude
                        currentLongitude = nwLocation.longitude
                    }
                }

                if (currentLatitude == 0.0 || currentLongitude == 0.0) {

                    if (pDialog != null && pDialog.isShowing) {
                        pDialog.dismiss()
                    }

                    showSettingsAlert()
                } else {

                    if (pDialog != null && pDialog.isShowing) {
                        pDialog.dismiss()
                    }


                    val intent = Intent()
                    intent.putExtra("LATITUDE_SEARCH", currentLatitude.toString())
                    intent.putExtra("LONGITUDE_SEARCH", currentLongitude.toString())
                    intent.putExtra("CURRENT", true)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }).start()
    }

    private fun getCurrentLocation(provider: String): Location? {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
                    RC_PERM
            )
        }
        val location: Location
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (locationManager!!.isProviderEnabled(provider)) {
            locationManager!!.requestLocationUpdates(provider, MIN_TIME_FOR_UPDATE, MIN_DISTANCE_FOR_UPDATE.toFloat(), this@SearchAddressGooglePlacesActivity)
            if (locationManager != null) {
                location = locationManager!!.getLastKnownLocation(provider)
                return location
            }
        }
        return null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            //getCurrentLocation();

        } else {
            Toast.makeText(this, R.string.grant_permission, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSettingsAlert() {
        val alertDialog = AlertDialog.Builder(this)

        // Setting Dialog Title
        alertDialog.setTitle("GPS Settings")
        alertDialog.setCancelable(false)

        // Setting Dialog Message
        alertDialog.setMessage("Please help us determine your location to show businesess near you. Click on Settings and turn on. Thanks")

        // On pressing Settings button
        alertDialog.setPositiveButton("Search") { dialog, which -> dialog.dismiss() }

        // on pressing cancel button
        alertDialog.setNegativeButton("Settings") { dialog, which ->
            dialog.dismiss()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

        alertDialog.show()
    }

    override fun onLocationChanged(location: Location) {
        // TODO Auto-generated method stub

    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        // TODO Auto-generated method stub

    }

    override fun onProviderEnabled(provider: String) {
        // TODO Auto-generated method stub

    }

    override fun onProviderDisabled(provider: String) {
        // TODO Auto-generated method stub

    }

    companion object {
        private var clicked_index = 0
        private val MIN_DISTANCE_FOR_UPDATE: Long = 10
        private val MIN_TIME_FOR_UPDATE = (1000 * 60 * 2).toLong()
        private val RC_PERM = 1024
    }
}
