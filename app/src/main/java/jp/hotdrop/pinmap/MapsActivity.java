package jp.hotdrop.pinmap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.PermissionChecker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, LocationListener {

    private static final int MY_LOCATION_REQUEST_CODE = 1000;
    private GoogleMap mMap;
    private LocationManager myLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in tokyo and move the camera
        LatLng tokyo = new LatLng(35.681298, 139.766247);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tokyo, 18));

        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "必要な権限は取得済みです。", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "requestLocationUpdatesを実行", Toast.LENGTH_SHORT).show();
            mMap.setMyLocationEnabled(true);
            myLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            String provider = getProvider();
            Toast.makeText(this, "Provider=" + provider, Toast.LENGTH_SHORT).show();
            myLocationManager.requestLocationUpdates(provider, 0, 0, this);
        } else {
            confirmPermission();
        }
    }

    private String getProvider() {
        Criteria crite = new Criteria();
        return myLocationManager.getBestProvider(crite, true);
    }

    private void confirmPermission() {

        //Toast.makeText(this, "should実行", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this).setTitle("パーミッションの説明")
                    .setMessage("このアプリを実行するにはパーミッションが必要です。")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // これTrueもfalseも結局同じことしているので１つでいい
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_LOCATION_REQUEST_CODE);
                        }
                    })
                    .create()
                    .show();
        } else {
            //Toast.makeText(this, "初回requestPermissions実行", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            //if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
            //        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this, "権限の取得に成功しました。", Toast.LENGTH_SHORT).show();
                mMap.setMyLocationEnabled(true);
                myLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                myLocationManager.requestLocationUpdates(getProvider(), 0, 0, this);
            } else {
                Toast.makeText(this, "権限を取得できませんでした。", Toast.LENGTH_SHORT).show();
                //Toast.makeText(this, "permission=" + permissions[0] + " grantResults=" + grantResults[0], Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            myLocationManager.removeUpdates(this);
        } catch(SecurityException e) {
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "LocationChanged実行" , Toast.LENGTH_SHORT).show();
        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));
        try {
            myLocationManager.removeUpdates(this);
        } catch(SecurityException e) {
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
