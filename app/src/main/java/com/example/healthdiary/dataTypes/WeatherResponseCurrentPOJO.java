package com.example.healthdiary.dataTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * "plain old java object"-model for the weather 2.5 API-response JSON from openweathermap.org<br>
 * annotated for use with jackson <br>
 * thanks to: https://www.jsonschema2pojo.org/
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "coord",
        "weather",
        "base",
        "main",
        "visibility",
        "wind",
        "clouds",
        "dt",
        "sys",
        "timezone",
        "id",
        "name",
        "cod"
})
public class WeatherResponseCurrentPOJO {
    @JsonProperty("coord")
    private Coord coord;
    @JsonProperty("coord")
    public Coord getCoord() {
        return coord;
    }
    @JsonProperty("weather")
    private List<Weather> weather;
    @JsonProperty("weather")
    public List<Weather> getWeather() {
        return weather;
    }
    @JsonProperty("base")
    private String base;
    @JsonProperty("base")
    public String getBase() {
        return base;
    }
    @JsonProperty("main")
    private Main main;
    @JsonProperty("main")
    public Main getMain() {
        return main;
    }
    @JsonProperty("visibility")
    private int visibility;
    @JsonProperty("visibility")
    public int getVisibility() {
        return visibility;
    }
    @JsonProperty("wind")
    private Wind wind;
    @JsonProperty("wind")
    public Wind getWind() {
        return wind;
    }
    @JsonProperty("clouds")
    private Clouds clouds;
    @JsonProperty("clouds")
    public Clouds getClouds() {
        return clouds;
    }
    @JsonProperty("dt")
    private long dt;
    @JsonProperty("dt")
    public long getDt() {
        return dt;
    }
    @JsonProperty("sys")
    private Sys sys;
    @JsonProperty("sys")
    public Sys getSys() {
        return sys;
    }
    @JsonProperty("timezone")
    private int timezone;
    @JsonProperty("timezone")
    public int getTimezone() {
        return timezone;
    }
    @JsonProperty("id")
    private int id;
    @JsonProperty("id")
    public int getId() {
        return id;
    }
    @JsonProperty("name")
    private String name;
    @JsonProperty("name")
    public String getName() {
        return name;
    }
    @JsonProperty("cod")
    private int cod;
    @JsonProperty("cod")
    public int getCod() {
        return cod;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "all"
    })
    public static class Clouds {
        @JsonProperty("all")
        private int all;
        @JsonProperty("all")
        public int getAll() {
            return all;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder({
            "lon",
            "lat"
    })
    public static class Coord {
        @JsonProperty("lon")
        private double lon;
        @JsonProperty("lon")
        public double getLon() {
            return lon;
        }
        @JsonProperty("lat")
        public double lat;
        @JsonProperty("lat")
        public double getLat() {
            return lat;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder({
            "temp",
            "feels_like",
            "temp_min",
            "temp_max",
            "pressure",
            "humidity"
    })
    public static class Main {
        @JsonProperty("temp")
        private double temp;
        @JsonProperty("temp")
        public double getTemp() {
            return temp;
        }
        @JsonProperty("feels_like")
        private double feelsLike;
        @JsonProperty("feels_like")
        public double getFeelsLike() {
            return feelsLike;
        }
        @JsonProperty("temp_min")
        private double tempMin;
        @JsonProperty("temp_min")
        public double getTempMin() {
            return tempMin;
        }
        @JsonProperty("temp_max")
        private double tempMax;
        @JsonProperty("temp_max")
        public double getTempMax() {
            return tempMax;
        }
        @JsonProperty("pressure")
        private int pressure;
        @JsonProperty("pressure")
        public int getPressure() {
            return pressure;
        }
        @JsonProperty("humidity")
        private int humidity;
        @JsonProperty("humidity")
        public int getHumidity() {
            return humidity;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "type",
            "id",
            "country",
            "sunrise",
            "sunset"
    })
    public static class Sys {
        @JsonProperty("type")
        private int type;
        @JsonProperty("type")
        public int getType() {
            return type;
        }
        @JsonProperty("id")
        private int id;
        @JsonProperty("id")
        public int getId() {
            return id;
        }
        @JsonProperty("country")
        private String country;
        @JsonProperty("country")
        public String getCountry() {
            return country;
        }
        @JsonProperty("sunrise")
        private int sunrise;
        @JsonProperty("sunrise")
        public int getSunrise() {
            return sunrise;
        }
        @JsonProperty("sunset")
        private int sunset;
        @JsonProperty("sunset")
        public int getSunset() {
            return sunset;
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
        @JsonProperty("id")
        public int getId() {
            return id;
        }
        @JsonProperty("main")
        private String main;
        @JsonProperty("main")
        public String getMain() {
            return main;
        }
        @JsonProperty("description")
        private String description;
        @JsonProperty("description")
        public String getDescription() {
            return description;
        }
        @JsonProperty("icon")
        private String icon;
        @JsonProperty("icon")
        public String getIcon() {
            return icon;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "speed",
            "deg",
            "gust"
    })
    public static class Wind {
        @JsonProperty("speed")
        private double speed;
        @JsonProperty("speed")
        public double getSpeed() {
            return speed;
        }
        @JsonProperty("deg")
        private int deg;
        @JsonProperty("deg")
        public int getDeg() {
            return deg;
        }
        @JsonProperty("gust")
        private double gust;
        @JsonProperty("gust")
        public double getGust() {
            return gust;
        }
    }
}