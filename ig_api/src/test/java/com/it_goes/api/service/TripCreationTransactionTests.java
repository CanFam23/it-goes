package com.it_goes.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.it_goes.api.dto.CreateTripRequest;
import com.it_goes.api.jpa.model.User;
import com.it_goes.api.jpa.repo.LocationRepository;
import com.it_goes.api.jpa.repo.UserRepository;
import com.it_goes.api.util.enums.Country;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = {
        "jwt.secret=dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQ=",
        "jwt.expirationMs=3600000"
})
@EnabledIfEnvironmentVariable(named = "IT_GOES_TX_TEST_DB_URL", matches = ".+")
class TripCreationTransactionTests {
    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getenv("IT_GOES_TX_TEST_DB_URL"));
        registry.add("spring.datasource.username", () -> System.getenv("IT_GOES_TX_TEST_DB_USER"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("IT_GOES_TX_TEST_DB_PASSWORD", ""));
    }

    @Autowired TripService trips;
    @Autowired UserRepository users;
    @Autowired LocationRepository locations;
    @Autowired ObjectMapper mapper;

    @Test
    void failedTripRollsBackItsNewLocation() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String username = "tx_" + suffix;
        users.save(new User(username, suffix + "@example.com", "hash", "Test", "User", null));
        String place = "Rollback " + suffix;
        var body = mapper.readTree("""
                {"root":{"type":"root","children":[{"type":"paragraph","children":[{"type":"text","text":"A trip","format":0}]}]}}
                """);
        var invalidRoute = mapper.readTree("""
                {"type":"LineString","coordinates":[]}
                """);
        var request = new CreateTripRequest("A trip", LocalDate.now().minusDays(1), null,
                new CreateTripRequest.NewLocation(place, Country.FR, null), List.of(), body, invalidRoute);

        assertThatThrownBy(() -> trips.createTrip(username, request)).hasMessageContaining("valid GPX route");
        assertThat(locations.existsByNameIgnoreCaseAndStateAndCountry(place, null, Country.FR)).isFalse();
    }
}
