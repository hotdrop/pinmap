package jp.hotdrop.pinmap

import android.Manifest
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
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

        setupMapView()
    }

    override fun onStop() {
        super.onStop()
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    private fun setupMapView() {
        SupportMapFragment.newInstance().let { mapFragment ->
            supportFragmentManager.beginTransaction()
                    .replace(R.id.map_content_view, mapFragment)
                    .commit()
            mapFragment.getMapAsync { googleMap ->
                mMap = googleMap.apply {
                    this.isIndoorEnabled = false
                }
                setLocation(LOCATION_TOKYO)
                onLocationToMapWithPermissionCheck()
            }
        }
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    internal fun onLocationToMap() {
        mMap.isMyLocationEnabled = true
        receiveLocation()
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

    private fun receiveLocation() {

        // Google Play Services APIの確認。Play Serviceが入っていない端末やバージョンが古い端末は位置情報を取得しない。
        // 位置情報が取得できなくても地図表示はさせたいのでreturnする。ここで終わりたいならfinishなど呼べば良いと思う。
        // そしてDeveloperサイトに従って判定しているが実際ちゃんと判定されるかは未検証。。
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            // デフォルト位置とか設定するかマップ自体表示させず画面閉じるか何かする。
            return
        }

        LocationServices.getFusedLocationProviderClient(this).let { client ->
            client.lastLocation.addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    setLocationWithMarker(LatLng(task.result!!.latitude, task.result!!.longitude))
                } else {
                    getUpdateLocation()
                }
            }
        }
    }

    // 1度失敗した場合はコールバックで位置情報を受け取る。
    // そのため、設定したコールバックをonStopなどで解除する必要があるのでClientとCallbackをフィールドに持つ。
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private val locationCallback: LocationCallback by lazy {
        (object: LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                p0?.lastLocation?.let { location ->
                    Snackbar.make(binding.layoutMap, "位置情報の再取得に成功しました。", Snackbar.LENGTH_LONG).show()
                    setLocationWithMarker(LatLng(location.latitude, location.longitude))
                }
            }
        })
    }

    private fun getUpdateLocation() {
        // とりあえず1回だけ取得
        val request = LocationRequest.create().apply {
            interval = 10000 // ms
            numUpdates = 1
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient?.let { client ->
            client.requestLocationUpdates(request, locationCallback, null)
                    .addOnCompleteListener { task ->
                        if (task.result == null) {
                            // lastLocationで失敗した場合は必ずここを通る。
                            // その後、コールバックがうまくいけば再取得で成功する。
                            Snackbar.make(binding.layoutMap, "位置情報の取得に失敗しました。", Snackbar.LENGTH_LONG).show()
                        }
                    }
        }
    }

    private fun setLocationWithMarker(latLng: LatLng) {
        mMap.addMarker(MarkerOptions()
                .position(latLng)
                .title("now Location"))
        setLocation(latLng)
    }

    private fun setLocation(latLng: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL))
    }

    companion object {
        private const val DEFAULT_ZOOM_LEVEL = 18f
        private val LOCATION_TOKYO = LatLng(35.681298, 139.766247)
    }
}