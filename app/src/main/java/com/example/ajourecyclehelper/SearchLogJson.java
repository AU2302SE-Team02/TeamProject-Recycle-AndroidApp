package com.example.ajourecyclehelper;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SearchLogJson {
    private String itemName;
    private String itemBarcode;
    private String itemImageLink;
    private String itemSearchDate;

    public SearchLogJson(String itemName, String itemBarcode, String itemImageLink) {
        this.itemName = itemName;
        this.itemBarcode = itemBarcode;
        this.itemImageLink = itemImageLink;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN);
        String time = sdf.format(System.currentTimeMillis());
        this.itemSearchDate = time;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemBarcode() {
        return itemBarcode;
    }

    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    public String getItemImageLink() {
        return itemImageLink;
    }

    public void setItemImageLink(String itemImageLink) {
        this.itemImageLink = itemImageLink;
    }

    public String getItemSearchDate() {
        return itemSearchDate;
    }

    public void setItemSearchDate(String itemSearchDate) {
        this.itemSearchDate = itemSearchDate;
    }
}
