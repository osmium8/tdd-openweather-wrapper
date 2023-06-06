package com.example.weather.repository;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.weather.entity.PincodeGeo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
// @DataJpaTest
public class PincodeRepoTest {

    @Autowired
    private PincodeRepo subject;

    
    @AfterEach
    public void tearDown() throws Exception {
        subject.deleteAll();
    }
    
    @Test
    public void shouldSaveAndFetchPincodeGeo() {
        PincodeGeo pincodeGeo = this.getStubPincodeGeo();
        
        subject.save(pincodeGeo);
        Optional<PincodeGeo> actualPincodeGeo = subject.findById(this.getStubPincode());

        assertTrue(actualPincodeGeo.isPresent());
        assertEquals(actualPincodeGeo.get().getLatitude(), pincodeGeo.getLatitude());
        assertEquals(actualPincodeGeo.get().getLongitude(), pincodeGeo.getLongitude());
    }

    private int getStubPincode() {
        return 123456;
    }

    private PincodeGeo getStubPincodeGeo() {
        PincodeGeo pincodeGeo = new PincodeGeo();
        pincodeGeo.setLatitude(0);
        pincodeGeo.setLongitude(0);
        pincodeGeo.setPincode(this.getStubPincode());

        return pincodeGeo;
    }
    
}
