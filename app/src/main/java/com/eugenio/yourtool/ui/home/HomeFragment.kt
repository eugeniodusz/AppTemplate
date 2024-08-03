package com.eugenio.yourtool.ui.home


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
<<<<<<< Updated upstream:app/src/main/java/com/jailton/apptemplateproject/ui/home/HomeFragment.kt
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
=======
>>>>>>> Stashed changes:app/src/main/java/com/eugenio/yourtool/ui/home/HomeFragment.kt
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
<<<<<<< Updated upstream:app/src/main/java/com/jailton/apptemplateproject/ui/home/HomeFragment.kt
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
=======
>>>>>>> Stashed changes:app/src/main/java/com/eugenio/yourtool/ui/home/HomeFragment.kt
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
<<<<<<< Updated upstream:app/src/main/java/com/jailton/apptemplateproject/ui/home/HomeFragment.kt
import com.jailton.apptemplateproject.R
import com.jailton.apptemplateproject.baseclasses.Item
import com.jailton.apptemplateproject.baseclasses.StoreAdapter
import com.jailton.apptemplateproject.databinding.FragmentHomeBinding
import java.util.Locale
=======
import com.eugenio.yourtool.R
import com.eugenio.yourtool.baseclasses.Item
import com.eugenio.yourtool.baseclasses.StoreAdapter
import com.eugenio.yourtool.databinding.FragmentHomeBinding
>>>>>>> Stashed changes:app/src/main/java/com/eugenio/yourtool/ui/home/HomeFragment.kt

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var recyclerViewStores: RecyclerView
    private lateinit var storeAdapter: StoreAdapter
    private lateinit var currentAddressTextView: TextView
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        currentAddressTextView = root.findViewById(R.id.currentAddressTextView)
        recyclerViewStores = root.findViewById(R.id.recyclerViewStores)
        recyclerViewStores.layoutManager = LinearLayoutManager(context)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        database = FirebaseDatabase.getInstance().reference

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        } else {
            getCurrentLocation()
        }

        return root
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                fetchStores(null)
                Snackbar.make(
                    requireView(),
                    "Permission denied. Cannot access location.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun fetchStores(userLocation: Location?) {
        database.child("stores").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val storeList = mutableListOf<Item>()
                for (storeSnapshot in snapshot.children) {
                    val store = storeSnapshot.getValue(Item::class.java)
                    store?.let { storeList.add(it) }
                }
                storeAdapter = StoreAdapter(requireContext(), storeList, userLocation)
                recyclerViewStores.adapter = storeAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Failed to load stores: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            fetchStores(null)
            return
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 5000 // Intervalo de 5 segundos
            fastestInterval = 5000 // Intervalo mais rápido de 5 segundos
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    fetchStores(location)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Não foi possível obter a localização", Toast.LENGTH_SHORT).show()
            }

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    displayAddress(location)
                }
            }
        }, Looper.getMainLooper())
    }


    private fun displayAddress(location: Location) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0].getAddressLine(0)
            currentAddressTextView.text = address
        } else {
            currentAddressTextView.text = "Address not found"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}