package api;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface Api {

    @FormUrlEncoded
    @POST("MobileCommonLoginController")
    Call<ResponseBody> submitData(
            @Field("longitude") String longitude,
            @Field("latitude") String latitude,
            @Field("spinner_data") String spinner_data
    );

}