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

        final Season season = new Season(2024);

        sr.save(season);

        ur.save(newUser);
        lr.save(location);

        final User newUser2 = new User("cmar","connor@gmail.com", "password","Connor","Marland",null);
        final User newUser3 = new User("jswea","jake@gmail.com", "password","Jake","Sweatland",null);

        ur.save(newUser2);
        ur.save(newUser3);

        for(int i = 0; i < 8; i++){
            final Trip t = new Trip("4th of July Couloir " + i, newUser, location, LocalDate.of(2025,6,i + 1));
            t.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");

            season.addTrip(t);
            t.setSeason(season);
            t.setCoverImageKey("images/DSC02993.jpg");

            newUser.addTripPosted(t);
            newUser2.addTrip(t);
            newUser3.addTrip(t);

            tr.save(t);
        }
    }
}
