package com.example.weather.service;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import com.example.weather.client.OpenWeatherClient;
import com.example.weather.entity.PincodeGeo;
import com.example.weather.entity.Weather;
import com.example.weather.exception.PincodeNotFoundException;
import com.example.weather.exception.WeatherNotFoundException;
import com.example.weather.repository.PincodeRepo;
import com.example.weather.repository.WeatherRepo;

@Service
public class WeatherService {

    private final WeatherRepo weatherRepo;
    private final PincodeRepo pincodeGeoRepo;
    private final OpenWeatherClient weatherClient;

    public WeatherService(final WeatherRepo weatherRepo, final PincodeRepo pincodeRepo,
            final OpenWeatherClient openWeatherClient) {
        this.weatherRepo = weatherRepo;
        this.pincodeGeoRepo = pincodeRepo;
        this.weatherClient = openWeatherClient;
    }

    @Cacheable(value = "weather", key = "#pincode+ '_' + #date")
    public Weather getWeather(Integer pincode, LocalDate date) throws PincodeNotFoundException, WeatherNotFoundException {

        Optional<Weather> optionalWeatherInfo = this.weatherRepo.findByPincodeAndDate(pincode, date);
        if (optionalWeatherInfo.isPresent()) {
            // persisted
            return optionalWeatherInfo.get();
        }

        double latitude;
        double longitude;

        Optional<PincodeGeo> optionalPincodeLocation = this.pincodeGeoRepo.findById(pincode);

        if (optionalPincodeLocation.isPresent()) {
            latitude = optionalPincodeLocation.get().getLatitude();
            longitude = optionalPincodeLocation.get().getLongitude();
        } else {
            try {
                // network call
                PincodeGeo pincodeGeo = weatherClient.fetchPincodeGeo(pincode);
                latitude = pincodeGeo.getLatitude();
                longitude = pincodeGeo.getLongitude();

                // persist
                this.pincodeGeoRepo.save(pincodeGeo);
            } catch (RestClientException e) {
                throw new PincodeNotFoundException("Provided pincode is invalid.");
            }
        }

        try {
            Weather weather = this.weatherClient.fetchWeather(latitude, longitude, date);
            
            // persist
            weather.setPincode(pincode);
            this.weatherRepo.save(weather);

            return weather;
        }  catch (RestClientException e) {
            throw new WeatherNotFoundException("Weather couldn't be fetched for the provided pincode or date.");
        }
    }

}
