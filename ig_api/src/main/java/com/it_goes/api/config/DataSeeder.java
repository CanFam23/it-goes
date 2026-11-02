package com.it_goes.api.config;

import com.it_goes.api.jpa.model.*;
import com.it_goes.api.jpa.repo.*;
import com.it_goes.api.service.TripService;
import com.it_goes.api.util.enums.Country;
import com.it_goes.api.util.enums.Social;
import com.it_goes.api.util.enums.State;
import org.locationtech.jts.geom.LineString;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Date;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(TripRepository tr, LocationRepository lr, UserRepository ur, SeasonRepository sr,
                               SocialLinkRepository slr, ImageRepository ir, PasswordEncoder pe) {
        return args -> seedData(tr, lr, ur, sr, slr, ir, pe);
    }

    @Transactional
    void seedData(TripRepository tr, LocationRepository lr, UserRepository ur, SeasonRepository sr,
                  SocialLinkRepository slr, ImageRepository ir, PasswordEncoder pe){
        final Image profileImg = new Image("images/DSC02232.JPG","nclouse-pi") ;
        final User newUser = new User("nclouse","nickclouse03@gmail.com", pe.encode("password"),"Nick","Clouse", profileImg);

        final SocialLink sl = new SocialLink("https://www.instagram.com/nick.clouse/", Social.INSTAGRAM);
        newUser.addSocialLink(sl);

        final Location location = new Location("Beehive Basin", State.MT, Country.US);

        location.setLocation(45.355648984592534,-111.39572786673328);

        final Location location2 = new Location("Glacier National Park", State.MT, Country.US);

        location2.setLocation(48.617557525634766,-113.7603988647461);

        final Season season = new Season(2024);

        final Season season2 = new Season(2023);

        final Path path;
        try {
            path = new ClassPathResource("tracks/4th_july.gpx").getFile().toPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final LineString routeData = TripService.toLineString(path);

        sr.save(season);
        sr.save(season2);

        ur.save(newUser);
        lr.save(location);
        lr.save(location2);

        final User newUser2 = new User("cmar","connor@gmail.com", pe.encode("password"),"Connor","Marland",null);
        final User newUser3 = new User("jswea","jake@gmail.com", pe.encode("password"),"Jake","Sweatland",null);

        ur.save(newUser2);
        ur.save(newUser3);

        for(int i = 0; i < 8; i++){
            Trip t = new Trip("4th of July Couloir " + i, newUser, i % 2 == 0 ? location : location2, LocalDate.of(2025,6,i + 1));
            t.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
            t.setRoute(routeData);
            season.addTrip(t);
            t.setSeason(season);
            t.setCoverImageKey("images/DSC02993.jpg");

            newUser.addTripPosted(t);
            newUser2.addTrip(t);
            newUser3.addTrip(t);

            tr.save(t);

            t = new Trip("4th of July Couloir " + i, newUser, i % 2 == 0 ? location : location2, LocalDate.of(2024,6,i + 1));
            t.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
            t.setRoute(routeData);
            season2.addTrip(t);
            t.setSeason(season2);
            t.setCoverImageKey("images/DSC02993.jpg");

            newUser.addTripPosted(t);
            newUser2.addTrip(t);
            newUser3.addTrip(t);

            tr.save(t);
        }
    }
}
