package fr.alvernhe.surroundead

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.SupportMapFragment
import java.util.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import android.os.Vibrator
import android.os.VibrationEffect
import androidx.activity.result.contract.ActivityResultContracts


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    var currentLocation: LatLng? = null
    var myMap: GoogleMap? = null
    var marker: Marker? = null

    private val REQUEST_CODE_UPDATE_LOCATION = 42
    private var GPSLocationClient: FusedLocationProviderClient? = null
    private var GPSLocationCallback: LocationCallback? = null
    var zoneJeuIsCreate = false
    var myZone: Polyline? = null
    var bordures: MutableList<Float>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)


        var zoomButton = findViewById<Button>(R.id.zoomOnMe)
        var vibreButton = findViewById<Button>(R.id.vibreButton)
        var zoneJeuButton = findViewById<Button>(R.id.zoneJeuButton)
        var resetZoneJeuButton = findViewById<Button>(R.id.resetZoneJeuButton)


        // Get instance of Vibrator from current Context
        // Get instance of Vibrator from current Context
        val v = getSystemService(VIBRATOR_SERVICE) as Vibrator

        /*
        Build.VERSION.SDK_INT
        Build.VERSION_CODES.
         */

        GPSUpdateLocation()


        resetZoneJeuButton.setOnClickListener {
            if (myZone != null && zoneJeuIsCreate) {
                myZone!!.remove()
                zoneJeuIsCreate = false
                bordures = null
            } else {
                Toast.makeText(this, "Pas de zone de jeu à supprimer", Toast.LENGTH_SHORT).show()
            }
        }

        zoneJeuButton.setOnClickListener {
            if (myMap != null && marker != null && !zoneJeuIsCreate) {
                zoneJeuIsCreate = true
                val dimention = 0.0014F
                myZone = myMap!!.addPolyline(
                    PolylineOptions()
                        .add(
                            LatLng(
                                marker!!.position.latitude - dimention,
                                marker!!.position.longitude - dimention
                            ),
                            LatLng(
                                marker!!.position.latitude + dimention,
                                marker!!.position.longitude - dimention
                            ),
                            LatLng(
                                marker!!.position.latitude + dimention,
                                marker!!.position.longitude + dimention
                            ),
                            LatLng(
                                marker!!.position.latitude - dimention,
                                marker!!.position.longitude + dimention
                            ),
                            LatLng(
                                marker!!.position.latitude - dimention,
                                marker!!.position.longitude - dimention
                            )
                        )
                )
                // min lat / max lat
                bordures = mutableListOf(
                    (marker!!.position.longitude - dimention).toFloat(),
                    (marker!!.position.longitude + dimention).toFloat(),
                    (marker!!.position.latitude - dimention).toFloat(),
                    (marker!!.position.latitude + dimention).toFloat()
                )
            } else {
                Toast.makeText(this, "Impossible de créer une zone de Jeu", Toast.LENGTH_SHORT)
                    .show()
            }
        }


        zoomButton.setOnClickListener {
            if(isLocationEnabled()){
                if (CheckPermission()) {
                    val bondiLocation: CameraPosition

                    if (zoneJeuIsCreate) {
                        bondiLocation = CameraPosition.Builder()
                            .target(currentLocation!!)
                            .zoom(17.4f)
                            .bearing(0f)
                            .tilt(0f)
                            .build()
                    }
                    else{
                        bondiLocation= CameraPosition.Builder()
                            .target(currentLocation!!)
                            .zoom(19.3f)
                            .bearing(0f)
                            .tilt(0f)
                            .build()
                    }
                    myMap?.animateCamera(CameraUpdateFactory.newCameraPosition(bondiLocation))
                } else {
                    RequestPermission()
                }
            }
            else{
                Toast.makeText(this, "Geolocalisation desactivée", Toast.LENGTH_SHORT).show()
            }
        }

        vibreButton.setOnClickListener {

            val vibrationWaveFormDurationPattern =
                longArrayOf(
                    0,
                    100,
                    80,
                    100,
                    80,
                    100,
                    80,
                    100,
                    40,
                    70,
                    40,
                    70,
                    40,
                    70,
                    40,
                    70,
                    200,
                    70
                )
            // the vibration of the type custom waveforms needs the API version 26
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                // create VibrationEffect instance and createWaveform of vibrationWaveFormDurationPattern
                // -1 here is the parameter which indicates that the vibration shouldn't be repeated.
                val vibrationEffect =
                    VibrationEffect.createWaveform(vibrationWaveFormDurationPattern, -1)

                // it is safe to cancel all the vibration taking place currently
                v.cancel()

                // now initiate the vibration of the device
                v.vibrate(vibrationEffect)
            } else {
                //deprecated in API 26
                v.vibrate(50)
            }
        }


        // Get a handle to the fragment and register the callback.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

    }

    // Get a handle to the GoogleMap object and display marker.
    override fun onMapReady(googleMap: GoogleMap) {

        myMap = googleMap

        myMap!!.setOnMarkerClickListener {
            if (it.getTitle() == "YOU") {
                Toast.makeText(this, "you cliqued on you", Toast.LENGTH_SHORT).show()
                true // comportement par default en moins
            } else {
                false // comportement par default en plus
            }
        }
    }
    /*
    @param position : contient la latitude et la longitude de la position où il faut placer un marker.
    @param label : donne un tag à ton marker pour le défférencier
     */
    fun putMarkerOnMap(position: LatLng, label: String) {
        if (marker == null) {
            marker = myMap?.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(label)
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.b))
            )
        } else {
            marker!!.position = position
        }
        myMap?.moveCamera(CameraUpdateFactory.newLatLng(position))
    }

    /*
    This function check if the user accept permissions for us to use Geolocalisation
    this function will return a boolean
    //true: if we have permission
    //false if not
     */
    private fun CheckPermission(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false
    }

    // demande la permission
    fun RequestPermission() {
        //this function will allows us to tell the user to requesut the necessary permsiion if they are not garented
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_CODE_UPDATE_LOCATION
        )
    }

    // regarde si la localisaiton est activée
    fun isLocationEnabled(): Boolean {
        //this function will return to us the state of the location service
        //if the gps or the network provider is enabled then it will return true otherwise it will return false
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_UPDATE_LOCATION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GPSUpdateLocation()
                } else {
                    Toast.makeText(this, "Permission refusée.", Toast.LENGTH_LONG)
                        .show()
                }
                return
            }
        }
    }

    // function ou on "s'abonne" àla localisation
    @SuppressLint("MissingPermission")
    fun GPSUpdateLocation() {
        if (!CheckPermission()) {
            RequestPermission()
        } else {
            if (isLocationEnabled()) {

                val locationRequest = LocationRequest()
                locationRequest.interval = 100
                locationRequest.fastestInterval = 100 // en millisecondes
                locationRequest.setSmallestDisplacement(1F) // en mètres
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

                GPSLocationCallback = object : LocationCallback() {
                    // on entre dans cette fonction a chaque fois que la position change
                    override fun onLocationResult(loc: LocationResult) {
                        findViewById<TextView>(R.id.textView).text =
                            loc.lastLocation.latitude.toString() + "," + loc.lastLocation.longitude.toString()
                        currentLocation =
                            LatLng(loc.lastLocation.latitude, loc.lastLocation.longitude)
                        putMarkerOnMap(currentLocation!!, "YOU")
                        if (zoneJeuIsCreate) {
                            if (!isInZoneDeJeu()) {
                                Toast.makeText(
                                    this@MapsActivity,
                                    "Vous n'êtes plus dans la zone",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    }
                }

                // on s'abonne à la position
                getGPSLocationClient().requestLocationUpdates(
                    locationRequest,        // parametres (fréquence etc... )
                    GPSLocationCallback,    // Ce que tu dois faire si la localisation change
                    null /* Looper */
                )


            }
        }
    }


    fun isInZoneDeJeu(): Boolean {
        var conditionPourEnvoyerUnefoisSeulement = true

        if (conditionPourEnvoyerUnefoisSeulement) {
            if (bordures != null && currentLocation != null && bordures!!.size == 4) {
                if (currentLocation!!.latitude < bordures!![2] || currentLocation!!.latitude > bordures!![3]) {
                    return false
                } else {
                    if (currentLocation!!.longitude < bordures!![0] || currentLocation!!.longitude > bordures!![1]) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun getGPSLocationClient(): FusedLocationProviderClient {
        if (GPSLocationClient == null) {
            GPSLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }
        return GPSLocationClient!!
    }

    override fun onPause() {
        super.onPause()
        if (GPSLocationClient != null) {
            if (GPSLocationCallback != null) {
                // on se désabonne de la localisation
                GPSLocationClient!!.removeLocationUpdates(GPSLocationCallback)
            }
            GPSLocationCallback = null
            GPSLocationClient = null
        }
    }

    override fun onResume() {
        super.onResume()
        // on se ré-abonne
        GPSUpdateLocation()
    }

}