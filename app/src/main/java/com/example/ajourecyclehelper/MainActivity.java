package com.example.ajourecyclehelper;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.*;
import com.google.mlkit.vision.common.InputImage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                            this.webView.loadUrl("javascript:setBarcode('" + barcode + "')");
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
                                            webView.loadUrl("javascript:setBarcode('" + barcode + "')");
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
}