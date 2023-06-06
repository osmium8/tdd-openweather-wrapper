package com.example.weather.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.example.weather.entity.Weather;
import com.example.weather.exception.PincodeNotFoundException;
import com.example.weather.exception.WeatherNotFoundException;
import com.example.weather.service.WeatherService;

@SpringBootTest
public class WeatherControllerTest {

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private WeatherController weatherController;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    /**
     * {
     * "pincode": 147001,
     * "place": "Nābha",
     * "date": "2023-06-05",
     * "temperature": 313.48,
     * "description": "clear sky"
     * }
     */
    private Weather getStubWeather() {
        Weather stubWeather = new Weather();
        stubWeather.setPincode(this.getStubPincode());
        stubWeather.setPlace("Nābha");
        stubWeather.setDate(this.getStubDate());
        stubWeather.setTemperature(313.48);
        stubWeather.setDescription("clear sky");
        return stubWeather;
    }

    private int getStubPincode() {
        return 147001;
    }

    private LocalDate getStubDate() {
        return LocalDate.parse("05-06-2023", dateTimeFormatter);
    }
    
    @Test
    void getWeather_whenValidInput_thenReturnWeather() throws Exception {
        int pincode = this.getStubPincode();
        LocalDate date = this.getStubDate();
        Weather expectedWeather = getStubWeather();
        Mockito.when(weatherService.getWeather(pincode, date)).thenReturn(expectedWeather);
        
        ResponseEntity<Weather> actualWeatherResponse = weatherController.getWeather(pincode, date);
        
        Assertions.assertEquals(HttpStatus.OK, actualWeatherResponse.getStatusCode());
        Assertions.assertEquals(expectedWeather.getTemperature(), actualWeatherResponse.getBody().getTemperature());
        verify(weatherService, Mockito.times(1)).getWeather(pincode, date);
    }

    @Test
    void getWeather_whenServiceReturnsPincodeNotFoundException_thenThrowsResponseStatusException() throws Exception {
        int pincode = this.getStubPincode();
        LocalDate date = this.getStubDate();
        Mockito.when(weatherService.getWeather(pincode, date)).thenThrow(PincodeNotFoundException.class);
        
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            weatherController.getWeather(pincode, date);
        });

        String expectedMessage = "404 NOT_FOUND";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void getWeather_whenServiceReturnsWeatherNotFoundException_thenThrowsResponseStatusException() throws Exception {
        int pincode = this.getStubPincode();
        LocalDate date = this.getStubDate();
        Mockito.when(weatherService.getWeather(pincode, date)).thenThrow(WeatherNotFoundException.class);
        
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            weatherController.getWeather(pincode, date);
        });

        String expectedMessage = "404 NOT_FOUND";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
