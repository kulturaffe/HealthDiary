package com.example.healthdiary.dataHandling;

import android.content.Context;
import android.util.Log;

import com.example.healthdiary.R;
import com.example.healthdiary.dataTypes.GeocodingResponsePOJO;
import com.example.healthdiary.dataTypes.TemperatureReading;
import com.example.healthdiary.dataTypes.WeatherResponseCurrentPOJO;
import com.example.healthdiary.dataTypes.WeatherResponseHistoricalPOJO;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * I could not figure out how to return a CompletableFuture with another return-type than Void (supplyAsync() instead of runAsync()-method)
 * with Volley, therefore I used Retrofit2 with OKHttp3 and Jackson
 */
public class APICaller {
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final Context ctx;

    public APICaller(Context ctx){ this.ctx = ctx; }

    public CompletableFuture<TemperatureReading> getCurrentWeatherFromStr(final String place) {
        final CurrentWeatherInterface weatherInterface = APIClient.getClient().create(CurrentWeatherInterface.class);
        return CompletableFuture.supplyAsync(() -> {
            Call<WeatherResponseCurrentPOJO> call = weatherInterface.currentWeatherFromString(place);
            return doMakeCallCurrent(call);
        }, exec);
    }

    public CompletableFuture<TemperatureReading> getCurrentWeatherFromCoord(final double lat, final double lon) {
        final CurrentWeatherInterface weatherInterface = APIClient.getClient().create(CurrentWeatherInterface.class);
        return CompletableFuture.supplyAsync(() -> {
            Call<WeatherResponseCurrentPOJO> call = weatherInterface.currentWeatherFromCoord(lat, lon);
            return doMakeCallCurrent(call);
        }, exec);
    }

    public CompletableFuture<TemperatureReading> getHistoricalWeatherFromCoord(final double lat, final double lon, long timeStamp) {
        long dt = timeStamp/1000; // convert ms to s
        final HistoricalWeatherInterface weatherInterface = APIClient.getClient().create(HistoricalWeatherInterface.class);
        return CompletableFuture.supplyAsync(() -> {
            Call<WeatherResponseHistoricalPOJO> call = weatherInterface.historicalWeather(lat, lon, dt);
            return doMakeCallHistoric(call);
        }, exec);
    }

    // TODO
    private TemperatureReading doMakeCallHistoric(Call<WeatherResponseHistoricalPOJO> call) {
        return new TemperatureReading(Double.NaN);
    }

    private TemperatureReading doMakeCallCurrent(Call<WeatherResponseCurrentPOJO> call) {
        try {
            Response<WeatherResponseCurrentPOJO> response = call.execute();
            WeatherResponseCurrentPOJO wResponse = response.body();
            Log.d(ctx.getString(R.string.log_tag),"Got api response: "+ response.code()+ ": " + response.message());
            if (null != wResponse){
                TemperatureReading result = new TemperatureReading(
                        wResponse.getMain().getTemp(),
                        wResponse.getCoord().getLat(),
                        wResponse.getCoord().getLon(),
                        wResponse.getName(),
                        wResponse.getDt() * 1000); // *1000 because ts in db is in ms but API provides s
                Log.d(ctx.getString(R.string.log_tag),"Temp: " + result);
                return result;
            }
        }
        catch (IOException | RuntimeException e) {
            e.printStackTrace();
            Log.d(ctx.getString(R.string.log_tag),"An error occurred during API-call: " + e);
        }
        return null;
    }

    /**
     * nested interface to provide HTTP-methods for current weather api
     */
    interface CurrentWeatherInterface {
        @GET("/data/2.5/weather?appid=8d646f4322201de967c67292a18bdfd7&units=metric")
        Call<WeatherResponseCurrentPOJO> currentWeatherFromString(@Query("q") String place);

        @GET("/data/2.5/weather?appid=8d646f4322201de967c67292a18bdfd7&units=metric")
        Call<WeatherResponseCurrentPOJO> currentWeatherFromCoord(@Query("lat") double lat, @Query("lon") double lon);
    }

    /**
     * nested interface to provide HTTP-methods for current weather api
     */
    interface GeocodingInterface {
        @GET("/geo/1.0/direct?appid=8d646f4322201de967c67292a18bdfd7&limit=5")
        Call<GeocodingResponsePOJO> directGeocoding(@Query("q") String place);

        @GET("/geo/1.0/reverse?appid=8d646f4322201de967c67292a18bdfd7&limit=5")
        Call<GeocodingResponsePOJO> reverseGeocoding(@Query("lat") double lat, @Query("lon") double lon);
    }

    /**
     * nested interface to provide HTTP-methods for historical weather api
     */
    interface HistoricalWeatherInterface {
        @GET("/data/3.0/onecall/timemachine?appid=8d646f4322201de967c67292a18bdfd7&units=metric")
        Call<WeatherResponseHistoricalPOJO> historicalWeather(@Query("lat") double lat, @Query("lon") double lon, @Query("dt") long unixEpochSeconds);
    }

    /**
     * inner class to return client
     */
     static class APIClient{
        public static Retrofit getClient(){
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.level(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient().newBuilder().addInterceptor(interceptor).build();
            return new Retrofit.Builder().baseUrl("https://api.openweathermap.org")
                    .addConverterFactory(JacksonConverterFactory.create())
                    .client(client).build();
        }
    }
}
