package com.example.weather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.weather.entity.PincodeGeo;

@Repository
public interface PincodeRepo extends JpaRepository<PincodeGeo,Integer> {

}
