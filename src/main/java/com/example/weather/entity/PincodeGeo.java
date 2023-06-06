package com.example.weather.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "pincode_geo")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PincodeGeo {
    
    @Id
    @Column(name = "pincode", nullable = false)
    private Integer pincode;

    private double latitude;
    private double longitude;

}
