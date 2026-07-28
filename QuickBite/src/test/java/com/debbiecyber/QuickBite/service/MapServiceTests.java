package com.debbiecyber.QuickBite.service;

import com.debbiecyber.QuickBite.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MapServiceTests {

    @Test
    void readsNominatimLatAndLonFields() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        MapService mapService = new MapService(restTemplate);

        server.expect(requestTo("https://nominatim.openstreetmap.org/search?q=Ikeja&format=json&limit=1"))
                .andRespond(withSuccess("[{\"lat\":\"6.6018\",\"lon\":\"3.3515\"}]", null));

        assertArrayEquals(new double[]{6.6018, 3.3515}, mapService.getCoordinates("Ikeja"));
        server.verify();
    }

    @Test
    void usesBothLongitudeValuesWhenCalculatingDeliveryFee() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        MapService mapService = new MapService(restTemplate);
        configureFeeTiers(mapService);

        server.expect(requestTo("https://nominatim.openstreetmap.org/search?q=Restaurant&format=json&limit=1"))
                .andRespond(withSuccess("[{\"lat\":\"6.6018\",\"lon\":\"3.3515\"}]", null));
        server.expect(requestTo("https://nominatim.openstreetmap.org/search?q=Customer&format=json&limit=1"))
                .andRespond(withSuccess("[{\"lat\":\"6.6018\",\"lon\":\"3.3515\"}]", null));

        assertEquals(500.0, mapService.calculateDeliveryFee("Restaurant", "Customer"));
        server.verify();
    }

    @Test
    void rejectsAnAddressThatCannotBeLocated() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        MapService mapService = new MapService(restTemplate);

        server.expect(requestTo("https://nominatim.openstreetmap.org/search?q=Unknown&format=json&limit=1"))
                .andRespond(withSuccess("[]", null));

        assertThrows(BadRequestException.class, () -> mapService.getCoordinates("Unknown"));
        server.verify();
    }

    private void configureFeeTiers(MapService mapService) {
        ReflectionTestUtils.setField(mapService, "tier1MaxKm", 3.0);
        ReflectionTestUtils.setField(mapService, "tier1Amount", 500.0);
        ReflectionTestUtils.setField(mapService, "tier2MaxKm", 7.0);
        ReflectionTestUtils.setField(mapService, "tier2Amount", 800.0);
        ReflectionTestUtils.setField(mapService, "tier3MaxKm", 15.0);
        ReflectionTestUtils.setField(mapService, "tier3Amount", 1200.0);
        ReflectionTestUtils.setField(mapService, "tier4Amount", 2000.0);
    }
}
