package com.example.healthdiary.dataHandling;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.healthdiary.R;
import com.example.healthdiary.dataTypes.GeocodingResponsePOJO;
import com.example.healthdiary.dataTypes.HealthDiaryLocation;
import com.example.healthdiary.dataTypes.TemperatureReading;
import com.example.healthdiary.dataTypes.WeatherResponseCurrentPOJO;
import com.example.healthdiary.dataTypes.WeatherResponseHistoricalPOJO;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Objects;
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

public class APICaller {
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final Context ctx;

    public APICaller(Context ctx){
        this.ctx = ctx;
    }

    /** this function of the api is actually deprecated, {@link #getCurrentWeatherFromCoord(double, double) getting from coordinates} is preferred.
     * use {@link #getDirectGeo(String)} to obtain coordinates from string query.
     * @see <a href="https://openweathermap.org/current#builtin">Openweathermap API documentation</a>
     */
    @Deprecated
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

    public CompletableFuture<TemperatureReading> getHistoricalWeatherForLocation(final HealthDiaryLocation loc, long timeStamp) {
        long dt = timeStamp/1000; // convert ms to s
        final HistoricalWeatherInterface weatherInterface = APIClient.getClient().create(HistoricalWeatherInterface.class);
        return CompletableFuture.supplyAsync(() -> {
            Call<WeatherResponseHistoricalPOJO> call = weatherInterface.historicalWeather(loc.getLat(), loc.getLon(), dt);
            return doMakeCallHistoric(call, loc.getName());
        }, exec);
    }

    public CompletableFuture<HealthDiaryLocation> getDirectGeo(String query) {
        try {
            query = URLEncoder.encode(query.trim(),"UTF-8"); // sanitising user input
        } catch (UnsupportedEncodingException e) {
            query = "";
        }
        final String q = query;
        final GeocodingInterface weatherInterface = APIClient.getClient().create(GeocodingInterface.class);
        return CompletableFuture.supplyAsync(() -> {
            Call<List<GeocodingResponsePOJO>> call = weatherInterface.directGeocoding(q);
            return doMakeCallDirectGeo(call, q);
        }, exec);
    }

    public CompletableFuture<HealthDiaryLocation> getReverseGeo(double lat, double lon) {
        final GeocodingInterface weatherInterface = APIClient.getClient().create(GeocodingInterface.class);
        return CompletableFuture.supplyAsync(() -> {
            Call<List<GeocodingResponsePOJO>> call = weatherInterface.reverseGeocoding(lat, lon);
            return doMakeCallReverseGeo(call, lat, lon);
        }, exec);
    }



    private HealthDiaryLocation doMakeCallDirectGeo(Call<List<GeocodingResponsePOJO>> call, String name) {
        try {
            Response<List<GeocodingResponsePOJO>> response = call.execute();
            List<GeocodingResponsePOJO> responseList = response.body();
            if(null == responseList) {
                Log.d(ctx.getString(R.string.log_tag),"No response from direct geo..");
                return new HealthDiaryLocation().setStatusInvalid();
            }
            Log.d(ctx.getString(R.string.log_tag),"Got direct geo response: "+ response.code()+ ": " + response.message());
            if (responseList.size() < 1 || null == responseList.get(0)){
                Log.d(ctx.getString(R.string.log_tag),String.format("No coordinates found from direct geo for %s..",name));
                HealthDiaryLocation l = new HealthDiaryLocation(name).setStatusInvalid();
                Log.d("MyTag",String.format("invalid location: %s",l));
                return l;
            }
            GeocodingResponsePOJO geoResponse = responseList.get(0);
            HealthDiaryLocation result = new HealthDiaryLocation(
                    geoResponse.getLat(),
                    geoResponse.getLon(),
                    null  != geoResponse.getName() ? geoResponse.getName() : "");
            Log.d(ctx.getString(R.string.log_tag),"Direct geo: " + result);
            return result;
        }
        catch (IOException | RuntimeException e) {
            e.printStackTrace();
            Log.d(ctx.getString(R.string.log_tag),"An error occurred during API-call for direct geo: " + e);
        }
        return null;
    }

