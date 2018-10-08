package apextechies.mybuddycab.splash

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast

import com.crashlytics.android.Crashlytics
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStates
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener

import java.util.Arrays

import apextechies.mybuddycab.R
import apextechies.mybuddycab.activity.MainActivity
import io.fabric.sdk.android.Fabric


/**
 * Created by shankar on 8/11/17.
 */

class SplashScreen : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.splas_screen)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {

                EnableGPSAutoMatically()
            }
        }.start()


    }


    private fun EnableGPSAutoMatically() {
        var googleApiClient: GoogleApiClient? = null
        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build()
            googleApiClient!!.connect()
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = (30 * 1000).toLong()
            locationRequest.fastestInterval = (5 * 1000).toLong()
            val builder = LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)

            // **************************
            builder.setAlwaysShow(true) // this is the key ingredient
            // **************************

            val result = LocationServices.SettingsApi
                    .checkLocationSettings(googleApiClient, builder.build())
            result.setResultCallback { result ->
                val status = result.status
                val state = result
                        .locationSettingsStates
                when (status.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS -> {
                        //toast("Success");
                        startActivity(Intent(this@SplashScreen, MainActivity::class.java))
                        finish()
                    }
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        // toast("GPS is not on");
                        // Location settings are not satisfied. But could be
                        // fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling
                            // startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(this@SplashScreen, 1000)

                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                        }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> toast("Setting change not allowed")
                }// Location settings are not satisfied. However, we have
                // no way to fix the
                // settings so we won't show the dialog.
            }
        } else {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                startActivity(Intent(this@SplashScreen, MainActivity::class.java))
                finish()
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                finish()
            }
        }

    }

    override fun onConnected(bundle: Bundle?) {

    }

    override fun onConnectionSuspended(i: Int) {
        toast("Suspended")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        toast("Failed")
    }

    private fun toast(message: String) {
        try {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        } catch (ex: Exception) {
        }

    }

    companion object {
        val RC_SIGN_IN = 1
    }


}

