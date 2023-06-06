package com.example.weather.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.weather.entity.Weather;

@Repository
public interface WeatherRepo extends JpaRepository<Weather,Long>{
   Optional<Weather> findByPincodeAndDate(Integer pincode, LocalDate date);
}
