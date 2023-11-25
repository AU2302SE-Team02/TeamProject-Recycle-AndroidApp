package com.example.ajourecyclehelper;

import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

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

        /* local setting */
        this.webView.getSettings().setAllowFileAccess(true);
        this.webView.getSettings().setAllowContentAccess(true);
        //webView.loadUrl("file:///android_asset/test.html");*/

        /* Redirect 할 때 브라우저 열리는 것 방지 */
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        /* 메인UI url 로드 */
        webView.loadUrl("http://ec2-54-180-122-139.ap-northeast-2.compute.amazonaws.com:8080/");

        /* javascript interface 사용 */
        webView.addJavascriptInterface(new AndroidJSInterface(), "AndroidClientApp");
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
    }
}
