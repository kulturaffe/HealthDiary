package com.example.healthdiary.dataTypes;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

/**
 * "plain old java object"-model for geocoding API-response JSON from openweathermap.org<br>
 * annotated for use with jackson <br>
 * thanks to: https://www.jsonschema2pojo.org/
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "local_names",
        "lat",
        "lon",
        "country",
        "state"
})
public class GeocodingResponsePOJO {

    @JsonProperty("name")
    private String name;
    @JsonProperty("local_names")
    private LocalNames localNames;
    @JsonProperty("lat")
    private double lat;
    @JsonProperty("lon")
    private double lon;
    @JsonProperty("country")
    private String country;
    @JsonProperty("state")
    private String state;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("local_names")
    public LocalNames getLocalNames() {
        return localNames;
    }

    @JsonProperty("local_names")
    public void setLocalNames(LocalNames localNames) {
        this.localNames = localNames;
    }

    @JsonProperty("lat")
    public double getLat() {
        return lat;
    }

    @JsonProperty("lat")
    public void setLat(double lat) {
        this.lat = lat;
    }

    @JsonProperty("lon")
    public double getLon() {
        return lon;
    }

    @JsonProperty("lon")
    public void setLon(double lon) {
        this.lon = lon;
    }

    @JsonProperty("country")
    public String getCountry() {
        return country;
    }

    @JsonProperty("country")
    public void setCountry(String country) {
        this.country = country;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "ascii",
            "de",
            "en",
            "feature_name"
    })
    public class LocalNames {
        @JsonProperty("ascii")
        private String ascii;
        @JsonProperty("de")
        private String de;
        @JsonProperty("en")
        private String en;
        @JsonProperty("feature_name")
        private String featureName;
        @JsonIgnore
        private Map<String, String> additionalProperties = new HashMap<String, String>();

        @JsonProperty("ascii")
        public String getAscii() {
            return ascii;
        }

        @JsonProperty("ascii")
        public void setAscii(String ascii) {
            this.ascii = ascii;
        }

        @JsonProperty("de")
        public String getDe() {
            return de;
        }

        @JsonProperty("de")
        public void setDe(String de) {
            this.de = de;
        }

        @JsonProperty("en")
        public String getEn() {
            return en;
        }

        @JsonProperty("en")
        public void setEn(String en) {
            this.en = en;
        }

        @JsonProperty("feature_name")
        public String getFeatureName() {
            return featureName;
        }

        @JsonProperty("feature_name")
        public void setFeatureName(String featureName) {
            this.featureName = featureName;
        }

        /**
         *
         * @return Map&lt;iso 639-1 language code, local name&gt;
         */
        @JsonAnyGetter
        public Map<String, String> getAdditionalLocalNames() {
            return this.additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalLocalName(String language, String name) {
            this.additionalProperties.put(language, name);
        }

    }
}