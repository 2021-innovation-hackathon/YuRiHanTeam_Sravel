package com.example.sravel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface RetrofitAPI {
    @POST("sm")
    Call<ImageModelVO> getImageList(@Body ImageModelPostVO imageModelPostVO);
}
