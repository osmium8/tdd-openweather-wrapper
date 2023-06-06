package com.example.weather.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClientException;
import com.example.weather.client.OpenWeatherClient;
import com.example.weather.entity.PincodeGeo;
import com.example.weather.entity.Weather;
import com.example.weather.exception.PincodeNotFoundException;
import com.example.weather.exception.WeatherNotFoundException;
import com.example.weather.repository.PincodeRepo;
import com.example.weather.repository.WeatherRepo;

@SpringBootTest
public class WeatherServiceTest {
    
    @Mock
    private OpenWeatherClient weatherClient;

    @Mock
    private PincodeRepo pincodeRepo;

    @Mock
     private WeatherRepo weatherRepo;

    @InjectMocks
    private WeatherService weatherService;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private int getStubPincode() {
        return 147001;
    }

    private LocalDate getStubDate() {
        return LocalDate.parse("05-06-2023", dateTimeFormatter);
    }

    private PincodeGeo getStubPincodeGeo() {
        return new PincodeGeo(147001, 0, 0);
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

    @Test
    void getWeather_whenDataNotPersisted_thenNetworkCallAndReturnsWeather() throws Exception{
        int pincode = getStubPincode();
        LocalDate date = getStubDate();
        PincodeGeo pincodeGeo = getStubPincodeGeo();
        Weather expectedWeather = getStubWeather();

        Mockito.when(weatherRepo.findByPincodeAndDate(pincode, date)).thenReturn(Optional.empty());
        Mockito.when(pincodeRepo.findById(pincode)).thenReturn(Optional.empty());
        Mockito.when(weatherClient.fetchPincodeGeo(pincode)).thenReturn(pincodeGeo);
        Mockito.when(weatherClient.fetchWeather(pincodeGeo.getLatitude(), pincodeGeo.getLongitude(), date)).thenReturn(expectedWeather);
        
        Weather actualWeather = weatherService.getWeather(pincode, date);

        Assertions.assertEquals(expectedWeather.getPincode(), actualWeather.getPincode());
        Assertions.assertEquals(expectedWeather.getTemperature(), actualWeather.getTemperature());
        Assertions.assertEquals(expectedWeather.getDescription(), actualWeather.getDescription());
    }

    @Test
    void getWeather_whenInvalidPincode_thenThrowsPincodeNotFoundException() throws Exception{
        int invalidPincode = 1234567;
        LocalDate date = getStubDate();

        Mockito.when(weatherRepo.findByPincodeAndDate(invalidPincode, date)).thenReturn(Optional.empty());
        Mockito.when(pincodeRepo.findById(invalidPincode)).thenReturn(Optional.empty());
        Mockito.when(weatherClient.fetchPincodeGeo(invalidPincode)).thenThrow(RestClientException.class);
        
        Exception exception = assertThrows(PincodeNotFoundException.class, () -> {
            weatherService.getWeather(invalidPincode, date);
        });

        String expectedMessage = "Provided pincode is invalid.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void getWeather_whenInvalidPincode_thenThrowsWeatherNotFoundException() throws Exception{
        int pincode = getStubPincode();
        LocalDate date = getStubDate();
        PincodeGeo pincodeGeo = getStubPincodeGeo();

        Mockito.when(weatherRepo.findByPincodeAndDate(pincode, date)).thenReturn(Optional.empty());
        Mockito.when(pincodeRepo.findById(pincode)).thenReturn(Optional.empty());
        Mockito.when(weatherClient.fetchPincodeGeo(pincode)).thenReturn(pincodeGeo);
        Mockito.when(weatherClient.fetchWeather(pincodeGeo.getLatitude(), pincodeGeo.getLongitude(), date)).thenThrow(RestClientException.class);
        
        Exception exception = assertThrows(WeatherNotFoundException.class, () -> {
            weatherService.getWeather(pincode, date);
        });

        String expectedMessage = "Weather couldn't be fetched for the provided pincode or date.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void getWeather_whenDataPersisted_thenNoNetworkCallAndReturnsWeather() throws Exception{
        int pincode = getStubPincode();
        LocalDate date = getStubDate();
        Weather expectedWeather = getStubWeather();

        Mockito.when(weatherRepo.findByPincodeAndDate(pincode, date)).thenReturn(Optional.of(expectedWeather));
        
        Weather actualWeather = weatherService.getWeather(pincode, date);

        Assertions.assertEquals(expectedWeather.getPincode(), actualWeather.getPincode());
        Assertions.assertEquals(expectedWeather.getTemperature(), actualWeather.getTemperature());
        Assertions.assertEquals(expectedWeather.getDescription(), actualWeather.getDescription());
    }

    @Test
    void getWeather_whenOnlyPincodePersisted_thenNetworkCallForWeatherAndReturnsWeather() throws Exception{
        int pincode = getStubPincode();
        LocalDate date = getStubDate();
        PincodeGeo pincodeGeo = getStubPincodeGeo();
        Weather expectedWeather = getStubWeather();

        Mockito.when(weatherRepo.findByPincodeAndDate(pincode, date)).thenReturn(Optional.empty());
        Mockito.when(pincodeRepo.findById(pincode)).thenReturn(Optional.of(getStubPincodeGeo()));
        Mockito.when(weatherClient.fetchWeather(pincodeGeo.getLatitude(), pincodeGeo.getLongitude(), date)).thenReturn(expectedWeather);
        
        Weather actualWeather = weatherService.getWeather(pincode, date);

        Assertions.assertEquals(expectedWeather.getPincode(), actualWeather.getPincode());
        Assertions.assertEquals(expectedWeather.getTemperature(), actualWeather.getTemperature());
        Assertions.assertEquals(expectedWeather.getDescription(), actualWeather.getDescription());
    }
}
