package com.example.ajourecyclehelper;

import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class RecycleWebView extends WebView {
    private MainActivity mainActivity;
    private WebView webView;

    public RecycleWebView(MainActivity mActivity) {
        super(mActivity);
        mainActivity = mActivity;
        this.webView = mainActivity.findViewById(R.id.webView);
        setWebSetting();
    }

    /* WebView 표시 관련 설정 */
    public void setWebSetting() {
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        this.webView.getSettings().setUseWideViewPort(true);
        this.webView.getSettings().setLoadWithOverviewMode(true);
        this.webView.getSettings().setSupportMultipleWindows(true);
        this.webView.getSettings().setDomStorageEnabled(true);
        this.webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        /* local setting */
        this.webView.getSettings().setAllowFileAccess(true);
        this.webView.getSettings().setAllowContentAccess(true);
        //webView.loadUrl("file:///android_asset/test.html");*/

        /* Redirect 할 때 브라우저 열리는 것 방지 */
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient(){
            public boolean onConsoleMessage(ConsoleMessage message){
                Log.d("WebViewConsoleLog", "message: " + message.message());
                return true;
            }
        });

        /* javascript interface 사용 */
        webView.addJavascriptInterface(new AndroidJSInterface(), "AndroidClientApp");
        /* 메인UI url 로드 */
        webView.loadUrl("http://ec2-54-180-122-139.ap-northeast-2.compute.amazonaws.com:8080/");
        //webView.loadUrl("https://bc3e-202-30-23-225.ngrok-free.app");
    }

    private class AndroidJSInterface {
        public AndroidJSInterface() { }

        @JavascriptInterface
        public void onClickCameraButton() {
            mainActivity.onClickBarcodeCamera(webView);
        }
        @JavascriptInterface
        public void onClickGalleryButton() {
            mainActivity.onClickBarcodeGallery(webView);
        }
        @JavascriptInterface
        public void onClickLocationButton(boolean isClick) {
            mainActivity.onClickLocateGPS(webView, isClick);
        }
        @JavascriptInterface
        public void onClickEmailButton() {
            mainActivity.onClickSendEmail();
        }
        @JavascriptInterface
        public void onClickSaveLog(String name, String barcode, String imageLink) {
            mainActivity.onSaveSearchLog(name, barcode, imageLink);
        }
        @JavascriptInterface
        public String onClickLoadLog() { return mainActivity.onLoadSearchLog(); }
        @JavascriptInterface
        public void onClickDeleteLog(int index) { mainActivity.onDeleteSearchLog(index); }
    }
}
