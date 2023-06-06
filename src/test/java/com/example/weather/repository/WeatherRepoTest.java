package com.example.weather.repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.weather.entity.Weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class WeatherRepoTest {

    @Autowired
    private WeatherRepo subject;

    
    @AfterEach
    public void tearDown() throws Exception {
        subject.deleteAll();
    }
    
    @Test
    public void shouldSaveAndFetchWeatherByPincodeAndDate() {
        Weather expectedWeather = this.getStubWeather();
        
        subject.save(expectedWeather);
        Optional<Weather> actualWeather = subject.findByPincodeAndDate(this.getStubPincode(), this.getStubDate());

        assertTrue(actualWeather.isPresent());
        assertEquals(actualWeather.get().getDate(), expectedWeather.getDate());
        assertEquals(actualWeather.get().getDescription(), expectedWeather.getDescription());
    }

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private LocalDate getStubDate() {
        return LocalDate.parse("05-06-2023", dateTimeFormatter);
    }

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
        return 123456;
    }
    
}
