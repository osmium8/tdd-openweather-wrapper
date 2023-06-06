package com.example.weather.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WeatherApiResponse {
    
    private List<Description> weather;
    private String base;
    private Temperature main;
    private int visibility;
    private long dt;
    private int timezone;
    private int id;
    private String name;
    private int cod;
}