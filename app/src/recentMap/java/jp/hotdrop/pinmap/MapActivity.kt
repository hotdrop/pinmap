package jp.hotdrop.pinmap

import android.Manifest
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import jp.hotdrop.pinmap.databinding.ActivityRecentMapBinding
import permissions.dispatcher.*

@RuntimePermissions
class MapActivity: AppCompatActivity() {

    private lateinit var binding: ActivityRecentMapBinding
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recent_map)
        SupportMapFragment.newInstance().let { mapFragment ->
            supportFragmentManager.beginTransaction()
                    .replace(R.id.map_content_view, mapFragment)
                    .commit()
            mapFragment.getMapAsync { googleMap ->
                mMap = googleMap
                googleMap.isIndoorEnabled = false
                setLocation(LOCATION_TOKYO)
                onLocationToMapWithPermissionCheck()
            }
        }
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    internal fun onLocationToMap() {
        mMap.isMyLocationEnabled = true
        LocationServices.getFusedLocationProviderClient(this).let { client ->
            client.lastLocation.addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    LatLng(task.result!!.latitude, task.result!!.longitude).let { latLng ->
                        mMap.addMarker(MarkerOptions()
                                .position(latLng)
                                .title("now Location"))
                        setLocation(latLng)
                    }
                } else {
                    Toast.makeText(this, R.string.receive_location_failure_message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onLocationDenied() {
        Toast.makeText(this, getString(R.string.permission_location_denied), Toast.LENGTH_SHORT).show()
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    fun showRationaleForLocation(request: PermissionRequest) {
        AlertDialog.Builder(this)
                .setTitle(R.string.permission_location_rationale_title)
                .setMessage(R.string.permission_location_rationale)
                .setPositiveButton(android.R.string.ok) { _, _ -> request.proceed() }
                .setNegativeButton(android.R.string.cancel) { _, _ -> request.cancel() }
                .show()
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onLocationNeverAskAgain() {
        Toast.makeText(this, getString(R.string.permission_location_never_ask), Toast.LENGTH_SHORT).show()
    }

    private fun setLocation(latLng: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL))
    }

    companion object {
        private const val DEFAULT_ZOOM_LEVEL = 18f
        private val LOCATION_TOKYO = LatLng(35.681298, 139.766247)
    }
}