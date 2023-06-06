package com.example.weather.client;

import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.weather.entity.PincodeGeo;
import com.example.weather.entity.Weather;
import com.example.weather.response.GeocodingApiResponse;
import com.example.weather.response.WeatherApiResponse;

@Component
public class OpenWeatherClient {

    private final RestTemplate restTemplate;
    private final String weatherServiceUrl;
    private final String weatherServiceApiKey;

    @Autowired
    public OpenWeatherClient(final RestTemplate restTemplate,
            @Value("${weather.url}") final String weatherServiceUrl,
            @Value("${weather.api_secret}") final String weatherServiceApiKey) {
        this.restTemplate = restTemplate;
        this.weatherServiceUrl = weatherServiceUrl;
        this.weatherServiceApiKey = weatherServiceApiKey;
    }

    public PincodeGeo fetchPincodeGeo(Integer pincode) throws RestClientException {
        String url = weatherServiceUrl + "/geo/1.0/zip?zip=" + pincode + ",in&appid=" + weatherServiceApiKey;

        ResponseEntity<GeocodingApiResponse> response = restTemplate.getForEntity(url, GeocodingApiResponse.class);

        double latitude = 0;
        double longitude = 0;

        if (response.getStatusCode().is2xxSuccessful()) {
            latitude = response.getBody().getLat();
            longitude = response.getBody().getLon();
            return new PincodeGeo(pincode, latitude, longitude);
        }

        throw new RestClientException(response.getStatusCode().getReasonPhrase());
    }

    public Weather fetchWeather(double latitude, double longitude, LocalDate date)
            throws RestClientException {

        long unixTimestamp = date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        String url = weatherServiceUrl + "/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid="
                + weatherServiceApiKey + "&dt=" + unixTimestamp;
        ResponseEntity<WeatherApiResponse> response = restTemplate.getForEntity(url, WeatherApiResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            Weather weather = new Weather();
            WeatherApiResponse apiResponse = response.getBody();
            if (apiResponse != null) {
                weather.setDate(date);
                weather.setTemperature(apiResponse.getMain().getTemp());
                weather.setDescription(apiResponse.getWeather().get(0).getDescription());
                weather.setPlace(apiResponse.getName());
            }
            return weather;
        }

        throw new RestClientException(response.getStatusCode().getReasonPhrase());
    }
}