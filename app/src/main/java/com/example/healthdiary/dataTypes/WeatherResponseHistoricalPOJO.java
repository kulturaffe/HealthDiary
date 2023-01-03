package com.example.healthdiary.dataTypes;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * "plain old java object"-model for the one call 3.0 history API-response JSON from openweathermap.org<br>
 * annotated for use with jackson <br>
 * thanks to: https://www.jsonschema2pojo.org/
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "lat",
        "lon",
        "timezone",
        "timezone_offset",
        "data"
})
public class WeatherResponseHistoricalPOJO {
    @JsonProperty("lat")
    private double lat;
    @JsonProperty("lon")
    private double lon;
    @JsonProperty("timezone")
    private String timezone;
    @JsonProperty("timezone_offset")
    private int timezoneOffset;
    @JsonProperty("data")
    private List<Datum> data;

    @JsonProperty("lat")
    public double getLat() {
        return lat;
    }
    @JsonProperty("lon")
    public double getLon() {
        return lon;
    }
    @JsonProperty("timezone")
    public String getTimezone() {
        return timezone;
    }
    @JsonProperty("timezone_offset")
    public int getTimezoneOffset() {
        return timezoneOffset;
    }
    @JsonProperty("data")
    public List<Datum> getData() {
        return data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "dt",
            "sunrise",
            "sunset",
            "temp",
            "feels_like",
            "pressure",
            "humidity",
            "dew_point",
            "uvi",
            "clouds",
            "visibility",
            "wind_speed",
            "wind_deg",
            "weather"
    })
    public static class Datum {
        @JsonProperty("dt")
        private int dt;
        @JsonProperty("sunrise")
        private int sunrise;
        @JsonProperty("sunset")
        private int sunset;
        @JsonProperty("temp")
        private double temp;
        @JsonProperty("feels_like")
        private double feelsLike;
        @JsonProperty("pressure")
        private int pressure;
        @JsonProperty("humidity")
        private int humidity;
        @JsonProperty("dew_point")
        private double dewPoint;
        @JsonProperty("uvi")
        private double uvi;
        @JsonProperty("clouds")
        private int clouds;
        @JsonProperty("visibility")
        private int visibility;
        @JsonProperty("wind_speed")
        private double windSpeed;
        @JsonProperty("wind_deg")
        private int windDeg;
        @JsonProperty("weather")
        private List<Weather> weather;

        @JsonProperty("dt")
        public int getDt() {
            return dt;
        }
        @JsonProperty("sunrise")
        public int getSunrise() {
            return sunrise;
        }
        @JsonProperty("sunset")
        public int getSunset() {
            return sunset;
        }
        @JsonProperty("temp")
        public double getTemp() {
            return temp;
        }
        @JsonProperty("feels_like")
        public double getFeelsLike() {
            return feelsLike;
        }
        @JsonProperty("pressure")
        public int getPressure() {
            return pressure;
        }
        @JsonProperty("humidity")
        public int getHumidity() {
            return humidity;
        }
        @JsonProperty("dew_point")
        public double getDewPoint() {
            return dewPoint;
        }
        @JsonProperty("uvi")
        public double getUvi() {
            return uvi;
        }
        @JsonProperty("clouds")
        public int getClouds() {
            return clouds;
        }
        @JsonProperty("visibility")
        public int getVisibility() {
            return visibility;
        }
        @JsonProperty("wind_speed")
        public double getWindSpeed() {
            return windSpeed;
        }
        @JsonProperty("wind_deg")
        public int getWindDeg() {
            return windDeg;
        }
        @JsonProperty("weather")
        public List<Weather> getWeather() {
            return weather;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "id",
            "main",
            "description",
            "icon"
    })
    public static class Weather {
        @JsonProperty("id")
        private int id;
        @JsonProperty("main")
        private String main;
        @JsonProperty("description")
        private String description;
        @JsonProperty("icon")
        private String icon;

        @JsonProperty("id")
        public int getId() {
            return id;
        }
        @JsonProperty("main")
        public String getMain() {
            return main;
        }
        @JsonProperty("description")
        public String getDescription() {
            return description;
        }
        @JsonProperty("icon")
        public String getIcon() {
            return icon;
        }
    }
}