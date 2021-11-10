package fr.alvernhe.surroundead.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import fr.alvernhe.surroundead.MainActivity
import fr.alvernhe.surroundead.MapsActivity
import fr.alvernhe.surroundead.R


class MainScreenMenu : Fragment() {

    val REQUEST_CODE_UPDATE_LOCATION = 42

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main_screen_menu, container, false)
        val buttonGoToMap = view.findViewById<Button>(R.id.goToMap)

        buttonGoToMap.setOnClickListener {
            if (CheckPermission()){
                if (isLocationEnabled()){
                    lanceMapActivity()
                }
                else{
                    Toast.makeText(requireContext(), "Activez votre localisation svp", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                RequestPermission()
            }
        }

        return view
    }

    override fun onDetach() {
        super.onDetach()
        activity?.also { main->
            if (main is MainActivity){
                main.MenuFragment = null
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.also { main->
            if (main is MainActivity){
                main.MenuFragment = this
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        //this function will return to us the state of the location service
        //if the gps or the network provider is enabled then it will return true otherwise it will return false
        var locationManager = getActivity()?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    private fun lanceMapActivity(){
        val intent = Intent(requireContext(), MapsActivity::class.java)
        startActivity(intent)
    }

    private fun CheckPermission(): Boolean {
        //this function will return a boolean
        //true: if we have permission
        //false if not
        if (
            ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false
    }

    fun RequestPermission() {
        //this function will allows us to tell the user to requesut the necessary permsiion if they are not garented
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_CODE_UPDATE_LOCATION
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
                    Toast.makeText(requireContext(), "Permission accordée.", Toast.LENGTH_LONG)
                        .show()
                    lanceMapActivity()
                } else {
                    Toast.makeText(requireContext(), "Permission refusée.", Toast.LENGTH_LONG)
                        .show()
                }
                return
            }
        }
    }


}