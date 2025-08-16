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
        final Image profileImg = new Image("images/DSC02232.JPG","https://it-goes.s3.us-east-2.amazonaws.com/images/DSC02232.JPG","nclouse-pi") ;
        final User newUser = new User("nclouse","nickclouse03@gmail.com", "password","Nick","Clouse", profileImg);

        final SocialLink sl = new SocialLink("https://www.instagram.com/nick.clouse/", Social.INSTAGRAM);
        newUser.addSocialLink(sl);

        final Location location = new Location("Beehive Basin", State.MT, Country.US);
        final Trip t = new Trip("4th of July Coulior", newUser, location, LocalDate.of(2025,6,1));
        t.setDescription("Awesome trip");

        final Season season = new Season(LocalDate.of(2024,11,1),LocalDate.of(2025,10,1));

        sr.save(season);
        season.addTrip(t);
        t.setSeason(season);

        newUser.addTripPosted(t);

        ur.save(newUser);
        lr.save(location);
        tr.save(t);
    }
}
