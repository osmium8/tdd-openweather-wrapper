package com.example.weather.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.example.weather.entity.Weather;
import com.example.weather.exception.PincodeNotFoundException;
import com.example.weather.service.WeatherService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = WeatherController.class)
public class WeatherControllerAPITest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

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
    public void getWeather_whenValidInput_thenReturnsResponse() throws Exception {
        int pincode = this.getStubPincode();
        LocalDate date = this.getStubDate();
        String formattedDate = date.format(dateTimeFormatter);
        Weather expectedWeather = this.getStubWeather();

        Mockito.when(weatherService.getWeather(pincode, date)).thenReturn(expectedWeather);

        RequestBuilder request = get("/api/v1/weather")
                .param("pincode", String.valueOf(pincode))
                .param("date", formattedDate);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(".pincode").value(expectedWeather.getPincode()))
                .andExpect(jsonPath(".temperature").value(expectedWeather.getTemperature()))
                .andExpect(jsonPath(".description").value(expectedWeather.getDescription()))
                .andExpect(jsonPath(".place").value(expectedWeather.getPlace()));

    }

    @Test
    public void getWeather_whenInvalidPincode_thenReturnsNotFound() throws Exception {
        int pincode = 411014;
        LocalDate date = LocalDate.parse("02-03-2023", dateTimeFormatter);
        String formattedDate = date.format(dateTimeFormatter);

        Mockito.when(weatherService.getWeather(pincode, date))
                .thenThrow(PincodeNotFoundException.class); /* TODO (test) */

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/weather")
                .param("pincode", String.valueOf(pincode))
                .param("date", formattedDate))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getWeather_whenInvalidDate_thenReturnsBadRequest() throws Exception {
        int pincode = 411014;
        String date = "02-99-2023";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/weather")
                .param("pincode", String.valueOf(pincode))
                .param("date", date))
                .andExpect(status().isBadRequest());
    }

}
