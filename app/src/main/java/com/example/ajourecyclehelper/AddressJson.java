package com.example.ajourecyclehelper;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AddressJson {

    @SerializedName("latitude")
    @Expose
    private String latitude;
    @SerializedName("longitude")
    @Expose
    private String longitude;
    @SerializedName("addressLand")
    @Expose
    private String addressLand;
    @SerializedName("addressLvl2")
    @Expose
    private String addressLvl2;
    @SerializedName("addressLvl0")
    @Expose
    private String addressLvl0;
    @SerializedName("addressLvl3")
    @Expose
    private String addressLvl3;
    @SerializedName("addressLvl1")
    @Expose
    private String addressLvl1;

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAddressLand() {
        return addressLand;
    }

    public void setAddressLand(String addressLand) {
        this.addressLand = addressLand;
    }

    public String getAddressLvl2() {
        return addressLvl2;
    }

    public void setAddressLvl2(String addressLvl2) {
        this.addressLvl2 = addressLvl2;
    }

    public String getAddressLvl0() {
        return addressLvl0;
    }

    public void setAddressLvl0(String addressLvl0) {
        this.addressLvl0 = addressLvl0;
    }

    public String getAddressLvl3() {
        return addressLvl3;
    }

    public void setAddressLvl3(String addressLvl3) {
        this.addressLvl3 = addressLvl3;
    }

    public String getAddressLvl1() {
        return addressLvl1;
    }

    public void setAddressLvl1(String addressLvl1) {
        this.addressLvl1 = addressLvl1;
    }

}