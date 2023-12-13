package com.example.ajourecyclehelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface AddressService {

    String ADDRESS_SERVER_URL = "http://ec2-54-180-122-139.ap-northeast-2.compute.amazonaws.com:8080/";

    // URL의 일부분이 동적 데이터에 의해 결정될 때
    @GET("api/rgc")
    Call<AddressJson> getAddress(
            @QueryMap Map<String, String> location
    );
}
