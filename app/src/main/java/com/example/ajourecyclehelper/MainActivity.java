package com.example.ajourecyclehelper;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.*;
import com.google.mlkit.vision.common.InputImage;

import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private Uri uriImage;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = new RecycleWebView(this);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view,
                                           SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    public void onClickBarcodeCamera(WebView webView) {
        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_ITF)
                .build();

        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(this, options);

        scanner.startScan()
                .addOnSuccessListener(
                        barcode -> {
                            this.webView = webView;
                            this.webView.loadUrl("javascript:setBarcode('"+ barcode.getRawValue() +"')");
                        })
                .addOnCanceledListener(
                        () -> {
                            // Task canceled : 토스트 메세지 팝업
                            Toast.makeText(this, "사용자에 의해 취소", Toast.LENGTH_SHORT).show();
                        })
                .addOnFailureListener(
                        e -> {
                            // Task failed with an exception : 에러 내용 표출
                            Toast.makeText(this, "알 수 없는 오류 발생", Toast.LENGTH_SHORT).show();
                        });
    }

    public void onClickBarcodeGallery(WebView webView) {
        this.webView = webView;

        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    public void onClickGPSButtonTest(View view) {
        onClickLocateGPS(true);
    }

    public void onClickLocateGPS(boolean isClick) {
        boolean isLocationPermissionGranted = checkLocationPermission();

        if (!isLocationPermissionGranted) {
            onClickLocateGPS(isClick);
        }
        else {
            if (isClick) {
                mFusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got newly scanned location. In some rare situations this can be null.
                                if (location != null) {
                                    // Logic to handle location object
                                    String coord = String.valueOf(location.getLatitude()) + ',' + String.valueOf(location.getLongitude());
                                    Log.d("GPS", coord);
                                    webView.loadUrl("javascript:setCoordination('"+ coord +"')");
                                }
                            }
                        });
            }
            else {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    String coord = String.valueOf(location.getLatitude()) + ',' + String.valueOf(location.getLongitude());
                                    Log.d("GPS", coord);
                                    webView.loadUrl("javascript:setCoordination('"+ coord +"')");
                                }
                            }
                        });
            }
        }
    }

    private boolean checkLocationPermission() {
        boolean coarseLocationGranted =
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean fineLocationGranted =
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // Before you perform the actual permission request, check whether your app
        // already has the permissions, and whether your app needs to show a permission
        // rationale dialog. For more details, see (link)Request permissions.(구글개발자)
        if (!coarseLocationGranted && !fineLocationGranted) {
            locationPermissionRequest.launch(new String[]{ android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION });
            return false;
        } else {
            return true;
        }
    }

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri o) {
                    if (o == null) {
                        Toast.makeText(MainActivity.this, "No image Selected", Toast.LENGTH_SHORT).show();
                    } else {
                        InputImage image = null;

                        try {
                            image = InputImage.fromFilePath(MainActivity.this, o);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(
                                        Barcode.FORMAT_EAN_13,
                                        Barcode.FORMAT_EAN_8,
                                        Barcode.FORMAT_ITF)
                                .build();

                        BarcodeScanner scanner = BarcodeScanning.getClient(options);

                        Task<List<Barcode>> result = scanner.process(image)
                                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                                    @Override
                                    public void onSuccess(List<Barcode> barcodes) {
                                        for (Barcode barcode: barcodes) {
                                            Rect bounds = barcode.getBoundingBox();
                                            Point[] corners = barcode.getCornerPoints();
                                            String rawValue = barcode.getRawValue();
                                            webView.loadUrl("javascript:setBarcode('"+ barcode.getRawValue() +"')");
                                        }
                                    }
                                })
                                .addOnCanceledListener(
                                        () -> {
                                            // Task canceled : 토스트 메세지 팝업
                                            Toast.makeText(MainActivity.super.getApplicationContext(), "사용자에 의해 취소", Toast.LENGTH_SHORT).show();
                                        })
                                .addOnFailureListener(
                                        e -> {
                                            // Task failed with an exception : 에러 내용 표출
                                            Toast.makeText(MainActivity.super.getApplicationContext(), "알 수 없는 오류 발생", Toast.LENGTH_SHORT).show();
                                        });
                    }
                }
            });

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION,false);
                        if (fineLocationGranted != null && fineLocationGranted) {
                            // 정확한 위치 접근 허용됨
                        } else if (coarseLocationGranted != null && coarseLocationGranted) {
                            // 대략적인 위치 접근 허용됨
                        } else {
                            // 위치 접근 금지됨

                        }
                    }
            );
}