package com.example.weather.client;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.weather.entity.PincodeGeo;
import com.example.weather.entity.Weather;
import com.example.weather.response.Description;
import com.example.weather.response.GeocodingApiResponse;
import com.example.weather.response.Temperature;
import com.example.weather.response.WeatherApiResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
public class OpenWeatherClientTest {

    private OpenWeatherClient subject;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() throws Exception {
        subject = new OpenWeatherClient(restTemplate, "http://localhost:8089", "someAppId");
    }

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private LocalDate getStubDate() {
        return LocalDate.parse("05-06-2023", dateTimeFormatter);
    }

    private double getStubLatitude() {
        return 0.00;
    }

    private double getStubLongitude() {
        return 0.00;
    }

    private int getStubPincode() {
        return 147001;
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
    private WeatherApiResponse getStubWeatherApiResponse() {
        WeatherApiResponse response = new WeatherApiResponse();
        String name = "City Name";
        Temperature temperature = new Temperature(123.00);
        Description description = new Description();
        description.setDescription("clear sky");
        List<Description> weather = new ArrayList<>();
        weather.add(description);

        response.setMain(temperature);
        response.setWeather(weather);
        response.setName(name);

        return response;
    }

    private GeocodingApiResponse getStubGeocodingApiResponse() {
        GeocodingApiResponse response = new GeocodingApiResponse();

        response.setLat(getStubLatitude());
        response.setLon(getStubLongitude());

        return response;
    }

    private Weather getStubWeather() {
        WeatherApiResponse apiResponse = this.getStubWeatherApiResponse();
        Weather weather = new Weather();
        weather.setDate(this.getStubDate());
        weather.setTemperature(apiResponse.getMain().getTemp());
        weather.setDescription(apiResponse.getWeather().get(0).getDescription());
        weather.setPlace(apiResponse.getName());
        return weather;
    }

    @Test
    public void fetchWeather_whenSuccessfulNetworkCall_thenReturnsWeatherObject() throws Exception {
        LocalDate date = this.getStubDate();
        double latitude = this.getStubLatitude();
        double longitude = this.getStubLongitude();
        WeatherApiResponse expectedResponse = this.getStubWeatherApiResponse();
        Weather expectedWeather = this.getStubWeather();

        long unixTimestamp = date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        String url = "http://localhost:8089/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid="
                + "someAppId" + "&dt=" + unixTimestamp;

        Mockito.when(restTemplate.getForEntity(url, WeatherApiResponse.class))
                .thenReturn(new ResponseEntity<WeatherApiResponse>(expectedResponse, HttpStatus.OK));

        var actualWeather = subject.fetchWeather(latitude, longitude, date);

        assertEquals(expectedWeather.getClass(), actualWeather.getClass());
        assertEquals(expectedWeather.getDate(), actualWeather.getDate());
        assertEquals(expectedWeather.getDescription(), actualWeather.getDescription());
        assertEquals(expectedWeather.getPlace(), actualWeather.getPlace());
        assertEquals(expectedWeather.getTemperature(), actualWeather.getTemperature());
    }

    @Test
    public void fetchWeather_whenHTTP4xx_thenThrowsRestClientException() throws Exception {
        LocalDate date = this.getStubDate();
        double latitude = this.getStubLatitude();
        double longitude = this.getStubLongitude();

        long unixTimestamp = date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        String url = "http://localhost:8089/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid="
                + "someAppId" + "&dt=" + unixTimestamp;

        Mockito.when(restTemplate.getForEntity(url, WeatherApiResponse.class))
                .thenReturn(new ResponseEntity<WeatherApiResponse>(HttpStatus.NOT_FOUND));
        
        Exception exception = assertThrows(RestClientException.class, () -> {
            subject.fetchWeather(latitude, longitude, date);
        });

        String expectedMessage = "Not Found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void fetchPincode_whenSuccessfulNetworkCall_thenReturnsPincodeGeoObject() throws Exception {
        int pincode = this.getStubPincode();
        double latitude = this.getStubLatitude();
        double longitude = this.getStubLongitude();
        GeocodingApiResponse expectedApiResponse = this.getStubGeocodingApiResponse(); 
        PincodeGeo expectedPincodeGeo = new PincodeGeo(pincode, latitude, longitude);
        String url = "http://localhost:8089/geo/1.0/zip?zip=" + pincode + ",in&appid=" + "someAppId";

        Mockito.when(restTemplate.getForEntity(url, GeocodingApiResponse.class))
                .thenReturn(new ResponseEntity<GeocodingApiResponse>(expectedApiResponse, HttpStatus.OK));
        
        var actualPincode = subject.fetchPincodeGeo(pincode);

        assertEquals(expectedPincodeGeo.getClass(), actualPincode.getClass());
        assertEquals(expectedPincodeGeo.getLatitude(), actualPincode.getLatitude());
        assertEquals(expectedPincodeGeo.getLongitude(), actualPincode.getLongitude());
        assertEquals(expectedPincodeGeo.getPincode(), actualPincode.getPincode());
    }

    @Test
    public void fetchPincode_whenHTTP4xx_thenThrowsRestClientException() throws Exception {
        int pincode = this.getStubPincode();

        String url = "http://localhost:8089/geo/1.0/zip?zip=" + pincode + ",in&appid=" + "someAppId";

        Mockito.when(restTemplate.getForEntity(url, GeocodingApiResponse.class))
                .thenReturn(new ResponseEntity<GeocodingApiResponse>(HttpStatus.NOT_FOUND));
        
        Exception exception = assertThrows(RestClientException.class, () -> {
            subject.fetchPincodeGeo(pincode);
        });

        String expectedMessage = "Not Found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
