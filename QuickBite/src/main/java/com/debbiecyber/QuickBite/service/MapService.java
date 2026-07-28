package com.debbiecyber.QuickBite.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.debbiecyber.QuickBite.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class MapService {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?q={address}&format=json&limit=1";
    private final RestTemplate restTemplate;

    @Value("${delivery.fee.tier1.maxKm}")
    private double tier1MaxKm;
    @Value("${delivery.fee.tier1.amount}")
    private double tier1Amount;
    @Value("${delivery.fee.tier2.maxKm}")
    private double tier2MaxKm;
    @Value("${delivery.fee.tier2.amount}")
    private double tier2Amount;
    @Value("${delivery.fee.tier3.maxKm}")
    private double tier3MaxKm;
    @Value("${delivery.fee.tier3.amount}")
    private double tier3Amount;
    @Value("${delivery.fee.tier4.amount}")
    private double tier4Amount;

    public double calculateDeliveryFee(String restaurantAddress, String deliveryAddress) {
        double[] restaurantCoordinates = getCoordinates(restaurantAddress);
        double[] customerCoordinates = getCoordinates(deliveryAddress);
        double distanceInKilometers = calculateHaversineDistance(
                restaurantCoordinates[0],
                restaurantCoordinates[1],
                customerCoordinates[0],
                customerCoordinates[1]
        );

        return getDeliveryFee(distanceInKilometers);
    }


    public double[] getCoordinates(String address) {
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, "QuickBite/1.0");
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    NOMINATIM_URL,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class,
                    address
            );
            String response = responseEntity.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response);

            if (root.isArray() && root.size() > 0) {
                JsonNode firstResult = root.get(0);
                double latitude = firstResult.path("lat").asDouble(Double.NaN);
                double longitude = firstResult.path("lon").asDouble(Double.NaN);
                if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
                    throw new BadRequestException("Map provider returned invalid coordinates");
                }
                return new double[]{latitude, longitude};
            }
            throw new BadRequestException("Address could not be located: " + address);
        } catch (BadRequestException exception) {
            throw exception;
        } catch (Exception e) {
            throw new BadRequestException("Unable to calculate delivery distance");
        }
    }


    private double calculateHaversineDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        final double EARTH_RADIUS_KILOMETERS = 6371.0;

        double latitudeDistance = Math.toRadians(latitude2 - latitude1);
        double longitudeDistance = Math.toRadians(longitude2 - longitude1);
        double a = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2)
                + Math.cos(Math.toRadians(latitude1))
                * Math.cos(Math.toRadians(latitude2))
                * Math.sin(longitudeDistance / 2)
                * Math.sin(longitudeDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KILOMETERS * c;
    }


    private double getDeliveryFee(double distanceInKilometers) {
        if (distanceInKilometers <= tier1MaxKm) {
            return tier1Amount;
        } else if (distanceInKilometers <= tier2MaxKm) {
            return tier2Amount;
        } else if (distanceInKilometers <= tier3MaxKm) {
            return tier3Amount;
        } else {
            return tier4Amount;
        }
    }
}
