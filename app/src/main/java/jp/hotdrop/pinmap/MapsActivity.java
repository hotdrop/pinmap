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

        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            myLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            String provider = getProvider();
            Location lastLocation = myLocationManager.getLastKnownLocation(provider);
            if(lastLocation != null) {
                setLocation(lastLocation);
            }
            mMap.setMyLocationEnabled(true);
            Toast.makeText(this, "Provider=" + provider, Toast.LENGTH_SHORT).show();
            myLocationManager.requestLocationUpdates(provider, 0, 0, this);
        } else {
            setDefaultLocation();
            confirmPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            // よくサンプルコードでは以下のように引数でパーミッションチェックしています。
            //if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
            //        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 一方、パーミッションの許可ダイアログで「許可」された場合、このコールバックメソッド以降で現在位置の取得処理を
            // 行う必要があります。
            // 現在位置の取得はrequestLocationUpdatesを実行する必要がありますが、パーミッションチェックをやれとエラーが出ます。
            // そこで、このメソッドに到達した時点ではすでにパーミッションが許可/拒否されていますので、引数でなくとも
            // heckSelfPermissionを実行すればエラーも解消されますし良いかなと思って、以下のようにしています。
            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                myLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                myLocationManager.requestLocationUpdates(getProvider(), 0, 0, this);
            } else {
                Toast.makeText(this, "権限を取得できませんでした。", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "LocationChanged実行" , Toast.LENGTH_SHORT).show();
        setLocation(location);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            myLocationManager.removeUpdates(this);
        } catch(SecurityException e) {
          // removeUpdatesを使用する場合もパーミッションチェックをするか、このようにSecurityExceptionをキャッチする対応が必要です。
          // onRequestPermissionsResultでパーミッションチェックを例にしたのでこちらはSecurityExceptionで対応します。
          // 何もしてませんが、本当は例外に応じた後続処理を書く必要があります。
        }
    }

    private String getProvider() {
        Criteria criteria = new Criteria();
        return myLocationManager.getBestProvider(criteria, true);
    }

    private void confirmPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this).setTitle("パーミッション説明")
                    .setMessage("このアプリを実行するには位置情報の権限を与えてやる必要です。よろしくお願い致します。")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // trueもfalseも結局同じrequestPermissionsを実行しているので一つにまとめるべきかも
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_LOCATION_REQUEST_CODE);
                        }
                    })
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        }
    }

    private void setDefaultLocation() {
        LatLng tokyo = new LatLng(35.681298, 139.766247);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tokyo, 18));
    }

    private void setLocation(Location location) {
        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(myLocation).title("now Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18));
    }
}
