package com.it_goes.api.jpa.repo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class SkiDayCountingTests {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgis/postgis:16-3.4");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired SkiDayLogRepository logs;

    @Test
    void overlappingTripsAndManualDaysCountOnceOverallAndPerLocation() {
        jdbc.update("INSERT INTO it_goes_user (id,email,first_name,hashed_password_string,last_name,username) VALUES (1,'a@example.com','Alice','hash','Skier','alice')");
        jdbc.update("INSERT INTO location (id,name,state,country) VALUES (1,'Beehive',0,0),(2,'Glacier',0,0)");
        jdbc.update("INSERT INTO trip (id,title,description,date_of_trip,date_posted,date_updated,author_id,location_id,cover_image_key) VALUES (1,'Trip',0,'2025-09-30','2025-09-30','2025-09-30',1,1,'cover'),(2,'Trip 2',0,'2025-09-30','2025-09-30','2025-09-30',1,1,'cover')");
        jdbc.update("INSERT INTO ski_day_log (user_id,location_id,ski_date) VALUES (1,1,'2025-09-30'),(1,2,'2025-09-30'),(1,1,'2025-10-01')");

        assertThat(logs.countDistinctDays(1L, LocalDate.of(2024,10,1), LocalDate.of(2025,9,30))).isEqualTo(1);
        assertThat(users.getDaysSkied(LocalDate.of(2024,10,1), LocalDate.of(2025,9,30)))
                .singleElement().satisfies(row -> assertThat(row.getDaysSkied()).isEqualTo(1));
        assertThat(users.getDaysSkiedEachLocation(LocalDate.of(2024,10,1), LocalDate.of(2025,9,30)))
                .hasSize(2).allSatisfy(row -> assertThat(row.getDaysSkied()).isEqualTo(1));
        assertThat(users.getDaysSkied()).extracting(row -> row.getYear()).containsExactlyInAnyOrder(2024, 2025);

        jdbc.update("DELETE FROM ski_day_log WHERE ski_date = '2025-09-30' AND location_id = 1");
        assertThat(logs.countDistinctDays(1L, LocalDate.of(2024,10,1), LocalDate.of(2025,9,30))).isEqualTo(1);

        jdbc.update("UPDATE ski_day_log SET ski_date = '2025-09-29' WHERE ski_date = '2025-10-01'");
        assertThat(logs.countDistinctDays(1L, LocalDate.of(2024,10,1), LocalDate.of(2025,9,30))).isEqualTo(2);
        jdbc.update("DELETE FROM ski_day_log WHERE ski_date = '2025-09-29'");
        assertThat(logs.countDistinctDays(1L, LocalDate.of(2024,10,1), LocalDate.of(2025,9,30))).isEqualTo(1);
    }

    @Test
    void migrationAllowsForeignLocationsWithoutStateButRequiresItForUs() {
        jdbc.update("INSERT INTO location (name, state, country) VALUES ('Chamonix', NULL, 3), ('Norway mountain', NULL, 6)");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM location WHERE state IS NULL", Integer.class)).isEqualTo(2);
        assertThatThrownBy(() -> jdbc.update("INSERT INTO location (name, state, country) VALUES ('US mountain', NULL, 0)"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
