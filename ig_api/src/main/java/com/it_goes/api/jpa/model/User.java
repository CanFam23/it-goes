package com.it_goes.api.jpa.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@ToString
@Entity
@Table(name="it_goes_user") // 'user' is reserved keyword
public class User {

    public User(String username, String email, String hashedPasswordString, String firstName, String lastName, Image profileImage) {
        this.username = username;
        this.email = email;
        this.hashedPasswordString = hashedPasswordString;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profileImage = profileImage;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, unique = true)
    private String username;

    @Setter
    @Column(nullable = false, unique = true)
    private String email;

    @Setter
    @ToString.Exclude
    @Column(nullable = false)
    private String hashedPasswordString;

    @Setter
    @Column(nullable = false)
    private String firstName;

    @Setter
    @Column(nullable = false)
    private String lastName;

    @Setter
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(cascade = CascadeType.ALL) // One-to-one relationship with Image
    @JoinColumn(name = "profile_image_id")
    private Image profileImage;

    // Unidirectional
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            cascade = { CascadeType.PERSIST, CascadeType.MERGE },
            orphanRemoval = true
    )
    @JoinColumn(name = "it_goes_user_id")
    private final Set<SocialLink> socialLinks = new HashSet<>();

    // Bidirectional
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "author")
    private final Set<Trip> tripsPosted = new HashSet<>();

    // Bidirectional, trips user went on
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(mappedBy = "users")
    private final Set<Trip> trips = new HashSet<>();

    public Set<SocialLink> getSocialLinks(){
        return Collections.unmodifiableSet(socialLinks);
    }

    public boolean addSocialLink(SocialLink sl){
        return socialLinks.add(sl);
    }

    public boolean removeSocialLink(SocialLink sl){
        return socialLinks.remove(sl);
    }

    public Set<Trip> getTripsPosted(){
        return Collections.unmodifiableSet(tripsPosted);
    }

    public boolean addTripPosted(Trip trip){
        if (trip == null) return false;
        
        if (tripsPosted.add(trip)){
            trip.setAuthor(this);
            return true;
        }
        return false;
    }

    public boolean removeTripPosted(Trip trip){
        if (trip == null) return false;

        if (tripsPosted.remove(trip)){
            trip.setAuthor(null);
            return true;
        }
        return false;
    }

    public Set<Trip> getTrips(){
        return Collections.unmodifiableSet(trips);
    }

    public boolean addTrip(Trip trip){
        if (trip == null) return false;

        if (trips.add(trip) && trip.addUser(this)){
            trip.setAuthor(this);
            return true;
        }
        // Reset sets if one add fails
        trips.remove(trip);
        trip.removeUser(this);
        return false;
    }

    public boolean removeTrip(Trip trip){
        if (trip == null) return false;

        if (trips.remove(trip) && trip.removeUser(this)){
            return true;
        }
        // Reset sets if one remove fails
        trips.add(trip);
        trip.addUser(this);
        return false;
    }
}
