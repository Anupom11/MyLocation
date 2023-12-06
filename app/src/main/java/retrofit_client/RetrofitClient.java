package retrofit_client;

import android.util.Log;

import api.Api;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private String baseURL = "http://192.168.4.12:8080/MERAMS/";

    private static RetrofitClient retrofitClient;
    public Retrofit retrofit;

    // --- connecting the retrofit client with the http url ---
    public RetrofitClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // --- creating the instance for the Retrofit client in the synchronized way ---
    public static synchronized RetrofitClient getInstance() {
        if(retrofitClient == null) {
            retrofitClient = new RetrofitClient();
        }
        return retrofitClient;
    }

    // creating object for the api interface
    public Api getApi() {
        return retrofit.create(Api.class);
    }
}