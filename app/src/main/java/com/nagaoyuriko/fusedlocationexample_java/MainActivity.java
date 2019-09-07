package com.nagaoyuriko.fusedlocationexample_java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;

    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;

    @Override
    protected void onStart() {
        super.onStart();
        if (!checkPermissions()) {
            requestPermissions();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button locationButton = findViewById(R.id.locationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLastLocation();
            }
        });
        mLatitudeTextView = findViewById(R.id.latitudeTextView);
        mLongitudeTextView = findViewById(R.id.longitudeTextView);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                            String latitude = String.valueOf(mLastLocation.getLatitude());
                            String longitude = String.valueOf(mLastLocation.getLongitude());
                            mLatitudeTextView.setText(latitude);
                            mLongitudeTextView.setText(longitude);
                        } else {
                            Log.w("ろぐ", "getLastLocation:exception", task.getException());
                            showSnackbar("位置情報を取得できませんでした");
                        }
                    }
                });
    }

    /**
     * すでにパーミッションに許可されているかどうかをチェックするメソッド
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

        if (shouldProvideRationale) {
            Log.i("ろぐ", "位置情報を許可しないを選択した状態");

            showSnackbar("位置情報の許可が必須です(T_T)！", "OK",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i("ろぐ", "位置情報パーミッションのリクエストを開始");
            startLocationPermissionRequest();
        }
    }

    /**
     * パーミッションに許可してもらったかどうかのコールバック
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // 許可する、許可しないのダイアログがキャンセルされた時
                Log.i("ろぐ", "許可ダイアログでキャンセルボタンが押された");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 許可する、が押された場合
                Log.i("ろぐ", "許可もらえたので位置情報を取得しにいく");
                getLastLocation();
            } else {
                // 許可しない、を押された場合
                showSnackbar("位置情報の許可が必要なので設定画面でお願いします", "設定画面にいく",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    /**
     * シンプルなSnackBarを出すメソッド
     */
    private void showSnackbar(final String text) {
        View container = findViewById(R.id.containerLayout);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * アクションつきのSnackBarを出すメソッド
     */
    private void showSnackbar(final String mainTextString, final String actionString,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                mainTextString,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(actionString, listener).show();
    }
}