    private HealthDiaryLocation doMakeCallReverseGeo(Call<List<GeocodingResponsePOJO>> call, double lat, double lon) {
        try {
            Response<List<GeocodingResponsePOJO>> response = call.execute();
            List<GeocodingResponsePOJO> responseList = response.body();
            if(null == responseList) {
                Log.d(ctx.getString(R.string.log_tag),"No response from reverse geo..");
                return new HealthDiaryLocation().setStatusInvalid();
            }
            Log.d(ctx.getString(R.string.log_tag),"Got reverse response: "+ response.code()+ ": " + response.message());
            if (null == responseList.get(0)){
                Log.d(ctx.getString(R.string.log_tag),String.format("No name found from reverse geo for %2f,%2f..",lat,lon));
                return new HealthDiaryLocation().setStatusInvalid();
            }
            GeocodingResponsePOJO geoResponse = responseList.get(0);
            HealthDiaryLocation result = new HealthDiaryLocation(
                    geoResponse.getLat(),
                    geoResponse.getLon(),
                    null  != geoResponse.getName() ? geoResponse.getName() : "");
            Log.d(ctx.getString(R.string.log_tag),"Reverse geo: " + result);
            return result;
        }
        catch (IOException | RuntimeException e) {
            e.printStackTrace();
            Log.d(ctx.getString(R.string.log_tag),"An error occurred during API-call for reverse geo: " + e);
        }
        return null;
    }

    private TemperatureReading doMakeCallHistoric(Call<WeatherResponseHistoricalPOJO> call, String name) {
        try {
            Response<WeatherResponseHistoricalPOJO> response = call.execute();
            WeatherResponseHistoricalPOJO wResponse = response.body();
            Log.d(ctx.getString(R.string.log_tag),"Got temperature response: "+ response.code()+ ": " + response.message());
            if(!response.isSuccessful()){
                Toast.makeText(ctx,String.format("An error occured: %s %s: %s",response.code(), response.message(), Objects.requireNonNull(response.errorBody()).string()),Toast.LENGTH_LONG).show();
                return null;
            }

            if (null != wResponse && null != wResponse.getData().get(0)){
                WeatherResponseHistoricalPOJO.Datum data = wResponse.getData().get(0);
                TemperatureReading result = new TemperatureReading(
                        data.getTemp(),
                        wResponse.getLat(),
                        wResponse.getLon(),
                        name,
                        data.getDt() * 1000L);
                Log.d(ctx.getString(R.string.log_tag),"Temp: " + result);
                return result;
            }
        }
        catch (IOException | RuntimeException e) {
            e.printStackTrace();
            Log.d(ctx.getString(R.string.log_tag),"An error occurred during API-call for historic temp: " + e);
        }
        return null;

    }

    private TemperatureReading doMakeCallCurrent(Call<WeatherResponseCurrentPOJO> call) {
        try {
            Response<WeatherResponseCurrentPOJO> response = call.execute();
            WeatherResponseCurrentPOJO wResponse = response.body();
            Log.d(ctx.getString(R.string.log_tag),"Got current temperature response: "+ response.code()+ ": " + response.message());
            if (null != wResponse){
                TemperatureReading result = new TemperatureReading(
                        wResponse.getMain().getTemp(),
                        wResponse.getCoord().getLat(),
                        wResponse.getCoord().getLon(),
                        wResponse.getName(),
                        wResponse.getDt() * 1000L);
                Log.d(ctx.getString(R.string.log_tag),"Temp: " + result);
                return result;
            }
        }
        catch (IOException | RuntimeException e) {
            e.printStackTrace();
            Log.d(ctx.getString(R.string.log_tag),"An error occurred during API-call for current temp: " + e);
        }
        return null;
    }

    interface CurrentWeatherInterface {
        @GET("/data/2.5/weather?appid=8d646f4322201de967c67292a18bdfd7&units=metric")
        Call<WeatherResponseCurrentPOJO> currentWeatherFromString(@Query("q") String place);

        @GET("/data/2.5/weather?appid=8d646f4322201de967c67292a18bdfd7&units=metric")
        Call<WeatherResponseCurrentPOJO> currentWeatherFromCoord(@Query("lat") double lat, @Query("lon") double lon);
    }

    interface GeocodingInterface {
        @GET("/geo/1.0/direct?appid=8d646f4322201de967c67292a18bdfd7&limit=1")
        Call<List<GeocodingResponsePOJO>> directGeocoding(@Query("q") String place);

        @GET("/geo/1.0/reverse?appid=8d646f4322201de967c67292a18bdfd7&limit=1")
        Call<List<GeocodingResponsePOJO>> reverseGeocoding(@Query("lat") double lat, @Query("lon") double lon);
    }

    interface HistoricalWeatherInterface {
        @GET("/data/3.0/onecall/timemachine?appid=8d646f4322201de967c67292a18bdfd7&units=metric")
        Call<WeatherResponseHistoricalPOJO> historicalWeather(@Query("lat") double lat, @Query("lon") double lon, @Query("dt") long unixEpochSeconds);
    }

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
