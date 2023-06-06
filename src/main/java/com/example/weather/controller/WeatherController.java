package com.example.weather.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.example.weather.entity.Weather;
import com.example.weather.exception.PincodeNotFoundException;
import com.example.weather.exception.WeatherNotFoundException;
import com.example.weather.service.WeatherService;

@RestController
@RequestMapping(value = "/api/v1", headers = "Accept=application/json")
public class WeatherController {

    @Autowired
    WeatherService weatherService;

    @GetMapping("/weather")
    public ResponseEntity<Weather> getWeather(
            @RequestParam Integer pincode,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate date) {

        Weather weather = null;

        try {
            weather = this.weatherService.getWeather(pincode, date);
            return ResponseEntity.ok(weather);
        }
        catch (PincodeNotFoundException | WeatherNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            // return ResponseEntity
            //         .status(HttpStatus.NOT_FOUND)
            //         .build();
        }
        
    }

}
