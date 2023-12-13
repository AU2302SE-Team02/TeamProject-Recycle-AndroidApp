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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.*;
import com.google.mlkit.vision.common.InputImage;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private Uri uriImage;
    private FusedLocationProviderClient mFusedLocationClient;
    private String userAddress = "경기도-수원시-팔달구-우만2동";
    private static final String KEY_SEARCH_LOG_FILE = "search_log_file";
    private static final String KEY_SEARCH_LOG = "search_log";
    private long backBtnDelay = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 웹뷰 프래그먼트 초기화
        webView = new RecycleWebView(this);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view,
                                           SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        // 위치 좌표 정보 수신을 위한 FusedLocationProviderClient 초기화
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnDelay;
        if (webView.canGoBack()) {
            webView.goBack();
        } else if (0 <= gapTime && 2000 >= gapTime) {
            super.onBackPressed();
        } else {
            backBtnDelay = curTime;
            Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
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
        // onClickLocateGPS(true);
    }

    public void onClickLocateGPS(WebView webView, boolean isClick) {
        Gson gson = new GsonBuilder().setLenient().create();

        boolean isLocationPermissionGranted = checkLocationPermission();

        if (!isLocationPermissionGranted) {
            Toast.makeText(MainActivity.this, "위치 권한 없음. 정상적인 서비스 이용을 위해 설정에서 허용해 주세요.", Toast.LENGTH_LONG).show();
        }
        else {
            if (isClick) {
                mFusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got newly scanned location. In some rare situations this can be null.
                                if (location != null) {
                                    // Logic to handle location object
                                    Log.d("GPS", String.valueOf(location.getLatitude()) + "/" + String.valueOf(location.getLongitude()));
                                    Map<String, String> query = new HashMap<>();
                                    query.put("latitude", String.valueOf(location.getLatitude()));
                                    query.put("longitude", String.valueOf(location.getLongitude()));

                                    // Retrofit 생성
                                    Retrofit retrofit = new Retrofit.Builder()
                                            .baseUrl(AddressService.ADDRESS_SERVER_URL) // 기본으로 적용되는 서버 URL (반드시 / 로 마무리되게 설정)
                                            .addConverterFactory(GsonConverterFactory.create(gson)) // JSON 데이터를 Gson 라이브러리로 파싱하고 데이터를 Model에 자동으로 담는 converter
                                            .build();

                                    AddressService retrofitAPI = retrofit.create(AddressService.class);

                                    retrofitAPI.getAddress(query).enqueue(new Callback<AddressJson>() {
                                        // interface 에서 정의했던 메소드 중 하나를 선언하고, 비동기 통신을 실행한다.
                                        // 통신이 완료되었을 때 이벤트를 처리하기 위해 Callback 리스너도 함께 등록한다.
                                        @Override
                                        public void onResponse(Call<AddressJson> call, Response<AddressJson> response) {
                                            if (response.isSuccessful()) { // 원활하게 통신이 이뤄졌을 때
                                                Log.d("Location", "Response Success");
                                                Log.d("Location", response.toString());
                                                AddressJson data = response.body(); // 응답 내용을 변수에 입력

                                                String addressLvl1 = data.getAddressLvl1().replace(" ", "-");
                                                String addressLvl2 = data.getAddressLvl2().replace(" ", "-");
                                                String addressLvl3 = data.getAddressLvl3().replace(" ", "-");
                                                String address = addressLvl1+"-"+addressLvl2+"-"+addressLvl3;
                                                if (data.getAddressLand() != null) address = address+"-"+data.getAddressLand();
                                                MainActivity.this.webView = webView;
                                                MainActivity.this.webView.loadUrl("javascript:setLocation('"+address+"')");
                                                // webView.reload();
                                                // webView.loadUrl("javascript:setLocation('경기도-수원시-아주구-테스트동')");
                                                // Toast.makeText(MainActivity.super.getApplicationContext(), "신규 주소 변환 성공: " + address, Toast.LENGTH_SHORT).show();
                                                // setUserAddress(address);
                                            } else { // 원활한 통신이 이뤄지지 않았을 때
                                                Log.d("Location","Response Fail: ");
                                                Toast.makeText(MainActivity.super.getApplicationContext(), "주소 변환 실패", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        // 통신 중 생각하지 못한 예외(네트워크 오류 등)가 발생되었을 때 호출된다.
                                        @Override
                                        public void onFailure(Call<AddressJson> call, Throwable t) {
                                            Toast.makeText(MainActivity.super.getApplicationContext(), "서버 통신 실패", Toast.LENGTH_SHORT).show();
                                            t.printStackTrace();
                                        }
                                    });
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
                                Log.d("GPS", String.valueOf(location.getLatitude()) + "/" + String.valueOf(location.getLongitude()));
                                Map<String, String> query = new HashMap<>();
                                query.put("latitude", String.valueOf(location.getLatitude()));
                                query.put("longitude", String.valueOf(location.getLongitude()));

                                // Retrofit 생성
                                Retrofit retrofit = new Retrofit.Builder()
                                        .baseUrl(AddressService.ADDRESS_SERVER_URL) // 기본으로 적용되는 서버 URL (반드시 / 로 마무리되게 설정)
                                        .addConverterFactory(GsonConverterFactory.create(gson)) // JSON 데이터를 Gson 라이브러리로 파싱하고 데이터를 Model에 자동으로 담는 converter
                                        .build();

                                AddressService retrofitAPI = retrofit.create(AddressService.class);

                                retrofitAPI.getAddress(query).enqueue(new Callback<AddressJson>() {
                                    // interface 에서 정의했던 메소드 중 하나를 선언하고, 비동기 통신을 실행한다.
                                    // 통신이 완료되었을 때 이벤트를 처리하기 위해 Callback 리스너도 함께 등록한다.
                                    @Override
                                    public void onResponse(Call<AddressJson> call, Response<AddressJson> response) {
                                        if (response.isSuccessful()) { // 원활하게 통신이 이뤄졌을 때
                                            Log.d("Location", "Response Success");
                                            AddressJson data = response.body(); // 응답 내용을 변수에 입력

                                            String addressLvl1 = data.getAddressLvl1().replace(" ", "-");
                                            String addressLvl2 = data.getAddressLvl2().replace(" ", "-");
                                            String addressLvl3 = data.getAddressLvl3().replace(" ", "-");
                                            String address = addressLvl1+"-"+addressLvl2+"-"+addressLvl3;
                                            if (data.getAddressLand() != null) address = address+"-"+data.getAddressLand();

                                            MainActivity.this.webView = webView;
                                            MainActivity.this.webView.loadUrl("javascript:setLocation('" + address + "')");
                                            // setUserAddress(address);
                                        } else { // 원활한 통신이 이뤄지지 않았을 때
                                            Log.d("Location","Response Fail");
                                            Toast.makeText(MainActivity.super.getApplicationContext(), "주소 변환 실패", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    // 통신 중 생각하지 못한 예외(네트워크 오류 등)가 발생되었을 때 호출된다.
                                    @Override
                                    public void onFailure(Call<AddressJson> call, Throwable t) {
                                        Toast.makeText(MainActivity.super.getApplicationContext(), "서버 통신 실패", Toast.LENGTH_SHORT).show();
                                        t.printStackTrace();
                                    }
                                });
                            }
                        });
            }
        }

        // return userAddress;
    }

    public void onClickEmailButtonTest(View view) {
        onClickSendEmail();
    }

    public void onClickSendEmail() {
        Intent emailSelectorIntent = new Intent(Intent.ACTION_SENDTO);
        emailSelectorIntent.setData(Uri.parse("mailto:"));

        Intent email = new Intent(Intent.ACTION_SEND);
        String[] address = {"recycle_dev_test@tmpbox.net"};
        email.putExtra(Intent.EXTRA_EMAIL, address);
        email.putExtra(Intent.EXTRA_SUBJECT, "[문의] Recycle 관련 문의");
        email.putExtra(Intent.EXTRA_TEXT, "문의 내용 :\n앱이 너무 구려요.");
        email.setSelector(emailSelectorIntent);

        startActivity(email);
    }

    public void onSaveSearchLog(String barcode, String name, String imageLink) {
        int duplicate_index = -1;

        SharedPreferences sharedPreferences = getSharedPreferences(KEY_SEARCH_LOG_FILE, MODE_PRIVATE);
        String stringSearchLog = sharedPreferences.getString(KEY_SEARCH_LOG, null);
        ArrayList<SearchLogJson> arrayListSearchLog = new ArrayList<>();

        if (stringSearchLog != null && !stringSearchLog.equals("[]")) {
            try {
                JSONArray jsonArray = new JSONArray(stringSearchLog);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String log = jsonArray.optString(i);
                    SearchLogJson searchLogJson = new Gson().fromJson(log, SearchLogJson.class);
                    if (searchLogJson.getItemBarcode().equals(barcode)) {
                        duplicate_index = i;
                    }
                    arrayListSearchLog.add(searchLogJson);
                }
            } catch (JSONException je) {
                je.printStackTrace();
            }
        }
        arrayListSearchLog.add(0, new SearchLogJson(name, barcode, imageLink));

        if (duplicate_index != -1) {
            arrayListSearchLog.remove(duplicate_index + 1);
        }

        stringSearchLog = new Gson().toJson(arrayListSearchLog);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SEARCH_LOG, stringSearchLog);
        editor.apply();
    }

    public String onLoadSearchLog() {
        SharedPreferences sharedPreferences = getSharedPreferences(KEY_SEARCH_LOG_FILE, MODE_PRIVATE);
        return sharedPreferences.getString(KEY_SEARCH_LOG, null);
    }

    public void onDeleteSearchLog(int index) {
        SharedPreferences sharedPreferences = getSharedPreferences(KEY_SEARCH_LOG_FILE, MODE_PRIVATE);
        String stringSearchLog = sharedPreferences.getString(KEY_SEARCH_LOG, null);
        ArrayList<SearchLogJson> arrayListSearchLog = new ArrayList<>();

        if (stringSearchLog != null && !stringSearchLog.equals("[]")) {
            try {
                JSONArray jsonArray = new JSONArray(stringSearchLog);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String log = jsonArray.optString(i);
                    SearchLogJson searchLogJson = new Gson().fromJson(log, SearchLogJson.class);
                    arrayListSearchLog.add(searchLogJson);
                }
                try {
                    arrayListSearchLog.remove(index);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stringSearchLog = new Gson().toJson(arrayListSearchLog);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_SEARCH_LOG, stringSearchLog);
                editor.apply();
            } catch (JSONException je) {
                je.printStackTrace();
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
        if (coarseLocationGranted && fineLocationGranted) {
            return true;
        } else {
            locationPermissionRequest.launch(new String[]{ android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION });
            Toast.makeText(MainActivity.this, "위치 권한 없음. 정상적인 서비스 이용을 위해 설정에서 허용해 주세요.", Toast.LENGTH_LONG).show();
            return false;
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
                        Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION,false);
                        if (fineLocationGranted != null && fineLocationGranted) {
                            // 정확한 위치 접근 허용됨
                        } else if (coarseLocationGranted != null && coarseLocationGranted) {
                            Toast.makeText(MainActivity.this, "대략적인 위치는 정확하지 않습니다. 정확한 위치 사용을 고려해주세요.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "위치 권한 없음. 정상적인 서비스 이용을 위해 설정에서 허용해 주세요.", Toast.LENGTH_LONG).show();
                        }
                    }
            );
}