package com.it_goes.api.jpa.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "trip")
public class Trip {

    public Trip(String title, User author, Location location, LocalDate dateOfTrip) {
        this.title = title;
        this.author = author;
        this.location = location;
        this.dateOfTrip = dateOfTrip;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String title;

    @Setter
    @Lob // Tells JPA to store data in Large Object (LOB), like TEXT
    @Column(nullable = false)
    private String description;

    @Setter
    @Column(nullable = false)
    private LocalDate dateOfTrip;

    @Setter
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDate datePosted;

    @Setter
    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDate dateUpdated;

    @Setter
    @Lob  // Store as binary large object (BLOB)
    @Column(nullable = true)
    private byte[] gpxContent;

    // Bidirectional
    @Setter
    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    // Bidirectional
    @Setter
    @ManyToOne
    @JoinColumn(name="location_id")
    private Location location;

    // Bidirectional
    @Setter
    @ManyToOne
    @JoinColumn(name="season_id")
    private Season season;

    // Unidirectional
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany
    @JoinTable(name = "image_trip",
        joinColumns = @JoinColumn(name="trip_id"),
        inverseJoinColumns = @JoinColumn(name="image_id")
    )
    private final Set<Image> images = new HashSet<>();

    // Bidirectional, users involved in the trip
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany
    @JoinTable(name = "trip_users",
        joinColumns = @JoinColumn(name="trip_id"),
        inverseJoinColumns = @JoinColumn(name="user_id")
    )
    private final Set<User> users = new HashSet<>();

    public Set<Image> getImages(){
        return Collections.unmodifiableSet(images);
    }

    public boolean addImage(Image img){
        return images.add(img);
    }

    public boolean removeImage(Image img){
        return images.remove(img);
    }

    public Set<User> getUsers(){
        return Collections.unmodifiableSet(users);
    }

    public boolean addUser(User user){
        return users.add(user);
    }

    public boolean removeUser(User user){
        return users.remove(user);
    }
}
