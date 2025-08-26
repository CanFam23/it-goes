package com.it_goes.api.config;

import com.it_goes.api.jpa.model.*;
import com.it_goes.api.jpa.repo.*;
import com.it_goes.api.util.enums.Country;
import com.it_goes.api.util.enums.Social;
import com.it_goes.api.util.enums.State;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(TripRepository tr, LocationRepository lr, UserRepository ur, SeasonRepository sr,
                               SocialLinkRepository slr, ImageRepository ir) {
        return args -> seedData(tr, lr, ur, sr, slr, ir);
    }

    @Transactional
    void seedData(TripRepository tr, LocationRepository lr, UserRepository ur, SeasonRepository sr,
                  SocialLinkRepository slr, ImageRepository ir){
        final Image profileImg = new Image("images/DSC02232.JPG","nclouse-pi") ;
        final User newUser = new User("nclouse","nickclouse03@gmail.com", "password","Nick","Clouse", profileImg);

        final SocialLink sl = new SocialLink("https://www.instagram.com/nick.clouse/", Social.INSTAGRAM);
        newUser.addSocialLink(sl);

        final Location location = new Location("Beehive Basin", State.MT, Country.US);
        final Trip t = new Trip("4th of July Couloir", newUser, location, LocalDate.of(2025,6,1));
        t.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");

        final Season season = new Season(LocalDate.of(2024,11,1),LocalDate.of(2025,10,1));

        sr.save(season);
        season.addTrip(t);
        t.setSeason(season);
        t.setCoverImageKey("images/DSC02993.jpg");

        newUser.addTripPosted(t);

        ur.save(newUser);
        lr.save(location);
        tr.save(t);
    }
}
